import kotlin.test.Test

class Tests {
    @Test
    fun `Can clone the yarn repository`(){
        YarnRepo.clean()
        YarnRepo.getOrClone()
        assert(YarnRepo.MappingsDirectory.exists())
    }

    @Test
    fun `Branches retain information`(){
        YarnRepo.clean()
        YarnRepo.getOrClone().switchToBranch("secretInfo")
        assert(YarnRepo.getFile("secretTestInfo").exists())
    }
}