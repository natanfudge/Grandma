import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.nio.file.Paths

object YarnRepo {
    private const val RemoteUrl = "https://github.com/natanfudge/yarn"
    private val LocalPath = File("yarn")
    private const val MappingsDirName = "mappings"
    val MappingsDirectory: File = getFile(MappingsDirName)
    private const val GithubUsername = "natanfudge"
    private val GithubPassword = System.getenv("GITHUB_PASSWORD")
    //TODO(version 2) let each discord user assign a github account
    val TemporaryAuthor = PersonIdent("natanfudge","natan.lifsiz@gmail.com")

    fun clean() = LocalPath.deleteRecursively()


    fun getOrClone(): Git = if (LocalPath.exists()) getGit() else Git.cloneRepository()
        .setURI(RemoteUrl)
        .setDirectory(LocalPath)
        .call()

    fun getGit(): Git = Git.open(LocalPath)

    fun getFile(path: String): File = LocalPath.toPath().resolve(path).toFile()

    fun walkMappingsDirectory(): FileTreeWalk = MappingsDirectory.walk()


    fun pathOfMappingFromGitRoot(relativeMappingPath: String): String {
        return Paths.get(MappingsDirName, relativeMappingPath).toString()
    }

    fun getMappingsFile(path: String): File = MappingsDirectory.toPath().resolve(path).toFile()

    fun push(repo : Git) = repo.actuallyPush(RemoteUrl,UsernamePasswordCredentialsProvider(GithubUsername, GithubPassword))
}

