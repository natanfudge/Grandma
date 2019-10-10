import enigma.*
import java.io.File

data class RenameResult(val fullOldName: String, val fullNewName: String)

data class Rename(val originalName: OriginalName, val explanation: String?, val byObfuscated: Boolean) {
//    abstract val explanation: String?
//    abstract val originalName: OriginalName
//    protected abstract val byObfuscated: Boolean


    fun canRename(mappings: MappingsFile): Boolean {
        return originalName.existsIn(mappings, byObfuscated)
    }

    /** Optimization to avoid parsing all of the files */
    fun matchesFileName(nameWithoutExtension: String, directory: String): Boolean {
        return originalName.matchesFileName(nameWithoutExtension, directory)
    }
    //TODO: handle moving the full package seperately

    /**
     * Rename result is mainly for human reading. This is information only gained after the rename, because
     * the information in the Rename itself does not include the full package name.
     */
    fun rename(mappings: MappingsFile) : RenameResult{

    }
//
//    abstract fun rename(mappings: MappingsFile): RenameResult
//
//    data class ByObfuscatedName(
//        override val originalName: OriginalName,
//        val newName: Name<*>,
//        override val explanation: String?
//    ) : Rename() {
//        override val byObfuscated = true
//        override fun rename(mappings: MappingsFile): RenameResult {
//            TODO("not implemented")
//        }
//    }
//
//    data class ByDeobfuscatedName(
//        override val originalName: OriginalName,
//        val newName: Name<*>,
//        override val explanation: String?
//    ) : Rename() {
//        override val byObfuscated = false
//        override fun rename(mappings: MappingsFile): RenameResult {
//            newName.
//        }
//    }
}


/**
 *  Code duplication here because using the same class for both makes it confusing during renaming - the subclasses exist
 *  for completely different reason.
 *  In the OriginalName you can qualify to resolve ambiguities, and in NewName you can qualify to change the package.
 */
sealed class OriginalName {
    abstract fun matchesFileName(fileName: String, fileDirectory: String): Boolean
    abstract fun existsIn(mappings: MappingsFile, byObfuscated: Boolean): Boolean

    data class Short(val name: Name<*>) : OriginalName() {
        override fun matchesFileName(fileName: String, fileDirectory: String): Boolean {
            return name.topLevelClassName == fileName
        }

        override fun existsIn(mappings: MappingsFile, byObfuscated: Boolean): Boolean {
            return name.findIn(mappings, byObfuscated) != null
        }
    }

    data class Qualified(val unqualifiedName: Short, val partialPackage: String) : OriginalName() {
        override fun matchesFileName(fileName: String, fileDirectory: String): Boolean {
            return unqualifiedName.matchesFileName(fileName, fileDirectory) && fileDirectory.endsWith(partialPackage)
        }

        override fun existsIn(mappings: MappingsFile, byObfuscated: Boolean): Boolean {
            return unqualifiedName.existsIn(mappings, byObfuscated)
        }

    }
}



//sealed class NewName {
//    abstract val name: Name<*>
//    abstract fun rename(mappings: MappingsFile)  : RenameResult
//
//    data class ClassNameChange(override val name: Name<*>) : NewName() {
//        override fun rename(mappings: MappingsFile): RenameResult {
//            TODO("not implemented")
//        }
//    }
//
//    data class CompleteChange(override val name: Name<*>, val fullPackage: String) : NewName()
//}
//TODO: remove all of this overengineering and overabstraction
sealed class Name<M : Mapping> {
    abstract val topLevelClassName: String
    abstract fun findIn(mappings: MappingsFile, byObfuscated: Boolean): M?

    fun rename(mappings: MappingsFile) : RenameResult{

    }


}


data class ClassName(val topLevelName: String, val innerClass: ClassName?) : Name<ClassMapping>() {
    override val topLevelClassName = topLevelName
    override fun findIn(mappings: MappingsFile, byObfuscated: Boolean) = findIn(mappings.topLevelClass, byObfuscated)

    private fun findIn(mappings: ClassMapping, byObfuscated: Boolean): ClassMapping? {
        val targetName = (if (byObfuscated) mappings.obfuscatedName else mappings.deobfuscatedName) ?: return null
        if (targetName.split("/").last() != topLevelName) return null
        if (innerClass != null) {
            for (innerClassMapping in mappings.innerClasses) {
                val found = innerClass.findIn(innerClassMapping, byObfuscated)
                if (found != null) return found
            }
            return null
        } else {
            return mappings
        }
    }
}

data class FieldName(val fieldName: String, val classIn: ClassName) : Name<FieldMapping>() {
    override val topLevelClassName = classIn.topLevelClassName
    override fun findIn(mappings: MappingsFile, byObfuscated: Boolean): FieldMapping? {
        TODO("not implemented")
    }
}

data class MethodName(val methodName: String, val classIn: ClassName) : Name<MethodMapping>() {
    override val topLevelClassName = classIn.topLevelClassName
    override fun findIn(mappings: MappingsFile, byObfuscated: Boolean): MethodMapping? {
        TODO("not implemented")
    }
}

sealed class ParameterName(methodIn: MethodName) : Name<ParameterMapping>() {
    override val topLevelClassName = methodIn.topLevelClassName

    data class Index(val index: Int, val method: MethodName) : ParameterName(method) {
        override fun findIn(mappings: MappingsFile, byObfuscated: Boolean): ParameterMapping? {
            TODO("not implemented")
        }
    }

    data class Name(val name: String, val method: MethodName) : ParameterName(method) {
        override fun findIn(mappings: MappingsFile, byObfuscated: Boolean): ParameterMapping? {
            TODO("not implemented")
        }
    }
}
