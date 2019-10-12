package grandma.enigma

import grandma.Joiner
import java.lang.reflect.Parameter



data class MappingsFile(val topLevelClass: ClassMapping) {
    // To be able to extend
    companion object
}

data class ClassMapping(
    override var obfuscatedName: String,
    override var deobfuscatedName: String?,
    var methods: MutableList<MethodMapping>,
    var fields: MutableList<FieldMapping>,
    var innerClasses: MutableList<ClassMapping>,
    /*override*/ val parent: ClassMapping?
) : Mapping() {
    override fun humanReadableName(obfuscated: Boolean): String {
        val humanReadableName = name(obfuscated) ?: "<un-named>"
        return if (parent == null) humanReadableName else parent.humanReadableName(obfuscated) +
                Joiner.InnerClass + humanReadableName
    }
    override fun toString() = humanReadableName(false)

    override val root : ClassMapping = parent?.root ?: this
}

//TODO: this needs to also include a parsed version of the descriptor, like Class#method(int,bool,MyClass)
data class MethodMapping(
    override var obfuscatedName: String,
    override var deobfuscatedName: String?,
    override var descriptor: String,
    var parameters: MutableList<ParameterMapping>,
    val parent: ClassMapping
) : Mapping(), Descriptored {
    override fun humanReadableName(obfuscated: Boolean) = parent.humanReadableName(obfuscated) +
            Joiner.Method + name(obfuscated)
    override fun toString() = humanReadableName(false)
    override val root = parent.root
}

data class FieldMapping(
    override var obfuscatedName: String, override var deobfuscatedName: String,
    override var descriptor: String,  val parent: ClassMapping
) : Mapping(), Descriptored {
    override fun humanReadableName(obfuscated: Boolean) = parent.humanReadableName(obfuscated) +
            Joiner.Field + name(obfuscated)
    override fun toString() = humanReadableName(false)
    override val root = parent.root
}

data class ParameterMapping(
    var index: Int, override var deobfuscatedName: String,
     val parent: MethodMapping
) : Mapping() {
    override val obfuscatedName = ""
    override fun humanReadableName(obfuscated: Boolean) = parent.humanReadableName(obfuscated) +
            "[param $index = ${name(obfuscated)}]"
    override fun toString() = humanReadableName(false)
    override val root = parent.root
}

sealed class Mapping {
    abstract val obfuscatedName: String
    abstract val deobfuscatedName: String?

    abstract fun humanReadableName(obfuscated: Boolean): String
    abstract val root : ClassMapping

    fun name(obfuscated: Boolean) = if (obfuscated) obfuscatedName else deobfuscatedName
}

interface Descriptored {
    val descriptor: String
}