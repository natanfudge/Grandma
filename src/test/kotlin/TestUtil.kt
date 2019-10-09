import java.io.File

object TestUtil {
    fun getResource(path: String): File = File("src/test/resources/$path")
}