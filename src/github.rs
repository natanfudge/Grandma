use std::io::Read;
use crate::pr_response::CreatePullRequestResponse;
use serde::Serialize;
use serde::Deserialize;
use git2::Repository;
use crate::util::{get_resource, ReadContentsExt};
use std::fs::File;

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
struct PullRequest {
    title: String,
    body: String,
    head: String,
    base: String,
}

const HEAD_OWNER: &str = "natanfudge";
const TARGET_VERSION: &str = "19w04b";
const GITHUB_API_KEY: &str = env!("GRANDMA_GITHUB_API_KEY");

pub fn send_pr(branch: &str, title: &str, body: &str) {
    let client = reqwest::Client::new();
    let request = PullRequest {
        title: title.to_string(),
        body: body.to_string(),
        head: f!("{}:{}",HEAD_OWNER,branch),
        base: TARGET_VERSION.to_string(),
    };
    let res_result = client.post("https://api.github.com/repos/shedaniel/yarn/pulls")
        .header("Authorization", fs!("token {}",GITHUB_API_KEY))
        .body(serde_json::to_string(&request).unwrap()).send();


    match res_result {
        Ok(mut res) => {
            let mut body = String::new();
            res.read_to_string(&mut body).expect("Could not read response");
            let response: serde_json::error::Result<CreatePullRequestResponse> = serde_json::from_str(body.as_str());

            match response {
                Err(error) => println!("Error opening pull request: {}", error),
                Ok(pr) => println!("Pull request opened successfully at {}", pr.issue_url.unwrap())
            }
        }
        Err(error) => {
            println!("Error opening pull request: {}", error);
        }
    }
}

