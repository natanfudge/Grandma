package grandma

import kotlin.system.measureTimeMillis


fun <T> MutableList<T>.put(index: Int, item: T) {
    if (index < size) set(index, item) else add(index, item)
}

const val Profile = true
inline fun <T> profile(sectionName: String, code: () -> T): T {
    if (Profile) {
        var result: T? = null
        val time = measureTimeMillis {
            result = code()
        }
        println("$sectionName in $time millis")
        return result!!
    } else return code()
}

fun String.splitOn(index: Int) = Pair(substring(0, index), substring(index + 1))