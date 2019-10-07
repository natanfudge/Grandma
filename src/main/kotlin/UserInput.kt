import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.util.EnhancedEventListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class MessageContext(val message: Message, private val eventListener: EnhancedEventListener) {
    fun reply(content: String) {
        GlobalScope.launch {
            with(eventListener) {
                message.reply(content)
            }
        }
    }
}

//TODO: test if indeed reply happens async
// For testing mainly
object UserInput {
    fun checkForError(keyWord: KeyWord, sentence: List<String>): String? {
        when (keyWord) {
            //TODO: x=>y: syntax
            KeyWord.Rename -> {
                if (sentence.size < 3 || sentence[1] != "to" || sentence.size == 4 ||
                    (sentence.size == 5 && sentence[3] != "because")
                ) {
                    return "Incorrect syntax. Use: rename <old-name> to <new-name> [because <explanation>]"
                }
            }
            KeyWord.Name -> {
                if (sentence.size < 2 || (sentence[1].endsWith(":") && sentence.size < 3)) {
                    return "Incorrect syntax. Use: name <obfuscated-name> <new-name>[: <explanation>]"
                }
            }
        }

        return null
    }
}


fun MessageContext.acceptRaw(keyWord: KeyWord, message: String) {
    val sentence = message.split(" ")
    UserInput.checkForError(keyWord, sentence)?.let { return reply(it) }

    val oldName = sentence[0]
    val newName = when (keyWord) {
        KeyWord.Name -> sentence[1].removeSuffix(":")
        KeyWord.Rename -> sentence[2]
    }

    val explanation = when (keyWord) {
        KeyWord.Rename -> {
            if (sentence.size < 5) null
            else sentence[4]
        }
        KeyWord.Name -> {
            if (sentence.size < 3) null
            else sentence[2]
        }
    }

    when (val result = parseRename(keyWord, oldName, newName, explanation)) {
        is RenameParseResult.Success -> rename(result.parsed)
        is RenameParseResult.Error -> result.error
    }

}

sealed class RenameParseResult {
    data class Success(val parsed: Rename) : RenameParseResult()
    data class Error(val error: String) : RenameParseResult()
}


private fun parseRename(
    keyWord: KeyWord,
    oldName: String,
    newName: String,
    explanation: String?
): RenameParseResult {
    return RenameParseResult.Success(
        Rename.ByDeobfuscatedName(
            deobfuscatedName = OriginalName.Short(ClassName(oldName, innerClass = null)),
            explanation = explanation,
            newName = NewName.ClassNameChange(ClassName(newName, innerClass = null))
        )
    )
}


private fun rename(rename: Rename) {

}

enum class KeyWord {
    Rename,
    Name
}

