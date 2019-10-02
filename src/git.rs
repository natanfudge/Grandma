use git2::{Repository, Oid, Commit, Signature, Direction, PushOptions, RemoteCallbacks, Cred};
use git2::build::CheckoutBuilder;
use std::path::{Path, PathBuf};
use crate::util::get_resource;
use std::str::FromStr;

const ORIGIN_YARN_REPO: &str = "https://github.com/natanfudge/yarn";
const LOCAL_YARN_REPO: &str = "yarn";
pub const YARN_MAPPINGS_DIR: &str = "yarn/mappings";
pub const RELATIVE_MAPPINGS_DIR: &str = "mappings";

pub const GIT_USER  :&str = "natanfudge";
pub const GIT_EMAIL  :&str = "natan.lifsiz@gmail.com";

pub trait GitExt {
    fn create_branch_if_missing(&self, branch_name: &str);
    fn switch_to_branch(&self, branch_name: &str);
    fn get_head_commit(&self) -> Commit;
    fn stage_changes(&self, changed_file: &Path);
    fn commit_changes(&self, author_name: &str, author_email: &str, message: &str) -> Oid;
    fn push(&self, branch: &str) -> Result<(), git2::Error>;
    fn remove(&self, path :&Path) -> Result<(),git2::Error>;
}

fn create_callbacks<'a>() -> RemoteCallbacks<'a> {
    let mut callbacks = RemoteCallbacks::new();
    &callbacks.credentials(|str, str_opt, cred_type| {
        Cred::userpass_plaintext(GIT_USER, env!("GITHUB_PASSWORD"))
    });
    callbacks
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

    fn stage_changes(&self, changed_file: &Path) {
        let mut index = self.index().expect("Could not find git index");
        index.add_path(changed_file).expect("Could not add file to git");
        index.write().expect("Could not write index changes to disk");
    }

    fn commit_changes(&self, author_name: &str, author_email: &str, message: &str) -> Oid {
        let tree = self.find_tree(self.index().expect("Could not get git index").write_tree().unwrap()).unwrap();
        let parent = self.get_head_commit();

        let signature = Signature::now(author_name, author_email).unwrap();

        self.commit(
            Some("HEAD"),
            &signature,
            &signature,
            message,
            &tree,
            &[&parent],
        ).expect("Could not commit changes")
    }

    fn push(&self, branch: &str) -> Result<(), git2::Error> {
        let mut remote = self.find_remote("origin").unwrap();

        remote.connect_auth(Direction::Push, Some(create_callbacks()), None)?;
        let refspec_str = format!("+refs/heads/{}:refs/heads/{}",branch,branch);
        self.remote_add_push("origin", refspec_str.as_str())?;
        let mut push_options = PushOptions::default();
        let callbacks = create_callbacks();
        push_options.remote_callbacks(callbacks);

        remote.push(&[refspec_str.as_str()], Some(&mut push_options))?;

        std::mem::drop(remote);

        Ok(())
    }

    fn remove(&self, path :&Path) -> Result<(),git2::Error>{
        self.index().unwrap().remove_path(path)
    }
}



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
        Repository::open(get_resource(LOCAL_YARN_REPO)).expect("Could not open yarn repository")
    }

    pub fn clone_yarn() -> Repository {
        YarnRepo::get_or_clone(get_resource(LOCAL_YARN_REPO),
                               ORIGIN_YARN_REPO)
    }

    pub fn get_mappings_directory() -> PathBuf {
        get_resource(YARN_MAPPINGS_DIR)
    }
}