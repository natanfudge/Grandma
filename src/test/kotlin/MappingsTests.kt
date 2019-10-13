import grandma.descriptor.FieldType
import grandma.descriptor.ObjectType
import grandma.descriptor.ReturnDescriptor
import grandma.mappings.*
import org.junit.Test
import util.TestUtil
import util.mappingsFile
import kotlin.test.assertEquals

class MappingsTests {

    private val testMappings = mappingsFile("bgq","net/minecraft/block/AbstractBannerBlock"){
        innerClass("testObf","testDeobf"){
            field("a","b",FieldType.Byte)
            method("d","e"){
                param(1,"g")
            }
        }
        innerClass("a","b"){
            method("a","b")
        }
        innerClass("testMissing")
        field("a","color",ObjectType("awa"))
        method("b","getColor",returnType = ObjectType("awa"))
        method("obf")

    }


    @Test
    fun `Enigma files are parsed correctly`() {
        val parsed = MappingsFile.read(TestUtil.getResource("AbstractBannerBlock.mapping"))
        assertEquals(expected = testMappings, actual = parsed)
    }

    @Test
    fun `Enigma files are written correctly`() {
        val expected = TestUtil.getResource("AbstractBannerBlock.mapping").readText()
        val actual = createTempFile().let {
            testMappings.writeTo(it)
            it.readText()
        }

        assertEquals(expected, actual)
    }

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