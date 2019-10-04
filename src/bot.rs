use std::env;
use serenity::{
    model::{channel::Message, gateway::Ready},
    prelude::*,
};
use serenity::model::id::ChannelId;
use crate::mappings::{ClassMapping, Mapping};
use crate::util::{VecExt, get_resource};
use std::fs::{File, remove_file, create_dir_all};
use serenity::model::user::User;
use git2::Repository;
use crate::git::{YarnRepo, GitExt, GIT_EMAIL, GIT_USER, YARN_MAPPINGS_DIR, RELATIVE_MAPPINGS_DIR};
use std::path::{Path, PathBuf};
use crate::query::{Rename, ClassRename, ClassPath, RenameResult, MAPPING_EXT_LENGTH};


pub struct Handler;

trait Messenger {
    fn send(&self, message: &str, context: &Context);
    fn send_boxed(&self, message: String, context: &Context);
}

impl Messenger for ChannelId {
    fn send(&self, message: &str, context: &Context) {
        if let Err(why) = self.say(&context.http, message) {
            println!("Error sending message: {:?}", why);
        }
    }

    fn send_boxed(&self, message: String, context: &Context) {
        if let Err(why) = self.say(&context.http, message.as_str()) {
            println!("Error sending message: {:?}", why);
        }
    }
}

struct Mappings;

impl TypeMapKey for Mappings {
    type Value = Vec<ClassMapping>;
}


impl EventHandler for Handler {
    // Set a handler for the `message` event - so that whenever a new message
    // is received - the closure (or function) passed will be called.
    //
    // Event handlers are dispatched through a threadpool, and so multiple
    // events can be dispatched simultaneously.
    fn message(&self, ctx: Context, msg: Message) {
        let context = CommandContext { context: ctx, message: msg };
        let content = &context.message.content;
        let words = content.split_whitespace().collect::<Vec<&str>>();
        if content.as_str() == "!ping" {
            context.send("Pong!")
        }
        if !words.is_empty() && (words[0] == "rename" || words[0] == "name") {
            if words[0] == "rename" {
                if words.len() < 4 || words[2] != "to" {
                    context.send("Incorrect syntax. Use: 'rename <named_class> to <new_name>'");
                } else {
                    if words.len() > 4 {
                        context.send("Ignoring the bit at the end there, you only need 4 words.");
                    }
                    try_rename(words[1], words[3], false, &context)
                }
            } else {
                if words.len() < 3 {
                    context.send("Incorrect syntax. Use: 'name <unnamed_class> <new_name>'");
                } else {
                    if words.len() > 3 {
                        context.send("Ignoring the bit at the end there, you only need 3 words.");
                    }
                    try_rename(words[1], words[2], true, &context)
                }
            }
        }
    }

    // Set a handler to be called on the `ready` event. This is called when a
// shard is booted, and a READY payload is sent by Discord. This payload
// contains data like the current user's guild Ids, current user data,
// private channels, and more.
//
// In this case, just print what the current user's username is.
    fn ready(&self, _: Context, ready: Ready) {
        println!("{} is connected!", ready.user.name);
    }
}


pub fn start_bot() {
    let token = env::var("DISCORD_TOKEN")
        .expect("Expected a token in the environment");

    let mut client = Client::new(&token, Handler).expect("Could not start bot");

    if let Err(why) = client.start() {
        println!("Client error: {:?}", why);
    }
}

//TODO: rename methods(class#method), rename inner classes (class$class), rename fields (class#field),
// rename parameters (class#method[index]), name things that are not named yet

//TODO: implement this properly
fn parse_rename(old_name: &str, new_name: &str, search_obf: bool) -> Box<dyn Rename> {
    return Box::new(
        ClassRename {
            old_name: ClassPath { class_name: old_name.to_string(), package: None },
            new_name: ClassPath { class_name: new_name.to_string(), package: None },
            inner_class: None,
            search_obf,
        }
    );
}

struct CommandContext { context: Context, message: Message }

impl CommandContext {
    fn send(&self, message: &str) {
        if let Err(why) = self.message.channel_id.say(&self.context.http, message) {
            println!("Error sending message: {:?}", why);
        }
    }
}


fn human_class_path(class_path: PathBuf) -> String {
    let diff = pathdiff::diff_paths(class_path.as_ref(),
                                    YarnRepo::get_mappings_directory().as_ref()).unwrap();
    let string = diff.to_str().unwrap();
    string[..string.len() - MAPPING_EXT_LENGTH].to_string()
}

fn try_rename(old_name: &str, new_name: &str, search_obf: bool, context: &CommandContext) {
    let renamer = parse_rename(old_name, new_name, search_obf);

    let repo = YarnRepo::get_git();
    let branch_name_str = user_branch_name(&context.message.author);
    let branch_name = branch_name_str.as_str();
    println!("Switching to branch '{}'", branch_name);
    repo.create_branch_if_missing(branch_name);
    repo.switch_to_branch(branch_name);
    println!("Parsing mappings");

    let mappings = YarnRepo::find(&renamer);

    if mappings.is_empty() {
        if search_obf {
            context.send(fs!("No intermediary class name '{}' or the class has been already named.",old_name));
        } else {
            context.send(fs!("No class named '{}'.",old_name));
        }
    } else if mappings.len() == 1 {
        rename(renamer, &mappings[0], context, &repo)
    } else {
        let matching_class_names = mappings
            .map_into(move |path| {
                human_class_path( path)
            }
            )
            .join(",\n");
        context.send(fs!("There are multiple classes with this name: \n{}\n\
Prefix the **original** class name with its enclosing package name followed by a '/'.",
                     matching_class_names
                    ));
    };
}

// Stop from renaming to something that already exists
fn rename(renamer: Box<dyn Rename>, mappings: &Path, context: &CommandContext, repo: &Repository) {
    let mut mapping = ClassMapping::parse(mappings);

    let rename_result = renamer.rename(&mut mapping);
    let new_mappings_location = rename_result.new_mapping_location;
    let human_old_name = rename_result.human_old_name;
    let human_new_name = rename_result.human_new_name;

    let old_path = YarnRepo::get_path(mappings);
    let new_path = YarnRepo::get_path(&new_mappings_location);
    if let Err(error) = create_dir_all(new_path.parent().unwrap()) {
        println!("Could not create directories for path {:?} : {:?}", new_path, error);
    }

    match File::create(&new_path) {
        Ok(new_mappings_file) => {
            // Remove old file

            if let Err(error) = remove_file(&old_path) {
                println!("Could not delete old mappings file at {:?} : {:?}", old_path, error);
            }

            //TODO: don't do this when the file name does not change.
            println!("Removing path = {:?}", old_path);
            if let Err(error) = repo.remove(YarnRepo::relative_path(&old_path)){
                println!("Could not remove file '{:?}' from git: {:?}", old_path,error);
            }

            context.send(fs!("Renamed {} to {}.", human_old_name,human_new_name));

            mapping.write(new_mappings_file);
            repo.stage_changes(new_mappings_location);
            repo.commit_changes(GIT_USER, GIT_EMAIL, fs!("{} -> {}",
        human_old_name,human_new_name));

            let result = repo.push(user_branch_name(&context.message.author).as_str());
            if let Err(error) = result {
                context.send(fs!("There was a problem while pushing the changes to github: {:?}",error));
            } else {
                println!("Changes pushed to repository");
            }
        }
        Err(error) => context.send(fs!("Could not save mappings to {:?} : {:?}",new_mappings_location,error))
    };
}

fn user_branch_name(user: &User) -> String {
    f!("{}{}",user.name,user.discriminator)
}

