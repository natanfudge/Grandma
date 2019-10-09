import kotlin.test.Test
import kotlin.test.assertEquals

class InputTests {
    @Test
    fun `Incorrect rename input is met with an error`() {
        val incorrectRenames = listOf(
            "x", "x y z", "x to y z", "x to y z a"
        )
        for (rename in incorrectRenames) {
            assertEquals(UserInput.checkForError(KeyWord.Rename, rename.split(" ")), UserInput.Errors.IncorrectRename)
        }
    }

    @Test
    fun `Incorrect name input is met with an error`() {
        val incorrectNames = listOf(
            "x", "x y:", "x y z"
        )
        for (name in incorrectNames) {
            assertEquals(UserInput.checkForError(KeyWord.Name, name.split(" ")), UserInput.Errors.IncorrectName)
        }
    }

}