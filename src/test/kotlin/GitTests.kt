import grandma.*
import org.junit.BeforeClass
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
        YarnRepo.getOrClone()
        assert(YarnRepo.MappingsDirectory.exists())
    }

    @Test
    fun `Branches retain information`() {
        YarnRepo.getOrClone().switchToBranch("secretInfo")
        assert(YarnRepo.getFile("secretTestInfo").exists())
        YarnRepo.getOrClone().switchToBranch("master")
        YarnRepo.getOrClone().switchToBranch("secretInfo")
        assert(YarnRepo.getFile("secretTestInfo").exists())
        YarnRepo.getOrClone().switchToBranch("secretInfo")
        assert(YarnRepo.getFile("secretTestInfo").exists())
    }

    @Test
    fun `Can switch to non-existent branch`() {
        val repo = YarnRepo.getOrClone()
        val branchName = "testBranch" + UUID.randomUUID()
        repo.switchToBranch(branchName)
        assert(repo.branchList().call().any { it.name == "refs/heads/$branchName" })
    }

    @Test
    fun `Can push changes to remote`() {
        val repo = YarnRepo.getOrClone()
        repo.stageChanges("MAINTAINERS")
        repo.commit(author = YarnRepo.TemporaryAuthor, commitMessage = "Test Commit")
        YarnRepo.push(repo)
    }

    @Test
    fun `Git remove deletes file`() {
        val repo = YarnRepo.getOrClone()
        repo.remove("mappings/afo.mapping")
        assert(!File("yarn/mappings/README.md").exists())
    }
}