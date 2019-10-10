import org.junit.BeforeClass
import kotlin.test.Test

class GitTests {

    companion object {
        @BeforeClass
        @JvmStatic
        fun clean() {
            YarnRepo.clean()
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
    }

    @Test
    fun `Can push changes to remote`() {
        val repo = YarnRepo.getOrClone()
        repo.stageChanges("MAINTAINERS")
        repo.commit(author = YarnRepo.TemporaryAuthor, commitMessage = "Test Commit")
        YarnRepo.push(repo)
    }
}