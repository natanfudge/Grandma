#![allow(unused_imports)]

use crate::mappings::ClassMapping;
use crate::util::{get_test_resource, get_resource, VecExt, ReadContentsExt};
use std::fs::{read_dir, File};
use walkdir::{WalkDir, DirEntry};
use git2::{Repository, Oid, Tree, Commit, Index, Signature, Direction, PushOptions, ProxyOptions, RemoteCallbacks, Cred};
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

//TODO: plan:
// - Maintain a singular git repository that exists in github.com at all times.
// - Whenever the bot starts, it will clone that repository for itself.
// - Whenever a person makes a rename, the git repository will switch to his own branch or create one as needed.
//   - The git repository will be modified with the change proposed,
//     and then the changes will be immediately commited locally, and pushed to github.com
//   - The author can specify an explanation to the rename.
//   - The full rename and the explanation will be repeated by the bot,
//     or a [no explanation] will be shown if there is no explanation.
//   - Explanation will be stored in a file in the branch and deleted when the pull request is made.
// - When a person wishes to submit his renames, he must specify a name for the mappings set,
//   and an author in the form of a github link,
//   and a new branch will be created with the changes he has made, named with the name of the mappings set.
//   - A pull request will then be immediately made from the created branch to the latest branch of yarn,
//     or, to a branch he will specify.
//   - His original branch will be updated to the latest version of yarn.
//   - At any time, he may do renames while specifying the pull request ID, and changes will be made to that PR specifically.
//   - The PR will specify the author has collaborated in making the PR.
//   - The author can be "anonymous".
//   - The pull request will provide a detailed list of changes in the body in an easy-to-read format,
//     together with the explanations provided during renaming.


//TODO: Version 2:
// - Users may message the bot directly.
// - Users may register their github name and email and bind it to their discord ID.
// This will be stored in a database and they will be given full credit for commits made in their name.
// - Branches will be stored in a database with the date they were last modified.
//    - Whenever a change is made, the bot will check if it conflicts with any branches that have recent changes (a week or so)


//TODO: test that branches are preserved between different deploys (deletions of the repo)



fn main() -> Result<(), git2::Error> {
    println!("Program started!");
    println!("Cloning yarn...");
    let repo = YarnRepo::clone_yarn();



    println!("Starting bot");
    start_bot();

    Ok(())
}
