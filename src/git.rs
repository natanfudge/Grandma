use git2::{Repository, Oid, Commit, Signature};
use git2::build::CheckoutBuilder;
use std::path::{Path, PathBuf};
use crate::util::get_resource;
use std::str::FromStr;

pub trait GitExt {
    fn create_branch_if_missing(&self, branch_name: &str);
    fn switch_to_branch(&self, branch_name: &str);
    fn get_head_commit(&self) -> Commit;
    fn stage_changes(&self, changed_file : &Path);
    fn commit_changes(&self, author_name : &str, author_email : &str, message : &str) -> Oid;
}

impl GitExt for Repository {
    fn create_branch_if_missing(&self, branch_name: &str) {
        let commit = self.get_head_commit();
        self.branch(branch_name, &commit, false).expect("Could not create branch");
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

    fn stage_changes(&self, changed_file : &Path){
        let mut index = self.index().expect("Could not find git index");
        index.add_path(changed_file).expect("Could not add file to git");
        index.write().expect("Could not write index changes to disk");
    }

    fn commit_changes(&self, author_name : &str, author_email : &str, message : &str) -> Oid{
        let tree = self.find_tree(self.index().expect("Could not get git index").write_tree().unwrap()).unwrap();
        let parent = self.get_head_commit();

        let signature = Signature::now(author_name,author_email).unwrap();

        self.commit(
            Some("HEAD"),
            &signature,
            &signature,
            message,
            &tree,
            &[&parent]
        ).expect("Could not commit changes")
    }

}

const ORIGIN_YARN_REPO: &str = "https://github.com/natanfudge/yarn";
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
                               ORIGIN_YARN_REPO)
    }

    pub fn get_mappings_directory() -> PathBuf {
        get_resource(LOCAL_YARN_REPO)
    }
}