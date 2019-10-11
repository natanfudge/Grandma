package grandma

import grandma.enigma.*
import java.io.File

data class RenameResult(val fullOldName: String, val fullNewName: String)


data class Rename<M : Mapping>(
    private val originalName: OriginalName<M>,
    private val newName: Name<M>,
    private val newPackageName: String?,
    private val explanation: String?,
    val byObfuscated: Boolean
) {
//    fun canRename(mappings: MappingsFile): Boolean {
//        return originalName.existsIn(mappings, byObfuscated)
//    }

    fun findRenameTarget(file: File): M? {
        if (!originalName.matchesFileName(file)) return null
        return originalName.findRenameTarget(MappingsFile.read(file), byObfuscated)
    }

//    /** Optimization to avoid parsing all of the files */
//    fun matchesFileName(nameWithoutExtension: String, directory: String): Boolean {
//        return originalName.matchesFileName(nameWithoutExtension, directory)
//    }


    fun rename(mappings: M) = newName.rename(mappings)
    fun renameAndChangePackage(mappings: ClassMapping) {
        TODO()
    }

}


/**
 *  Code duplication here because using the same class for both makes it confusing during renaming - the subclasses exist
 *  for completely different reason.
 *  In the grandma.OriginalName you can qualify to resolve ambiguities, and in NewName you can qualify to change the package.
 */
sealed class OriginalName<M : Mapping> {
    abstract fun matchesFileName(file: File): Boolean
    abstract fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): M?

    data class Short<M : Mapping>(val name: Name<M>) : OriginalName<M>() {
        override fun matchesFileName(file: File): Boolean {
            return name.topLevelClassName == file.nameWithoutExtension
        }

        override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): M? {
            return name.findRenameTarget(mappings, byObfuscated)
        }
    }

    data class Qualified<M : Mapping>(val unqualifiedName: Short<M>, val partialPackage: String) : OriginalName<M>() {
        override fun matchesFileName(file: File): Boolean {
            return unqualifiedName.matchesFileName(file) && file.parent.endsWith(partialPackage)
        }

        override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): M? {
            return unqualifiedName.findRenameTarget(mappings, byObfuscated)
        }

    }
}


sealed class Name<M : Mapping> {
    abstract val topLevelClassName: String
    abstract fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): M?

    abstract fun rename(mappings: M)
}


data class ClassName(val topLevelName: String, val innerClass: ClassName?) : Name<ClassMapping>() {
    override val topLevelClassName = topLevelName
    override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): ClassMapping? {
        return findRenameTarget(mappings.topLevelClass, byObfuscated)
    }

    private fun findRenameTarget(mappings: ClassMapping, byObfuscated: Boolean): ClassMapping? {
        val targetName = (if (byObfuscated) mappings.obfuscatedName else mappings.deobfuscatedName) ?: return null
        if (targetName.split("/").last() != topLevelName) return null
        if (innerClass != null) {
            for (innerClassMapping in mappings.innerClasses) {
                val found = innerClass.findRenameTarget(innerClassMapping, byObfuscated)
                if (found != null) return found
            }
            return null
        } else {
            return mappings
        }
    }

    //TODO: hook up
    fun renameAndChangePackage(mappings: ClassMapping, newPackageName: String) {
        mappings.deobfuscatedName = "$newPackageName/$topLevelName"
    }


    override fun rename(mappings: ClassMapping) {
        val packageName = (mappings.deobfuscatedName ?: mappings.obfuscatedName).split("/")
            .let { it.subList(0, it.size - 1).joinToString("/") }

        mappings.deobfuscatedName = "$packageName/$topLevelName"
    }
}


data class FieldName(val fieldName: String, val classIn: ClassName) : Name<FieldMapping>() {
    override val topLevelClassName = classIn.topLevelClassName
    override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): FieldMapping? {
        TODO("not implemented")
    }

    override fun rename(mappings: FieldMapping) {
        TODO("not implemented")
    }


}

data class MethodName(val methodName: String, val classIn: ClassName, val parameterTypes: List<String>?) :
    Name<MethodMapping>() {
    override val topLevelClassName = classIn.topLevelClassName
    override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): MethodMapping? {
        TODO("not implemented")
    }

    override fun rename(mappings: MethodMapping) {
        TODO("not implemented")
    }


}

sealed class ParameterName(methodIn: MethodName) : Name<ParameterMapping>() {
    override val topLevelClassName = methodIn.topLevelClassName
    override fun rename(mappings: ParameterMapping) {
        TODO("not implemented")
    }

    data class ByIndex(val index: Int, val method: MethodName) : ParameterName(method) {
        override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): ParameterMapping? {
            TODO("not implemented")
        }
    }

    data class ByName(val name: String, val method: MethodName) : ParameterName(method) {
        override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): ParameterMapping? {
            TODO("not implemented")
        }
    }
}
