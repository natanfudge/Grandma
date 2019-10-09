import cuchaz.enigma.translation.mapping.EntryMapping
import cuchaz.enigma.translation.mapping.tree.EntryTree

sealed class Rename {
    abstract val explanation: String?
    abstract val originalName: OriginalName

//    fun canRename(mappings: EntryTree<EntryMapping>) {
//        return originalName
//    }

    data class ByObfuscatedName(
        override val originalName: OriginalName,
        val newName: NewName.CompleteChange,
        override val explanation: String?
    ) : Rename()

    data class ByDeobfuscatedName(
        override val originalName: OriginalName,
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

    fun find(){
        
    }

    data class Short(override val name: Name) : OriginalName()
    data class Qualified(override val name: Name, val partialPackage: String) : OriginalName()
}

sealed class NewName {
    abstract val name: Name

    data class ClassNameChange(override val name: Name) : NewName()
    data class CompleteChange(override val name: Name, val fullPackage: String) : NewName()
}

sealed class Name {
    abstract val topLevelClassName: String
}

data class ClassName(val topLevelName: String, val innerClass: ClassName?) : Name() {
    override val topLevelClassName = topLevelName
}

data class FieldName(val fieldName: String, val classIn: ClassName) : Name() {
    override val topLevelClassName = classIn.topLevelClassName
}

data class MethodName(val methodName: String, val classIn: ClassName) : Name() {
    override val topLevelClassName = classIn.topLevelClassName
}

sealed class ParameterName(methodIn: MethodName) : Name() {
    override val topLevelClassName = methodIn.topLevelClassName

    data class Index(val index: Int, val method: MethodName) : ParameterName(method)
    data class Name(val name: String, val method: MethodName) : ParameterName(method)
}
