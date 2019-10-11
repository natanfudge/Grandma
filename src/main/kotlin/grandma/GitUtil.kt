package grandma

import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.URIish

fun Git.switchToBranch(branchName: String) {
    if (repository.branch == branchName) return

    val remoteBranchExists = branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call()
        .any { it.name == "refs/remotes/origin/$branchName" }

    val localBranchAlreadyExists = branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
        .any { it.name == "refs/heads/$branchName" }

    val startPoint = when {
        localBranchAlreadyExists -> branchName
        remoteBranchExists -> "origin/$branchName"
        else -> "master"
    }

    checkout()
        .setCreateBranch(!localBranchAlreadyExists)
        .setName(branchName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
        .setStartPoint(startPoint).call()

}


fun Git.remove(path: String): DirCache {
    return rm().addFilepattern(path).call()
}

fun Git.stageChanges(path: String): DirCache {
    return add().addFilepattern(path).call()
}

fun Git.commit(author: PersonIdent, commitMessage: String): RevCommit = commit()
    .setAuthor(author)
    .setCommitter(author)
    .setMessage(commitMessage)
    .call()

fun Git.actuallyPush(remoteUrl: String, credentialsProvider: CredentialsProvider) {
    remoteAdd().setName("origin").setUri(URIish(remoteUrl)).call()
    push().setCredentialsProvider(credentialsProvider).call()
}