import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

object YarnRepo {
    private const val RemoteUrl = "https://github.com/natanfudge/yarn"
    val LocalPath = File("yarn")
    private const val MappingsDirName = "mappings"
    val MappingsDirectory: File = LocalPath.toPath().resolve(MappingsDirName).toFile()
    private const val GithubUsername = "natanfudge"
    private val GithubPassword = System.getenv("GITHUB_PASSWORD")


    fun getOrClone(): Git = if (LocalPath.exists()) Git.open(LocalPath) else Git.cloneRepository()
        .setURI(RemoteUrl)
        .setDirectory(LocalPath)
        .setCredentialsProvider(UsernamePasswordCredentialsProvider(GithubUsername, GithubPassword))
        .call()


}