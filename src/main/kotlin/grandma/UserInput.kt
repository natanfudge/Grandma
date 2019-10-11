package grandma

import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.util.EnhancedEventListener
import com.jessecorbett.diskord.util.authorId
import grandma.enigma.*
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
        is StringSuccess -> tryRename(result.value, oldName)
        is StringError -> result.value
    }

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
    profile("Switching to branch $branchNameOfSender") {
        repo.switchToBranch(branchNameOfSender)
    }


    val matchingMappingsFiles = YarnRepo.walkMappingsDirectory()
        .mapNotNull { rename.findRenameTarget(it) }
        .toList()

    when {
        matchingMappingsFiles.isEmpty() -> {
            return profile("replied") {
                reply(
                    if (rename.byObfuscated) "No intermediary class name '$oldNameInputString' or the class has already been named."
                    else "No class named '$oldNameInputString'."
                )
            }
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

val Mapping.filePath get() = (root.deobfuscatedName ?: root.obfuscatedName) + ".mapping"

private fun <M : Mapping> MessageContext.rename(rename: Rename<M>, renameTarget: M, repo: Git) {
    val oldPath = renameTarget.filePath
    val oldName = renameTarget.humanReadableName(rename.byObfuscated)
    rename.rename(renameTarget)
    val newPath = renameTarget.filePath
    val newName = renameTarget.humanReadableName(rename.byObfuscated)

    reply("Renamed $oldName to $newName")

    if (oldPath != newPath) {
        repo.remove("mappings/net/minecraft/block/BambooSaplingBlock.mapping")
        repo.remove(YarnRepo.pathOfMappingFromGitRoot(oldPath))
    }

    MappingsFile(renameTarget.root).writeTo(YarnRepo.getMappingsFile(newPath))
    repo.stageChanges(YarnRepo.pathOfMappingFromGitRoot(newPath))
    repo.commit(author = YarnRepo.TemporaryAuthor, commitMessage = "$oldName -> $newName")
    YarnRepo.push(repo)
    println("Changes pushed successfully!")


}