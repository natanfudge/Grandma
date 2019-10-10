import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.RmCommand
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.lib.Ref

fun Git.switchToBranch(branchName: String): Ref = checkout()
    .setCreateBranch(true).setName(branchName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
        .setStartPoint("origin/$branchName").call()


fun Git.remove(path : String): DirCache = RmCommand(repository).addFilepattern(path).call()