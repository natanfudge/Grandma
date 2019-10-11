@file:Suppress("UnnecessaryVariable")

package grandma

import grandma.enigma.*
import javax.lang.model.SourceVersion

//typealias Errorable<T> = StringResult<out T, String>
//typealias Success<T> = StringSuccess<out T, String>
//typealias StringError<T> = GenericError<T, String>

private val <T : Name<*>> T.success get() = NameParseResult.Success(this)
private val <T> T.success get() = StringSuccess(this)
private fun <T : Name<*>> fail(error: String) = NameParseResult.Error<T>(error)

//sealed class RenameParseResult<M : Mapping> {
//    data class Success<M : Mapping>(val parsed: Rename<M>) : RenameParseResult<M>()
//    data class Error<M : Mapping>(val error: String) : RenameParseResult<M>()
//}


//private fun Rename<*>.success/
//private fun stringError(str : String)

fun buildRename(
    explanation: String?, byObfuscated: Boolean, oldPackageName: String?, newPackageName: String?,
    oldName: Name<*>, newName: Name<*>
): StringResult<Rename<*>> {
    fun <T : Mapping> buildSpecificRename(specificOldName: Name<T>, specificNewName: Name<T>): Rename<*> {
        val oldShortName = OriginalName.Short(specificOldName)
        return Rename(
            originalName = if (oldPackageName != null) {
                OriginalName.Qualified(unqualifiedName = oldShortName, partialPackage = oldPackageName)
            } else oldShortName,
            explanation = explanation,
            newName = specificNewName,
            byObfuscated = byObfuscated,
            newPackageName = newPackageName
        )
    }

    return when (oldName) {
        is ClassName -> if (newName is ClassName) buildSpecificRename(oldName, newName).success
        else StringError("The old name is a class, but the new one is not")
        is FieldName -> if (newName is FieldName) buildSpecificRename(oldName, newName).success
        else StringError("The old name is a field, but the new one is not")
        is MethodName -> if (newName is MethodName) buildSpecificRename(oldName, newName).success
        else StringError("The old name is a method, but the new one is not")
        is ParameterName -> if (newName is ParameterName) buildSpecificRename(oldName, newName).success
        else StringError("The old name is a parameter, but the new one is not")
    }

}


fun parseRename(
    keyWord: KeyWord,
    rawOldName: String,
    rawNewName: String,
    explanation: String?
): StringResult<Rename<*>> {
    val (oldNamePackage, oldName) = splitPackageAndName(rawOldName)
    val (newNamePackage, newName) = splitPackageAndName(rawNewName)
    val oldNameParsed = when (val oldNameParsedOrError = NameParser(allowIndexParameter = true).parseName(oldName)) {
        is NameParseResult.Success -> oldNameParsedOrError.parsed
        is NameParseResult.Error -> return StringError(oldNameParsedOrError.error)
    }
    val newNameParsed = when (val newNameParsedOrError = NameParser(allowIndexParameter = false).parseName(newName)) {
        is NameParseResult.Success -> newNameParsedOrError.parsed
        is NameParseResult.Error -> return StringError(newNameParsedOrError.error)
    }

    return buildRename(explanation,keyWord == KeyWord.Name,oldNamePackage,newNamePackage,
        oldNameParsed,newNameParsed)
}

//TODO: the propagation of errors here needs to be refactored to use type aliased StringResult

private fun splitPackageAndName(rawName: String): Pair<String?, String> {
    val lastSlashIndex = rawName.lastIndexOf('/')
    return if (lastSlashIndex == -1) null to rawName
    else rawName.splitOn(lastSlashIndex)
}

private class NameParser(
    /**
     * To handle the case in which the user inputs an indexed parameter name as the new name (this is invalid)
     */
    val allowIndexParameter: Boolean
) {
    fun parseName(name: String): NameParseResult<*> {
        // Yes, the split index is being found twice, this is slightly inefficient.
        val lastSplitter = name.findLastAnyOf(Joiner.All)

        return if (lastSplitter != null) {
            when (lastSplitter.second) {
                Joiner.Method -> parseMethod(name)
                Joiner.Field -> parseField(name)
                Joiner.InnerClass -> parseClass(name)
                Joiner.Parameter -> parseParameter(name)
                else -> error("Impossible")
            }
        } else parseClass(name)

    }

    // Note that classes are parsed from left to right, and methods, fields and parameter are parsed from right to left
    private fun parseClass(name: String): NameParseResult<ClassName> {
        val splitIndex = name.indexOf(Joiner.InnerClass)
        if (splitIndex == -1) {
            return if (!SourceVersion.isName(name)) fail("'$name' is not a valid class name")
            else ClassName(name, innerClass = null).success
        }

        val (outerClass, innerClass) = name.splitOn(splitIndex)
        if (!SourceVersion.isName(name)) fail<ClassName>("'$name' is not a valid class name")

        return parseClass(innerClass).map { ClassName(outerClass, innerClass = it) }
    }

    private fun parseMethod(name: String): NameParseResult<MethodName> {
        val splitIndex = name.lastIndexOf(Joiner.Method)
        if (splitIndex == -1) return fail("Expected '$name' to be a method")
        val (className, methodName) = name.splitOn(splitIndex)
        if (!SourceVersion.isName(methodName)) return fail("'$methodName' is not a valid method name")

        //TODO: parse parameter types
        return parseClass(className).map { MethodName(methodName, it, null) }
    }

    private fun parseField(name: String): NameParseResult<FieldName> {
        val splitIndex = name.lastIndexOf(Joiner.Field)
        if (splitIndex == -1) return fail("Expected '$name' to be a field")
        val (className, fieldName) = name.splitOn(splitIndex)
        if (!SourceVersion.isName(fieldName)) return fail("'$fieldName' is not a valid field name")

        return parseClass(className).map { FieldName(fieldName, it) }
    }

    private fun parseParameter(name: String): NameParseResult<ParameterName> {
        val splitIndex = name.lastIndexOf(Joiner.Parameter)
        if (splitIndex == -1) return fail("Expected '$name' to be a parameter")
        val (methodName, parameterIndexOrNameWithBracket) = name.splitOn(splitIndex)
        val parameterIndexOrName = parameterIndexOrNameWithBracket.removeSuffix("]")
        parameterIndexOrName.toIntOrNull()?.let { parameterIndex ->
            if (!allowIndexParameter) return fail("The new parameter name must an identifier, not a number.")
            return parseMethod(methodName).map { ParameterName.ByIndex(parameterIndex, it) }
        }

        val parameterName = parameterIndexOrName

        if (!SourceVersion.isName(parameterName)) return fail("'$parameterName' is not a valid parameter name")
        return parseMethod(methodName).map { ParameterName.ByName(parameterName, it) }
    }

}


sealed class NameParseResult<T : Name<*>> {
    open class Success<T : Name<*>>(open val parsed: T) : NameParseResult<T>() {
        override fun <C : Name<*>> map(mapping: (T) -> C) = mapping(parsed).success
    }

    data class Error<T : Name<*>>(val error: String) : NameParseResult<T>() {
        override fun <C : Name<*>> map(mapping: (T) -> C) = fail<C>(error)
    }

    abstract fun <C : Name<*>> map(mapping: (T) -> C): NameParseResult<C>

}


object Joiner {
    const val Method = "#"
    const val Field = "F"
    const val InnerClass = "$"
    const val Parameter = "["
    val All = listOf(Method, Field, InnerClass, Parameter)
}

