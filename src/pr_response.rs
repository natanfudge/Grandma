
use serde_json::de::Deserializer;
use serde::Serialize;
use serde::Deserialize;


#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct CreatePullRequestResponse {
    pub url: Option<String>,
    pub id: Option<i64>,
    pub node_id: Option<String>,
    pub html_url: Option<String>,
    pub diff_url: Option<String>,
    pub patch_url: Option<String>,
    pub issue_url: Option<String>,
    pub commits_url: Option<String>,
    pub review_comments_url: Option<String>,
    pub review_comment_url: Option<String>,
    pub comments_url: Option<String>,
    pub statuses_url: Option<String>,
    pub number: Option<i64>,
    pub state: Option<String>,
    pub locked: Option<bool>,
    pub title: Option<String>,
    pub user: Option<User>,
    pub body: Option<String>,
    pub labels: Option<Vec<Label>>,
    pub milestone: Option<Milestone>,
    pub active_lock_reason: Option<String>,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
    pub closed_at: Option<String>,
    pub merged_at: Option<String>,
    pub merge_commit_sha: Option<String>,
    pub assignee: Option<User>,
    pub assignees: Option<Vec<User>>,
    pub requested_reviewers: Option<Vec<User>>,
    pub requested_teams: Option<Vec<RequestedTeam>>,
    pub head: Option<Head>,
    pub base: Option<Base>,
    #[serde(rename = "_links")]
    pub links: Option<Links>,
    pub author_association: Option<String>,
    pub draft: Option<bool>,
    pub merged: Option<bool>,
    pub mergeable: Option<bool>,
    pub rebaseable: Option<bool>,
    pub mergeable_state: Option<String>,
    pub merged_by: Option<User>,
    pub comments: Option<i64>,
    pub review_comments: Option<i64>,
    pub maintainer_can_modify: Option<bool>,
    pub commits: Option<i64>,
    pub additions: Option<i64>,
    pub deletions: Option<i64>,
    pub changed_files: Option<i64>,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct User {
    pub login: Option<String>,
    pub id: Option<i64>,
    pub node_id: Option<String>,
    pub avatar_url: Option<String>,
    pub gravatar_id: Option<String>,
    pub url: Option<String>,
    pub html_url: Option<String>,
    pub followers_url: Option<String>,
    pub following_url: Option<String>,
    pub gists_url: Option<String>,
    pub starred_url: Option<String>,
    pub subscriptions_url: Option<String>,
    pub organizations_url: Option<String>,
    pub repos_url: Option<String>,
    pub events_url: Option<String>,
    pub received_events_url: Option<String>,
    #[serde(rename = "type")]
    pub type_field: Option<String>,
    pub site_admin: Option<bool>,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Label {
    pub id: Option<i64>,
    pub node_id: Option<String>,
    pub url: Option<String>,
    pub name: Option<String>,
    pub description: Option<String>,
    pub color: Option<String>,
    pub default: Option<bool>,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Milestone {
    pub url: Option<String>,
    pub html_url: Option<String>,
    pub labels_url: Option<String>,
    pub id: Option<i64>,
    pub node_id: Option<String>,
    pub number: Option<i64>,
    pub state: Option<String>,
    pub title: Option<String>,
    pub description: Option<String>,
    pub creator: Option<User>,
    pub open_issues: Option<i64>,
    pub closed_issues: Option<i64>,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
    pub closed_at: Option<String>,
    pub due_on: Option<String>,
}


#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct RequestedTeam {
    pub id: Option<i64>,
    pub node_id: Option<String>,
    pub url: Option<String>,
    pub html_url: Option<String>,
    pub name: Option<String>,
    pub slug: Option<String>,
    pub description: Option<String>,
    pub privacy: Option<String>,
    pub permission: Option<String>,
    pub members_url: Option<String>,
    pub repositories_url: Option<String>,
    pub parent: Option<::serde_json::Value>,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Head {
    pub label: Option<String>,
    #[serde(rename = "ref")]
    pub ref_field: Option<String>,
    pub sha: Option<String>,
    pub user: Option<User>,
    pub repo: Option<Repo>,
}


#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Repo {
    pub id: Option<i64>,
    pub node_id: Option<String>,
    pub name: Option<String>,
    pub full_name: Option<String>,
    pub owner: Option<User>,
    pub private: Option<bool>,
    pub html_url: Option<String>,
    pub description: Option<String>,
    pub fork: Option<bool>,
    pub url: Option<String>,
    pub archive_url: Option<String>,
    pub assignees_url: Option<String>,
    pub blobs_url: Option<String>,
    pub branches_url: Option<String>,
    pub collaborators_url: Option<String>,
    pub comments_url: Option<String>,
    pub commits_url: Option<String>,
    pub compare_url: Option<String>,
    pub contents_url: Option<String>,
    pub contributors_url: Option<String>,
    pub deployments_url: Option<String>,
    pub downloads_url: Option<String>,
    pub events_url: Option<String>,
    pub forks_url: Option<String>,
    pub git_commits_url: Option<String>,
    pub git_refs_url: Option<String>,
    pub git_tags_url: Option<String>,
    pub git_url: Option<String>,
    pub issue_comment_url: Option<String>,
    pub issue_events_url: Option<String>,
    pub issues_url: Option<String>,
    pub keys_url: Option<String>,
    pub labels_url: Option<String>,
    pub languages_url: Option<String>,
    pub merges_url: Option<String>,
    pub milestones_url: Option<String>,
    pub notifications_url: Option<String>,
    pub pulls_url: Option<String>,
    pub releases_url: Option<String>,
    pub ssh_url: Option<String>,
    pub stargazers_url: Option<String>,
    pub statuses_url: Option<String>,
    pub subscribers_url: Option<String>,
    pub subscription_url: Option<String>,
    pub tags_url: Option<String>,
    pub teams_url: Option<String>,
    pub trees_url: Option<String>,
    pub clone_url: Option<String>,
    pub mirror_url: Option<String>,
    pub hooks_url: Option<String>,
    pub svn_url: Option<String>,
    pub homepage: Option<String>,
    pub language: Option<::serde_json::Value>,
    pub forks_count: Option<i64>,
    pub stargazers_count: Option<i64>,
    pub watchers_count: Option<i64>,
    pub size: Option<i64>,
    pub default_branch: Option<String>,
    pub open_issues_count: Option<i64>,
    pub is_template: Option<bool>,
    pub topics: Option<Vec<String>>,
    pub has_issues: Option<bool>,
    pub has_projects: Option<bool>,
    pub has_wiki: Option<bool>,
    pub has_pages: Option<bool>,
    pub has_downloads: Option<bool>,
    pub archived: Option<bool>,
    pub disabled: Option<bool>,
    pub pushed_at: Option<String>,
    pub created_at: Option<String>,
    pub updated_at: Option<String>,
    pub permissions: Option<Permissions>,
    pub allow_rebase_merge: Option<bool>,
    pub template_repository: Option<::serde_json::Value>,
    pub allow_squash_merge: Option<bool>,
    pub allow_merge_commit: Option<bool>,
    pub subscribers_count: Option<i64>,
    pub network_count: Option<i64>,
}


#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Permissions {
    pub admin: Option<bool>,
    pub push: Option<bool>,
    pub pull: Option<bool>,
}

#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Base {
    pub label: Option<String>,
    #[serde(rename = "ref")]
    pub ref_field: Option<String>,
    pub sha: Option<String>,
    pub user: Option<User>,
    pub repo: Option<Repo>,
}



#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Links {
    #[serde(rename = "self")]
    pub self_field: Option<Link>,
    pub html: Option<Link>,
    pub issue: Option<Link>,
    pub comments: Option<Link>,
    pub review_comments: Option<Link>,
    pub review_comment: Option<Link>,
    pub commits: Option<Link>,
    pub statuses: Option<Link>,
}


#[derive(Default, Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct Link {
    pub href: Option<String>,
}


