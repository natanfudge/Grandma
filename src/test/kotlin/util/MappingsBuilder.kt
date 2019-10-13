package util

import grandma.descriptor.FieldDescriptor
import grandma.descriptor.FieldType
import grandma.descriptor.MethodDescriptor
import grandma.descriptor.ReturnDescriptor
import grandma.mappings.ClassMapping
import grandma.mappings.FieldMapping
import grandma.mappings.MethodMapping
import grandma.mappings.ParameterMapping

@DslMarker
annotation class MappingsBuilderDsl

fun mappingsFile(topLevelObf: String, topLevelDeobf: String? = null, init: ClassMappingsBuilder.() -> Unit) =
    ClassMappingsBuilder(topLevelObf, topLevelDeobf, null).apply(init).build()


@MappingsBuilderDsl
class ClassMappingsBuilder(obfuscatedName: String, deobfuscatedName: String?, parent: ClassMapping?) {
    private val mapping = ClassMapping(
        obfuscatedName, deobfuscatedName, mutableListOf(), mutableListOf(),
        mutableListOf(), parent
    )

    fun innerClass(obfName: String, deobfName: String? = null, init: ClassMappingsBuilder.() -> Unit = {}) {
        mapping.innerClasses.add(ClassMappingsBuilder(obfName, deobfName, mapping).apply(init).build())
    }

    fun method(
        obfName: String,
        deobfName: String? = null,
        returnType: ReturnDescriptor = ReturnDescriptor.Void,
        vararg parameterTypes: FieldType,
        init: MethodMappingsBuilder.() -> Unit = {}
    ) = mapping.methods.add(
        MethodMappingsBuilder(obfName, deobfName, MethodDescriptor(parameterTypes.toList(), returnType), mapping)
            .apply(init).build()
    )

    fun field(obfName: String, deobfName: String, type: FieldDescriptor) = mapping.fields.add(
        FieldMapping(obfName, deobfName, type.classFileName, mapping)
    )


    fun build() = mapping
}

@MappingsBuilderDsl
class MethodMappingsBuilder(
    obfuscatedName: String, deobfuscatedName: String?, descriptor: MethodDescriptor, parent: ClassMapping
) {

    private val mapping = MethodMapping(
        obfuscatedName, deobfuscatedName, descriptor.classFileName,
        mutableListOf(), parent
    )

    fun param(index: Int, name: String) = mapping.parameters.add(ParameterMapping(index, name, mapping))
    fun build() = mapping
}

