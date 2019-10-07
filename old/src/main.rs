#![allow(unused_imports)]

use crate::mappings::ClassMapping;
use crate::util::{get_test_resource, get_resource, VecExt, ReadContentsExt};
use std::fs::{read_dir, File, remove_file};
use walkdir::{WalkDir, DirEntry};
use git2::{Repository, Oid, Tree, Commit, Index, Signature, Direction, PushOptions, ProxyOptions, RemoteCallbacks, Cred, BranchType};
use git2::build::CheckoutBuilder;
use crate::git::{GitExt, YarnRepo};
use std::path::{PathBuf, Path};
use std::str::FromStr;
use serenity::cache::FromStrAndCache;
use crate::bot::start_bot;


#[macro_use]
mod foo {
    macro_rules! f {
        ($($arg:tt)*) => (format!($($arg)*))
    }

    macro_rules! fs {
        ($($arg:tt)*) => (format!($($arg)*).as_str())
    }

    macro_rules! concat {
        ($part1:expr,$part2:expr) => (fs!("{}{}",$part1,$part2))
    }
}


mod util;
mod parse;
mod mappings;
mod tests;
mod write;
mod pr_response;
mod github;
mod git;
mod bot;
mod query;




fn main() -> Result<(), git2::Error> {
    println!("Program started!");
    println!("Cloning yarn...");
    let repo = YarnRepo::clone_yarn();

    println!("Starting bot");
    start_bot();

    Ok(())
}
