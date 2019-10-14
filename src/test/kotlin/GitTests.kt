import grandma.*
import org.junit.BeforeClass
import util.getOrCloneGit
import java.io.File
import java.util.*
import kotlin.test.Test

class GitTests {

    companion object {
        @BeforeClass
        @JvmStatic
        fun clean() {
//            grandma.YarnRepo.clean()
        }
    }


    @Test
    fun `Can clone the yarn repository`() {
        YarnRepo.cloneIfMissing()
        assert(YarnRepo.MappingsDirectory.exists())
    }

    @Test
    fun `Branches retain information`() {
        getOrCloneGit().switchToBranch("secretInfo")
        assert(YarnRepo.getFile("secretTestInfo").exists())
        getOrCloneGit().switchToBranch("master")
        getOrCloneGit().switchToBranch("secretInfo")
        assert(YarnRepo.getFile("secretTestInfo").exists())
        getOrCloneGit().switchToBranch("secretInfo")
        assert(YarnRepo.getFile("secretTestInfo").exists())
    }

    @Test
    fun `Can switch to non-existent branch`() {
        val repo = getOrCloneGit()
        val branchName = "testBranch" + UUID.randomUUID()
        repo.switchToBranch(branchName)
        assert(repo.getBranches().any { it.name == "refs/heads/$branchName" })
    }

    @Test
    fun `Can push changes to remote`() {
        val repo = getOrCloneGit()
        repo.stageChanges("MAINTAINERS")
        repo.commit(author = YarnRepo.TemporaryAuthor, commitMessage = "Test Commit")
        YarnRepo.push(repo)
    }

    @Test
    fun `Git remove deletes file`() {
        val repo = getOrCloneGit()
        repo.remove("mappings/afo.mapping")
        assert(!File("yarn/mappings/README.md").exists())
    }
}