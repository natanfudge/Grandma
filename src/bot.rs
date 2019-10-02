use std::env;
use serenity::{
    model::{channel::Message, gateway::Ready},
    prelude::*,
};
use serenity::model::id::ChannelId;
use crate::mappings::ClassMapping;
use crate::util::{VecExt, get_resource};
use std::fs::{File, remove_file};
use serenity::model::user::User;
use git2::Repository;
use crate::git::{YarnRepo, GitExt, GIT_EMAIL, GIT_USER, YARN_MAPPINGS_DIR, RELATIVE_MAPPINGS_DIR};
use std::path::Path;


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
        let content = msg.content;
        let words = content.split_whitespace().collect::<Vec<&str>>();
        if content.as_str() == "!ping" {
            // Sending a message can fail, due to a network error, an
            // authentication error, or lack of permissions to post in the
            // channel, so log to stdout when some error happens, with a
            // description of it.
            msg.channel_id.send("Pong!", &ctx)
        }
        if !words.is_empty() && words[0] == "rename" {
            if words.len() < 4 || words[2] != "to" {
                msg.channel_id.send("Incorrect syntax. Use: 'rename <class> to <new_name>'", &ctx);
            } else {
                if words.len() > 4 {
                    msg.channel_id.send("Ignoring the bit at the end there, you only need 4 words.", &ctx);
                }

                Handler::try_rename(&ctx, msg.channel_id, words[1], words[3], &msg.author);
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

// Create a new instance of the Client, logging in as a bot. This will
// automatically prepend your bot token with "Bot ", which is a requirement
// by Discord for bot users.
    let mut client = Client::new(&token, Handler).expect("Could not start bot");

//    {
//        let mut data = client.data.write();
//        data.insert::<Mappings>(mappings);
//    }

// Finally, start a single shard, and start listening to events.
//
// Shards will automatically attempt to reconnect, and will perform
// exponential backoff until it reconnects.
    if let Err(why) = client.start() {
        println!("Client error: {:?}", why);
    }
}

impl Handler {
    fn user_branch(user: &User) -> String {
        f!("{}{}",user.name,user.discriminator)
    }

    fn try_rename(ctx: &Context, channel_id: ChannelId, old_class_name: &str, new_class_name: &str, author: &User) {
        let repo = YarnRepo::get_git();
        let branch_name_str = Handler::user_branch(author);
        let branch_name = branch_name_str.as_str();
        println!("Switching to branch '{}'", branch_name);
        repo.create_branch_if_missing(branch_name);
        repo.switch_to_branch(branch_name);
        println!("Parsing mappings");
        let mut mappings = YarnRepo::get_current_mappings();

//        let mut data = ctx.data.write();
//        let mappings = data.get_mut::<Mappings>().unwrap();
        let mut matching_mappings = if old_class_name.contains('/') {
            mappings.filter_mut(|class| class.name_deobf.ends_with(old_class_name))
        } else {
            mappings.filter_mut(|class: &&mut ClassMapping| class.deobf_class_name() == old_class_name)
        };
        if matching_mappings.is_empty() {
            channel_id.send(fs!("No class named '{}'.",old_class_name), &ctx);
        } else if matching_mappings.len() == 1 {
            Handler::rename(ctx, channel_id, &mut matching_mappings[0], new_class_name, author, &repo)
        } else {
            let matching_class_names = matching_mappings
                .map(|class| class.name_deobf.clone())
                .join(",\n");
            channel_id.send(fs!("There are multiple classes with this name: \n{}\n\
Prefix the **original** class name with its enclosing package name followed by a '/'.",
                     matching_class_names
                    ), &ctx);
        };
    }

    fn rename(ctx: &Context, channel_id: ChannelId, class_mapping: &mut ClassMapping, new_class_name: &str,
              author: &User, repo: &Repository) {
        let old_name = class_mapping.name_deobf.clone();
        class_mapping.name_deobf = f!("{}/{}",class_mapping.deobf_package_name(), new_class_name);

        let new_path = format!("{}/{}.mapping", RELATIVE_MAPPINGS_DIR, class_mapping.name_deobf);
        let path = YarnRepo::get_path(new_path.as_str());
        if let Ok(new_mappings_file) = File::create(&path) {
            // Remove old file
            let old_path = format!("{}/{}.mapping",RELATIVE_MAPPINGS_DIR, old_name);
            if let Err(_error) = remove_file(YarnRepo::get_path(old_path.as_str())) {
                channel_id.send(fs!("Could not delete old mappings file at {:?}",old_path), ctx);
            }

            //TODO: don't do this when the file name does not change.
            repo.remove((old_path).as_ref());

            channel_id.send(fs!("Renamed {} to {}.", old_name,class_mapping.name_deobf), ctx);

            class_mapping.write(new_mappings_file);
            repo.stage_changes(new_path.as_ref());
            repo.commit_changes(GIT_USER, GIT_EMAIL, fs!("{} -> {}",old_name,class_mapping.name_deobf));

            let result = repo.push(Handler::user_branch(author).as_str());
            if let Err(error) = result {
                channel_id.send(fs!("There was a problem while pushing the changes to github: {:?}",error), ctx);
            } else {
                println!("Changes pushed to repository");
            }
        } else {
            channel_id.send(fs!("Could not save mappings to {:?}.",path), ctx);
            class_mapping.name_deobf = old_name;
        }
    }
}
