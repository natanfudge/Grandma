#![allow(unused_imports)]

use crate::mappings::ClassMapping;
use crate::util::{get_test_resource, get_resource, VecExt, ReadContentsExt};
use std::fs::{read_dir, File};
use walkdir::{WalkDir, DirEntry};
use git2::{Repository, Oid};
use git2::build::CheckoutBuilder;
use crate::git::{GitExt, YARN_REPO};


#[macro_use]
mod foo {
    macro_rules! f {
        ($($arg:tt)*) => (format!($($arg)*))
    }

    macro_rules! fs {
        ($($arg:tt)*) => (format!($($arg)*).as_str())
    }
}
const MAPPINGS_DIR: &str = "yarn/mappings";
const MAPPING_GIT_ROOT : &str = "yarn";
mod bot;
mod parse;
mod mappings;
mod tests;
mod util;
mod write;
mod pr_response;
mod github;
mod git;




fn main() {
//    let repository = Repository::open(get_resource(MAPPING_GIT_ROOT))
//        .expect("Could not open yarn repository");

//    git2::Repository::clone(YARN_REPO,get_resource("Fudge")).unwrap();

//    github::send_pr();
//    println!("Parsing mappings...");
//    let mappings_dir = get_resource(MAPPINGS_DIR);
//
//    let mappings: Vec<ClassMapping> = WalkDir::new(mappings_dir)
//        .into_iter()
//        .filter_map(Result::ok)
//        .filter(|file: &DirEntry| !file.file_type().is_dir())
//        .map(|file: DirEntry|
//            ClassMapping::parse(File::open(file.into_path()).expect("Could not open file"))
//        ).collect();
//
//    println!("Starting bot...");
//    bot::start_bot(mappings);
}