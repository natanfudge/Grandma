use std::env;
use serenity::{
    model::{channel::Message, gateway::Ready},
    prelude::*,
};
use serenity::model::id::ChannelId;
use crate::mappings::ClassMapping;


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
                let mut data = ctx.data.write();
                let mappings = data.get_mut::<Mappings>().unwrap();
                let old_name_input = words[1];
                let new_name = words[3];
                let mut matching_mappings = if old_name_input.contains('/') {
                    mappings.iter_mut()
                        .filter(|class| class.name_deobf.ends_with(old_name_input))
                        .collect::<Vec<&mut ClassMapping>>()
                } else {
                    mappings.iter_mut()
                        .filter(|class: &&mut ClassMapping| class.deobf_class_name() == old_name_input)
                        .collect::<Vec<&mut ClassMapping>>()
                };

                if matching_mappings.is_empty() {
                    msg.channel_id.send_boxed(f!("No class named '{}'.",old_name_input), &ctx)
                } else if matching_mappings.len() == 1 {
                    let class_mapping = &mut matching_mappings[0];
                    let old_name = class_mapping.name_deobf.clone();
                    class_mapping.name_deobf = f!("{}/{}",class_mapping.deobf_package_name(), new_name);
                    msg.channel_id.send_boxed(f!("Renamed {} to {}.", old_name,class_mapping.name_deobf), &ctx)
                } else {
                    //TODO: have standard list function shit
                    let matching_class_names = matching_mappings.iter()
                        .map(|class| class.name_deobf.clone())
                        .collect::<Vec<String>>()
                        .join("\n");
                    msg.channel_id.send_boxed(f!("There are multiple classes with this name: {}\n. \
Prefix your class name with its enclosing package name followed by a '/'.",
                     matching_class_names
                    ), &ctx)
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


pub fn start_bot(mappings: Vec<ClassMapping>) {
    let token = env::var("DISCORD_TOKEN")
        .expect("Expected a token in the environment");

// Create a new instance of the Client, logging in as a bot. This will
// automatically prepend your bot token with "Bot ", which is a requirement
// by Discord for bot users.
    let mut client = Client::new(&token, Handler).expect("Err creating client");

    {
        let mut data = client.data.write();
        data.insert::<Mappings>(mappings);
    }

// Finally, start a single shard, and start listening to events.
//
// Shards will automatically attempt to reconnect, and will perform
// exponential backoff until it reconnects.
    if let Err(why) = client.start() {
        println!("Client error: {:?}", why);
    }
}
