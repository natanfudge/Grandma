import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.util.EnhancedEventListener
import com.jessecorbett.diskord.util.authorId
import enigma.MappingsFile
import enigma.read
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eclipse.jgit.api.Git
import java.io.File

data class MessageContext(val message: Message, private val eventListener: EnhancedEventListener) {
    fun reply(content: String) {
        GlobalScope.launch {
            with(eventListener) {
                message.reply(content)
            }
        }
    }

    val branchNameOfSender = message.authorId
}

enum class KeyWord {
    Rename,
    Name
}


//TODO: test if indeed reply happens async
// For testing mainly
object UserInput {
    object Errors {
        const val IncorrectRename = "Incorrect syntax. Use: rename <old-name> to <new-name> [because <explanation>]"
        const val IncorrectName = "Incorrect syntax. Use: name <obfuscated-name> <new-name>[: <explanation>]"
    }

    fun checkForError(keyWord: KeyWord, sentence: List<String>): String? {
        when (keyWord) {
            //TODO: x=>y: syntax
            KeyWord.Rename -> {
                if (sentence.size < 3 || sentence[1] != "to" || sentence.size == 4 ||
                    (sentence.size == 5 && sentence[3] != "because")
                ) {
                    return Errors.IncorrectRename
                }
            }
            KeyWord.Name -> {
                if (sentence.size < 2 || (sentence[1].endsWith(":") && sentence.size < 3)
                    || (!sentence[1].endsWith(":") && sentence.size >= 3)
                ) {
                    return Errors.IncorrectName
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
        is RenameParseResult.Success -> tryRename(result.parsed,oldName)
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
            originalName = OriginalName.Short(ClassName(oldName, innerClass = null)),
            explanation = explanation,
            newName = NewName.ClassNameChange(ClassName(newName, innerClass = null))
        )
    )
}


//private fun Rename.matchesFileName(nameWithoutExt: String): Boolean {
//    return oldTopLevelClassName == nameWithoutExt
//}

//private val Rename.oldTopLevelClassName get() = originalName.name.topLevelClassName


private fun MessageContext.tryRename(rename: Rename, oldNameInputString : String) {
    val repo = YarnRepo.getGit()
    println("Switching to branch $branchNameOfSender")
    repo.switchToBranch(branchNameOfSender)

    val matchingMappingsFiles = YarnRepo.walkMappingsDirectory()
        .filter { rename.matchesFileName(it.name.removeSuffix(".mapping"),it.parent)
                && rename.canRename(MappingsFile.read(it)) }
        .toList()

    when {
        matchingMappingsFiles.isEmpty() -> {
            return reply(
                if (rename is Rename.ByDeobfuscatedName) "No class named '$oldNameInputString'"
                else "No intermediary class name '$oldNameInputString' or the class has already been named"
            )
        }
        matchingMappingsFiles.size > 1 -> {
            val options = matchingMappingsFiles.joinToString("\n") { YarnRepo.mappingsPathOf(it).absolutePath }
            return reply(
                "There are multiple classes with this name: \n$options\n" +
                        "Prefix the **original** class name with its enclosing package name followed by a '/'."
            )
        }
        else -> {
            rename(rename, matchingMappingsFiles[0], repo)
        }
    }

}

private fun MessageContext.rename(rename: Rename, mappingsFile: File, repo: Git) {

}