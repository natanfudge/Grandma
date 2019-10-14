package util

import grandma.GitRepository
import grandma.MessageContext
import grandma.YarnRepo
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.transport.CredentialsProvider

class TestMessageContext : MessageContext {
    private val replies = mutableListOf<String>()
    override fun reply(content: String) {
        replies.add(content)
    }

    override val branchNameOfSender = "TestBranch"
    override val repo by lazy { TestGitRepository(YarnRepo.getRawGit()) }
}

class TestGitRepository(git: Git) : GitRepository(git) {
    override fun commit(author: PersonIdent, commitMessage: String) {}
    override fun actuallyPush(remoteUrl: String, credentialsProvider: CredentialsProvider) {}
}