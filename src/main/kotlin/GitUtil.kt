import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git

fun Git.switchToBranch(branchName: String) {
    checkout().setCreateBranch(true).setName(branchName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
        .setStartPoint("origin/$branchName").call()
}