package util

import grandma.*
import grandma.ParameterName



fun className(name: String, packageName: String? = null,
              init: (ClassBuilder.() -> NameBuilder<*>)? = null) : RenameBuilder {
    val builder = ClassBuilder(listOf(name))
    return RenameBuilder(
        if (init != null) builder.init().build() else builder.build(),
        packageName
    )
}

class RenameBuilder(
    private val oldName : Name<*>,
    private val oldNamePackage : String?
) {
    fun renamedTo(newName : String, packageName: String? = null, explanation: String? = null) : Rename<*>{
        return Rename(OriginalName(oldName,oldNamePackage) ,newName,packageName, explanation, false)
    }

    fun named(newName : String, packageName: String? = null, explanation: String? = null) : Rename<*>{
        return Rename(OriginalName(oldName,oldNamePackage) ,newName,packageName, explanation, true)
    }
}


interface NameBuilder<T : Name<*>> {
    fun build(): T
}


class ClassBuilder(private val innerClasses: List<String>) : NameBuilder<ClassName> {
    fun innerClass(className: String) = ClassBuilder(innerClasses + className)
    fun field(fieldName: String) = FieldBuilder(build(), fieldName)
    fun method(methodName: String, vararg parameterTypes: String) = MethodBuilder(
        build(), methodName,
        if (parameterTypes.isEmpty()) null else parameterTypes.toList()
    )

    override fun build(): ClassName {
        var classNameHolder = ClassName(innerClasses.last(), null)
        for (className in innerClasses.reversed().subList(1)) {
            classNameHolder = ClassName(className, classNameHolder)
        }

        return classNameHolder
    }

}

class FieldBuilder(private val className: ClassName, private val field: String) :
    NameBuilder<FieldName> {
    override fun build() = FieldName(field, className)
}

class MethodBuilder(
    private val className: ClassName, private val method: String,
    private val parameterTypes: List<String>?
) : NameBuilder<MethodName> {
    override fun build() = MethodName(method, className, parameterTypes)
    fun parameter(index: Int) = IndexParameterBuilder(build(), index)
    fun parameter(name: String) = StringParameterBuilder(build(), name)
}

class IndexParameterBuilder(private val method: MethodName, private val index: Int) :
    NameBuilder<ParameterName.ByIndex> {
    override fun build() = ParameterName.ByIndex(index, method)
}

class StringParameterBuilder(private val method: MethodName, private val paramName: String) :
    NameBuilder<ParameterName.ByName> {
    override fun build() = ParameterName.ByName(paramName, method)
}

private fun <E> List<E>.subList(fromIndex: Int) = subList(fromIndex, size)

