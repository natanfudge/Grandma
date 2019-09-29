use crate::mappings::ClassMapping;
use crate::util::get_test_resource;


#[macro_use]
mod foo {
    macro_rules! f {
        ($($arg:tt)*) => (format!($($arg)*))
    }
}
mod bot;
mod parse;
mod mappings;
mod tests;
mod util;
mod write;
struct A;


fn main() {
    println!("Parsing mappings...");
    let mappings = ClassMapping::parse(get_test_resource("Block.mapping"));
    println!("Starting bot...");
    bot::start_bot(vec![mappings]);
}
