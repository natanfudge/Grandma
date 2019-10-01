use git2::{Repository, Oid, Commit};
use git2::build::CheckoutBuilder;
use std::path::{Path, PathBuf};
use crate::util::get_resource;
use std::str::FromStr;

pub trait GitExt {
    fn create_branch_if_missing(&self, branch_name: &str);
    fn switch_to_branch(&self, branch_name: &str);
    fn get_head_commit(&self) -> Commit;
}

impl GitExt for Repository {
    fn create_branch_if_missing(&self, branch_name: &str) {
        let commit = self.get_head_commit();
        self.branch(branch_name, &commit, false);
    }
    fn switch_to_branch(&self, branch_name: &str) {
        let mut checkout = CheckoutBuilder::new();
        checkout.safe();
        let tree = self.revparse_single(branch_name).unwrap();
        self.checkout_tree(&tree, Some(&mut checkout)).unwrap();
        self.set_head(fs!("refs/heads/{}",branch_name)).unwrap();
    }

    fn get_head_commit(&self) -> Commit {
        self.find_commit(self.refname_to_id("HEAD").unwrap()).unwrap()
    }
}

const UPSTREAM_YARN_REPO: &str = "https://github.com/FabricMC/yarn";
const LOCAL_YARN_REPO: &str = "yarn";
const YARN_MAPPINGS_DIR: &str = "yarn/mappings";

pub struct YarnRepo;

impl YarnRepo {
    fn get_or_clone(local_repo_path: PathBuf, remote_repo_url: &str) -> Repository {
        if local_repo_path.exists() {
            Repository::open(local_repo_path)
        } else {
            Repository::clone(remote_repo_url, local_repo_path)
        }.unwrap()
    }



    pub fn get_git() -> Repository {
        Repository::open(YARN_MAPPINGS_DIR).expect("Could not open yarn repository")
    }

    pub fn clone_yarn() -> Repository {
        YarnRepo::get_or_clone(get_resource(LOCAL_YARN_REPO),
                               UPSTREAM_YARN_REPO)
    }

    pub fn get_mappings_directory() -> PathBuf {
        get_resource(LOCAL_YARN_REPO)
    }
}