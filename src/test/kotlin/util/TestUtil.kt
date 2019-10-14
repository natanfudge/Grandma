package util

import grandma.*
import java.io.File

object TestUtil {
    fun getResource(path: String): File = File("src/test/resources/$path")
}

inline fun <reified T> Errorable<T>.assertSucceeds(): T = when (this) {
    is StringSuccess -> this.value
    is StringError -> throw AssertionError("Expected success, but instead an error occurred: '${this.value}'")
}

fun getOrCloneGit() : GitRepository {
    YarnRepo.cloneIfMissing()
    return GitRepository(YarnRepo.getRawGit())
}