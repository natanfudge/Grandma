package grandma

import com.jessecorbett.diskord.api.model.Message
import com.jessecorbett.diskord.util.EnhancedEventListener
import com.jessecorbett.diskord.util.authorId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface MessageContext {
    fun reply(content: String)
    val repo: GitRepository
    val branchNameOfSender: String
}

data class DiscordMessageContext(val message: Message, private val eventListener: EnhancedEventListener) :
    MessageContext {
    override fun reply(content: String) {
        GlobalScope.launch {
            with(eventListener) {
                message.reply(content)
            }
        }
    }

    override val repo by lazy { GitRepository(YarnRepo.getRawGit()) }
    override val branchNameOfSender = message.authorId
}