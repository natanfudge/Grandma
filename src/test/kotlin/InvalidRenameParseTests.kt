import grandma.KeyWord
import grandma.StringError
import grandma.parseRename
import kotlin.test.Test

class InvalidRenameParseTests {

    private fun assertInvalid(oldName: String, newName: String) {
        val result =parseRename(keyWord = KeyWord.Rename, rawOldName = oldName, rawNewName = newName, explanation = null)
        assert(result    is StringError)
        result as StringError
        println("Error = ${result.value}")
    }


    @Test
    fun `Invalid new package name`() = assertInvalid("c","][]/New")
    @Test
    fun `Invalid new class name`() = assertInvalid("Old","/")
    @Test
    fun `Not method before parameter`() = assertInvalid("Old%method[2]","New")

    //TODO: test method parameter type list parsing
}
