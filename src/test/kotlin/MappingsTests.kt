import enigma.*
import org.junit.Test
import kotlin.test.assertEquals

private fun <T> ml(vararg args: T): MutableList<T> = mutableListOf(*args)
class MappingsTests {

//    private val testMappings = MappingsFile(
//        ClassMapping(
//            obfuscatedName = "bgq",
//            deobfuscatedName = "net/minecraft/block/AbstractBannerBlock",
//            innerClasses = ml(
//                ClassMapping(
//                    obfuscatedName = "testObf",
//                    deobfuscatedName = "testDeobf",
//                    innerClasses = ml(),
//                    fields = ml(
//                        FieldMapping(
//                            obfuscatedName = "a",
//                            deobfuscatedName = "b",
//                            descriptor = "c"
//                        )
//                    ),
//                    methods = ml(
//                        MethodMapping(
//                            obfuscatedName = "d",
//                            deobfuscatedName = "e",
//                            descriptor = "f",
//                            parameters = ml(
//                                ParameterMapping(1, "g")
//                            )
//                        )
//                    )
//                ),
//                ClassMapping(
//                    "a", "b", ml(
//                        MethodMapping("a", "b", "c", ml())
//                    ), ml(), ml()
//                ),
//                ClassMapping("testMissing", null, ml(), ml(), ml())
//            ),
//            fields = ml(
//                FieldMapping(
//                    obfuscatedName = "a",
//                    deobfuscatedName = "color",
//                    descriptor = "Lawa;"
//                )
//            ),
//            methods = ml(
//                MethodMapping(
//                    obfuscatedName = "b",
//                    deobfuscatedName = "getColor",
//                    descriptor = "()Lawa;",
//                    parameters = ml()
//                ),
//                MethodMapping("obf", null, "descriptor", ml())
//            )
//        )
//    )

//    @Test
//    fun `Enigma files are parsed correctly`() {
//        val parsed = MappingsFile.read(TestUtil.getResource("AbstractBannerBlock.mapping"))
//        assertEquals(expected = testMappings, actual = parsed)
//    }
//
//    @Test
//    fun `Enigma files are written correctly`() {
//        val expected = TestUtil.getResource("AbstractBannerBlock.mapping").readText()
//        val actual = createTempFile().let {
//            testMappings.writeTo(it)
//            it.readText()
//        }
//
//        assertEquals(expected, actual)
//    }

    @Test
    fun `Reading and writing a mappings file results in the same thing`() {
        val originalFile = TestUtil.getResource("Block.mapping")
        val actual = createTempFile().let {
            MappingsFile.read(originalFile).writeTo(it)
            it.readText()
        }
        assertEquals(originalFile.readText(), actual)
    }
}