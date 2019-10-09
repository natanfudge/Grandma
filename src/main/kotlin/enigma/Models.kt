package enigma


data class MappingsFile(val topLevelClass: ClassMapping) {
    // To be able to extend
    companion object
}

data class ClassMapping(
    override var obfuscatedName: String,
    override var deobfuscatedName: String?,
    var methods: MutableList<MethodMapping>,
    var fields: MutableList<FieldMapping>,
    var innerClasses: MutableList<ClassMapping>
) : Mapping

data class MethodMapping(
    override var obfuscatedName: String, override var deobfuscatedName: String?,
    override var descriptor: String, var parameters: MutableList<ParameterMapping>
) : Mapping, Descriptored

data class FieldMapping(
    override var obfuscatedName: String, override var deobfuscatedName: String,
    override var descriptor: String
) : Mapping, Descriptored

data class ParameterMapping(var index: Int, override var deobfuscatedName: String) : Mapping {
    override val obfuscatedName = ""
}

interface Mapping {
    val obfuscatedName: String
    val deobfuscatedName: String?
}

interface Descriptored {
    val descriptor: String
}