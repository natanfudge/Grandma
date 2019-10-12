import grandma.KeyWord
import grandma.Rename
import grandma.parseRename
import kotlin.test.Test
import kotlin.test.assertEquals

class RenameParseTests {

    private fun assertValid(oldName: String, newName: String): Rename<*> {
        return parseRename(keyWord = KeyWord.Rename, rawOldName = oldName, rawNewName = newName, explanation = null)
            .assertSucceeds()
    }


    @Test
    fun `Class names are parsed correctly`() {
        val expected = className("Old").renamedTo("New")
        val actual = assertValid(oldName = "Old", newName = "New")

        assertEquals(expected, actual)
    }

    @Test
    fun `Disambiguation is parsed correctly`() {
        val expected = className("Old", packageName = "mePackage/foo").renamedTo("New")
        val actual = assertValid(oldName = "mePackage/foo/Old", newName = "New")

        assertEquals(expected, actual)
    }


    @Test
    fun `Package rename is parsed correctly`() {
        val expected = className("Old").renamedTo("New", packageName = "somePackage/foo")
        val actual = assertValid(oldName = "Old", newName = "somePackage/foo/New")

        assertEquals(expected, actual)
    }

    @Test
    fun `Inner class is parsed correctly`() {
        val expected = className("Old") {
            innerClass("Inner")
        }.renamedTo("New")

        val actual = assertValid(oldName = "Old\$Inner", newName = "New")

        assertEquals(expected, actual)

    }

    @Test
    fun `Field is parsed correctly`() {
        val expected = className("Old") {
            field("field")
        }.renamedTo("NewF")

        val actual = assertValid(oldName = "Old%field", newName = "NewF")

        assertEquals(expected, actual)

    }


    @Test
    fun `Method is parsed correctly`() {
        val expected = className("Old") {
            method("method")
        }.renamedTo("NewM")

        val actual = assertValid(oldName = "Old#method", newName = "NewM")

        assertEquals(expected, actual)

    }


    @Test
    fun `Indexed Parameter is parsed correctly`() {
        val expected = className("Old") {
            method("method").parameter(2)
        }.renamedTo("NewP")

        val actual = assertValid(oldName = "Old#method[2]", newName = "NewP")

        assertEquals(expected, actual)

    }

    @Test
    fun `Named Parameter is parsed correctly`() {
        val expected = className("Old") {
            method("method").parameter("paramName")
        }.renamedTo("NewP")

        val actual = assertValid(oldName = "Old#method[paramName]", newName = "NewP")

        assertEquals(expected, actual)
    }

    @Test
    fun `Complex rename is parsed correctly`(){
        val expected = className("Block",packageName = "net/minecraft/blocks"){
            innerClass("InnerBlock").innerClass("Innerer").method("Blockaside")
                .parameter("f2f")
        }.renamedTo("f3f")

        val actual = assertValid(oldName = "net/minecraft/blocks/Block\$InnerBlock\$Innerer#Blockaside[f2f]",
            newName = "f3f")

        assertEquals(expected,actual)
    }
    //TODO: test method parameter type list parsing
}
