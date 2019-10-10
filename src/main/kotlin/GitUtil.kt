import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.RmCommand
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.ChainingCredentialsProvider
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.URIish

fun Git.switchToBranch(branchName: String): Ref = checkout()
    .setCreateBranch(true).setName(branchName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
    .setStartPoint("origin/$branchName").call()


fun Git.remove(path: String): DirCache = RmCommand(repository).addFilepattern(path).call()

fun Git.stageChanges(path: String): DirCache = add().addFilepattern(path).call()
fun Git.commit(author: PersonIdent, commitMessage: String): RevCommit = commit()
    .setAuthor(author)
    .setCommitter(author)
    .setMessage(commitMessage)
    .call()

fun Git.actuallyPush(remoteUrl: String, credentialsProvider: CredentialsProvider) {
    remoteAdd().setName("origin").setUri(URIish(remoteUrl)).call()
    push().setCredentialsProvider(credentialsProvider).call()
}