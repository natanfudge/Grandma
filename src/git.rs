use git2::Repository;
use git2::build::CheckoutBuilder;
use std::path::{Path, PathBuf};

pub trait GitExt {
    fn create_branch(&self, branch_name: &str);
    fn switch_to_branch(&self, branch_name: &str);
}

impl GitExt for Repository {
    fn create_branch(&self, branch_name: &str) {
        let commit = self.find_commit(self.refname_to_id("HEAD").unwrap()).unwrap();
        self.branch(branch_name, &commit, false).unwrap();
    }

    fn switch_to_branch(&self, branch_name: &str) {
        let mut checkout = CheckoutBuilder::new();
        checkout.safe();
        let tree = self.revparse_single(branch_name).unwrap();
        self.checkout_tree(&tree, Some(&mut checkout)).unwrap();
        self.set_head(fs!("refs/heads/{}",branch_name)).unwrap();
    }
}
pub const YARN_REPO : &str = "https://github.com/FabricMC/yarn";
pub struct YarnRepo;

impl YarnRepo {
    fn get_or_clone(local_repo_path: PathBuf, remote_repo_url: &str) -> Repository {
        if local_repo_path.exists() {
            Repository::open(local_repo_path)
        } else {
            Repository::clone(remote_repo_url, local_repo_path)
        }.unwrap()
    }

    pub fn get_or_clone_yarn(local_repo_path: PathBuf) -> Repository {
        YarnRepo::get_or_clone(local_repo_path,YARN_REPO)
    }
}