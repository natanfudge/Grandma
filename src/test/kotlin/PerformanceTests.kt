import grandma.YarnRepo
import grandma.profile
import org.junit.Test

class PerformanceTests {
    @Test
    fun `Walking time`(){
        val result = profile("walking"){
            YarnRepo.walkMappingsDirectory()
        }

        for(f in result){ }
    }


    @Test
    fun `Filtering time`(){
        val result = profile("filtering"){
            val x = "123".toInt()
        }

    }
}