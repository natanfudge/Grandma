import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.util.EnhancedEventListener
import com.jessecorbett.diskord.util.authorId
import enigma.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.eclipse.jgit.api.Git

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
    val sentence = message.split(" ").toMutableList()
    // Remove the command prefix
    sentence.removeAt(0)

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
        is RenameParseResult.Success -> tryRename(result.parsed, oldName)
        is RenameParseResult.Error -> result.error
    }

}

sealed class RenameParseResult {
    data class Success(val parsed: Rename<*>) : RenameParseResult()
    data class Error(val error: String) : RenameParseResult()
}


private fun parseRename(
    keyWord: KeyWord,
    oldName: String,
    newName: String,
    explanation: String?
): RenameParseResult {
    return RenameParseResult.Success(
        Rename(
            originalName = OriginalName.Short(ClassName(oldName, innerClass = null)),
            explanation = explanation,
            newName = ClassName(newName, innerClass = null),
            byObfuscated = false
        )
    )
}

fun Mapping.type(): String = when (this) {
    is ClassMapping -> "class"
    is MethodMapping -> "method"
    is FieldMapping -> "field"
    is ParameterMapping -> "parameter"
}

fun Mapping.typePlural(): String = when (this) {
    is ClassMapping -> "classes"
    is MethodMapping -> "methods"
    is FieldMapping -> "fields"
    is ParameterMapping -> "parameters"
}

private fun <M : Mapping> MessageContext.tryRename(rename: Rename<M>, oldNameInputString: String) {
    val repo = YarnRepo.getGit()
    println("Switching to branch $branchNameOfSender")
    repo.switchToBranch(branchNameOfSender)

    val matchingMappingsFiles = YarnRepo.walkMappingsDirectory()
        .mapNotNull { rename.findRenameTarget(it) }
        .toList()

    when {
        matchingMappingsFiles.isEmpty() -> {
            return reply(
                if (rename.byObfuscated) "No class named '$oldNameInputString'"
                else "No intermediary class name '$oldNameInputString' or the class has already been named"
            )
        }
        matchingMappingsFiles.size > 1 -> {
            val typePlural = matchingMappingsFiles[0].typePlural()
            val type = matchingMappingsFiles[0].type()
            val options = matchingMappingsFiles.joinToString("\n") { it.humanReadableName(rename.byObfuscated) }
            return reply(
                "There are multiple $typePlural with this name: \n$options\n" +
                        "Prefix the **original** $type name with its enclosing package name followed by a '/'."
            )
        }
        else -> {
            rename(rename, matchingMappingsFiles[0], repo)
        }
    }

}
val Mapping.filePath get() = root.deobfuscatedName ?: root.obfuscatedName

private fun <M : Mapping> MessageContext.rename(rename: Rename<M>, renameTarget: M, repo: Git) {
    val oldPath = renameTarget.filePath
    val oldName = renameTarget.humanReadableName(rename.byObfuscated)
    rename.rename(renameTarget)
    val newPath = renameTarget.filePath
    val newName = renameTarget.humanReadableName(rename.byObfuscated)

    if(oldPath != newPath){
        repo.remove(YarnRepo.pathOfMappingFromGitRoot(oldPath))
    }

    reply("Renamed '$oldName' to '$newName'!")

    MappingsFile(renameTarget.root).writeTo(YarnRepo.getMappingsFile(newPath))
    repo.stageChanges(newPath)
    repo.commit(author = YarnRepo.TemporaryAuthor,commitMessage = "$oldName -> $newName")
    YarnRepo.push(repo)

}