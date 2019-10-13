package util

import grandma.Errorable
import grandma.StringError
import grandma.StringSuccess
import java.io.File

object TestUtil {
    fun getResource(path: String): File = File("src/test/resources/$path")
}

inline fun <reified T> Errorable<T>.assertSucceeds(): T = when (this) {
    is StringSuccess -> this.value
    is StringError -> throw AssertionError("Expected success, but instead an error occurred: '${this.value}'")
}