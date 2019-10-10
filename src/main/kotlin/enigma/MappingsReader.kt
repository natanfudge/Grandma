package enigma

import put
import java.io.File


fun MappingsFile.Companion.read(file: File): MappingsFile {
    val classesIn = mutableListOf<ClassMapping>()
    var methodIn: MethodMapping? = null

    file.bufferedReader().use { reader ->
        reader.lines().forEach { line ->
            var indentCount = 0
            val lineWithoutIndentBuilder = StringBuilder()
            for (c in line) {
                if (c == '\t') indentCount++
                else lineWithoutIndentBuilder.append(c)
            }

            val lineWithoutIndent = lineWithoutIndentBuilder.toString()

            val tokens = lineWithoutIndent.split(" ")
            when (tokens[0]) {
                Prefix.Class -> parseClass(tokens).let {
                    val nestingLevel = indentCount - NaturalIndent.Class
                    classesIn.put(nestingLevel, it)
                    if (nestingLevel >= 1) classesIn[nestingLevel - 1].innerClasses.add(it)
                }
                Prefix.Field -> classesIn.getOrNull(indentCount - NaturalIndent.Field)?.fields?.add(parseField(tokens))
                    ?: error("Missing parent class of field")
                Prefix.Method -> parseMethod(tokens).let {
                    methodIn = it
                    classesIn.getOrNull(indentCount - NaturalIndent.Method)?.methods?.add(it)
                        ?: error("Missing parent class of method")
                }
                Prefix.Parameter -> methodIn?.parameters?.add(parseParameter(tokens))
                    ?: error("Missing parent method of parameter")
                else -> error("Unknown token '${tokens[0]}'")
            }
        }
    }


    return MappingsFile(classesIn.getOrNull(0) ?: error("No class found in file"))
}


fun parseClass(tokens: List<String>) = ClassMapping(
    obfuscatedName = tokens[1],
    deobfuscatedName = tokens.getOrNull(2),
    fields = mutableListOf(), innerClasses = mutableListOf(), methods = mutableListOf()
)

fun parseField(tokens: List<String>) = FieldMapping(
    obfuscatedName = tokens[1],
    deobfuscatedName = tokens[2],
    descriptor = tokens[3]
)

fun parseMethod(
    tokens: List<String>
): MethodMapping {
    var deobfName: String? = null
    val descriptor: String
    when (tokens.size) {
        3 -> descriptor = tokens[2]
        4 -> {
            deobfName = tokens[2]
            descriptor = tokens[3]
        }
        else -> error("Invalid method declaration")
    }

    return MethodMapping(
        obfuscatedName = tokens[1],
        deobfuscatedName = deobfName,
        descriptor = descriptor,
        parameters = mutableListOf()
    )
}

fun parseParameter(tokens: List<String>): ParameterMapping = ParameterMapping(
    index = tokens[1].toInt(),
    deobfuscatedName = tokens[2]
)
