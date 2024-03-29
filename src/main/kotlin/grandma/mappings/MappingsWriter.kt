@file:Suppress("NOTHING_TO_INLINE")

package grandma.mappings

import java.io.BufferedWriter
import java.io.File

fun MappingsFile.writeTo(file: File) {
    file.parentFile.mkdirs()
    file.bufferedWriter().use {
        MappingsWriter(it).write(this)
    }
}

object Prefix {
    const val Class = "CLASS"
    const val Method = "METHOD"
    const val Field = "FIELD"
    const val Parameter = "ARG"
}

object NaturalIndent {
    const val Class = 0
    const val Method = 1
    const val Field = 1
    const val Parameter = 2
}

private class MappingsWriter(val writer: BufferedWriter) {

    fun write(mappingsFile: MappingsFile) = mappingsFile.write()

    private fun ClassMapping.write(indent: Int = 0) {
        val classIndent = indent + NaturalIndent.Class
        if (deobfuscatedName != null) writeLine(classIndent, Prefix.Class, obfuscatedName, deobfuscatedName!!)
        else writeLine(classIndent, Prefix.Class, obfuscatedName)

        for (innerClass in innerClasses) innerClass.write(indent + 1)

        for (field in fields) {
            val fieldIndent = indent + NaturalIndent.Field
            writeLine(fieldIndent, Prefix.Field, field.obfuscatedName, field.deobfuscatedName, field.descriptor)
        }

        for (method in methods) {
            val methodIndent = indent + NaturalIndent.Method
            if (method.deobfuscatedName != null) {
                writeLine(
                    methodIndent,
                    Prefix.Method,
                    method.obfuscatedName,
                    method.deobfuscatedName!!,
                    method.descriptor
                )
            } else writeLine(methodIndent, Prefix.Method, method.obfuscatedName, method.descriptor)

            for (parameter in method.parameters) {
                val paramIndent = indent + NaturalIndent.Parameter
                writeLine(paramIndent, Prefix.Parameter, parameter.index.toString(), parameter.deobfuscatedName)
            }
        }


    }


    private inline fun writeSpace() = writer.write(' '.toInt())
    private inline fun writeNewLine() = writer.write('\n'.toInt())

    private fun write(indent: Int, part1: String, part2: String) {
        for (i in 0 until indent) writer.write('\t'.toInt())
        writer.write(part1)
        writeSpace()
        writer.write(part2)
    }

    private fun write(indent: Int, part1: String, part2: String, part3: String) {
        write(indent, part1, part2)
        writeSpace()
        writer.write(part3)
    }

    private inline fun write(indent: Int, part1: String, part2: String, part3: String, part4: String) {
        write(indent, part1, part2, part3)
        writeSpace()
        writer.write(part4)
    }

    private inline fun writeLine(indent: Int, part1: String, part2: String) {
        write(indent, part1, part2)
        writeNewLine()
    }

    private fun writeLine(indent: Int, part1: String, part2: String, part3: String) {
        write(indent, part1, part2, part3)
        writeNewLine()
    }

    private fun writeLine(indent: Int, part1: String, part2: String, part3: String, part4: String) {
        write(indent, part1, part2, part3, part4)
        writeNewLine()
    }
}

