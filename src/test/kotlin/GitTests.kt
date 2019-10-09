import org.junit.BeforeClass
import kotlin.test.Test

class GitTests {

    @BeforeClass
    fun clean() {
        YarnRepo.clean()
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
}