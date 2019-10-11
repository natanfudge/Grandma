package grandma

import grandma.enigma.*
import java.io.File


data class Rename<M : Mapping>(
    private val originalName: OriginalName<M>,
    private val newName: Name<M>,
    private val newPackageName: String?,
    private val explanation: String?,
    val byObfuscated: Boolean
) {
    fun findRenameTarget(file: File): M? {
        if (!originalName.matchesFileName(file)) return null
        return originalName.findRenameTarget(MappingsFile.read(file), byObfuscated)
    }

    fun rename(mappings: M) {
        if (newPackageName != null) {
            // Changing the package can only be done on top-level classes
            newName as ClassName
            mappings as ClassMapping
            newName.renameAndChangePackage(mappings, newPackageName)
        } else {
            newName.rename(mappings)
        }

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
            // Subtle difference between 'endsWith(partialPackage)' and  == 'file.nameWithoutExtension',
            // the former will allow not matching entirely (only the end),
            // and the latter will force the class name to completely match.
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

    abstract fun rename(mapping: M)
}

//TODO: test list:
// Disambiguation by specifying package
// Full package rename of class
// Rename of class
// Rename of inner class
// Rename of field
// Rename of method
// Rename of parameter

//TODO: maybe change to classIn for consistency?
data class ClassName(val topLevelName: String, val innerClass: ClassName?) : Name<ClassMapping>() {
    override val topLevelClassName = topLevelName
    override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): ClassMapping? {
        return findRenameTarget(mappings.topLevelClass, byObfuscated)
    }

    private fun findRenameTarget(mappings: ClassMapping, byObfuscated: Boolean): ClassMapping? {
        val targetName = mappings.name(byObfuscated) ?: return null
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

    fun renameAndChangePackage(mappings: ClassMapping, newPackageName: String) {
        mappings.deobfuscatedName = "$newPackageName/$topLevelName"
    }


    override fun rename(mapping: ClassMapping) {
        val packageName = (mapping.deobfuscatedName ?: mapping.obfuscatedName).split("/")
            .let { it.subList(0, it.size - 1).joinToString("/") }

        mapping.deobfuscatedName = "$packageName/$topLevelName"
    }
}


data class FieldName(val fieldName: String, val classIn: ClassName) : Name<FieldMapping>() {
    override val topLevelClassName = classIn.topLevelClassName
    override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): FieldMapping? {
        return classIn.findRenameTarget(mappings, byObfuscated)?.fields
            ?.find { it.name(byObfuscated) == fieldName }
    }

    override fun rename(mapping: FieldMapping) {
        mapping.deobfuscatedName = fieldName
    }


}

data class MethodName(val methodName: String, val classIn: ClassName, val parameterTypes: List<String>?) :
    Name<MethodMapping>() {
    override val topLevelClassName = classIn.topLevelClassName
    override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): MethodMapping? {
        val targetClass = classIn.findRenameTarget(mappings, byObfuscated) ?: return null

        // User can disambiguate with descriptor (user inputs something human readable, what is stored is not readable)
        if (parameterTypes != null) {
            TODO("Implement descriptor parser")
        }
        return targetClass.methods.find { it.name(byObfuscated) == methodName }
    }

    override fun rename(mapping: MethodMapping) {
        mapping.deobfuscatedName = methodName
    }


}

sealed class ParameterName(methodIn: MethodName) : Name<ParameterMapping>() {
    override val topLevelClassName = methodIn.topLevelClassName


    data class ByIndex(val index: Int, val method: MethodName) : ParameterName(method) {
        override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): ParameterMapping? {
            val targetMethod = method.findRenameTarget(mappings, byObfuscated) ?: return null
            targetMethod.parameters.find { it.index == index }?.let { return it }

            //TODO: uncomment once we have descriptor parser
            /*val parameterAmount =2 *//*Find method amount from descriptor *//*
            return if(parameterAmount >= index){
                // Since the enigma format emits unnamed parameters, we will add the unnamed parameter ourselves
                // so we can rename it in the rename method
                val fabricatedParameter = ParameterMapping(index, deobfuscatedName = "", parent = targetMethod)
                targetMethod.parameters.add(fabricatedParameter)
                fabricatedParameter
            }else null*/

            return null
        }

        override fun rename(mapping: ParameterMapping) {
            error("Cannot rename by index (this should be validated before we get here)")
        }
    }

    data class ByName(val name: String, val method: MethodName) : ParameterName(method) {
        override fun findRenameTarget(mappings: MappingsFile, byObfuscated: Boolean): ParameterMapping? {
            return method.findRenameTarget(mappings, byObfuscated)?.parameters?.find { it.name(byObfuscated) == name }
        }

        override fun rename(mapping: ParameterMapping) {
            mapping.deobfuscatedName = name
        }
    }
}
