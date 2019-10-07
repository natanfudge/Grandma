sealed class Rename {
    abstract val explanation: String?

    data class ByObfuscatedName(
        val obfuscatedName: OriginalName,
        val newName: NewName.CompleteChange,
        override val explanation: String?
    ) : Rename()

    data class ByDeobfuscatedName(
        val deobfuscatedName: OriginalName,
        val newName: NewName,
        override val explanation: String?
    ) : Rename()
}


/**
 *  Code duplication here because using the same class for both makes it confusing during renaming - the subclasses exist
 *  for completely different reason.
 *  In the OriginalName you can qualify to resolve ambiguities, and in NewName you can qualify to change the package.
 */
sealed class OriginalName {
    abstract val name: Name

    data class Short(override val name: Name) : OriginalName()
    data class Qualified(override val name: Name, val partialPackage: String) : OriginalName()
}
sealed class NewName {
    abstract val name: Name
    data class ClassNameChange(override val name: Name) : NewName()
    data class CompleteChange(override val name: Name, val fullPackage: String) : NewName()
}

sealed class Name
data class ClassName(val topLevelName: String, val innerClass: ClassName?) : Name()
data class FieldName(val fieldName: String, val classIn: ClassName) : Name()
data class MethodName(val methodName: String, val classIn: ClassName) : Name()
sealed class ParameterName : Name() {
    data class Index(val index: Int) : ParameterName()
    data class Name(val name: String) : ParameterName()
}
