import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

object YarnRepo {
    private const val RemoteUrl = "https://github.com/natanfudge/yarn"
    private val LocalPath = File("yarn")
    private const val MappingsDirName = "mappings"
    val MappingsDirectory: File = getFile(MappingsDirName)
    private const val GithubUsername = "natanfudge"
    private val GithubPassword = System.getenv("GITHUB_PASSWORD")

    fun clean() = LocalPath.deleteRecursively()


    fun getOrClone(): Git = if (LocalPath.exists()) getGit() else Git.cloneRepository()
        .setURI(RemoteUrl)
        .setDirectory(LocalPath)
        .setCredentialsProvider(UsernamePasswordCredentialsProvider(GithubUsername, GithubPassword))
        .call()

    fun getGit(): Git = Git.open(LocalPath)

    fun getFile(path: String): File = LocalPath.toPath().resolve(path).toFile()

    fun walkMappingsDirectory(): FileTreeWalk = MappingsDirectory.walk()

    fun mappingsPathOf(path : File) = path.relativeTo(MappingsDirectory)
}

