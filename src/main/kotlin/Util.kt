import java.io.File
import java.net.URL

//
fun URL.toFile() = File(file)

fun <T> MutableList<T>.put(index: Int, item: T) {
    if (index < size) set(index, item) else add(index, item)
}
//object Util
//fun getResource(resource : String) : File = YarnR.javaClass.classLoader.getResource(resource)?.toFile()
//    ?: error("Could not get resource $resource")