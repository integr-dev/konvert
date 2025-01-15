package net.integr.serialization.converter.impl

import net.integr.serialization.converter.Converter
import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.parts.ListDataStructure
import net.integr.serialization.data.parts.ObjectDataStructure
import net.integr.serialization.data.parts.ValueDataStructure
import net.integr.serialization.mapper.ObjectMapper
import net.integr.serialization.prettyprint.PrettyPrintContext

class JsonConverter : Converter {
    override fun deserialize(data: String): DataStructure {
        return deserializeCorrect(data)
    }

    private fun deserializeObject(data: String): ObjectDataStructure {
        val objectMapper = ObjectMapper()
        val parts = splitDataIntoParts(data.substring(1, data.length - 1))

        for (part in parts) {
            val key = part.substringBefore(':').trim().trim('"')
            val value = part.substringAfter(':').trim()

            objectMapper.map(key, deserializeCorrect(value))
        }

        return objectMapper.build()
    }

    private fun deserializeList(data: String): ListDataStructure {
        val parts = splitDataIntoParts(data.substring(1, data.length - 1))

        val list = mutableListOf<DataStructure>()

        for (part in parts) {
            list.add(deserializeCorrect(part))
        }

        return ListDataStructure(list)
    }

    private fun deserializeCorrect(data: String): DataStructure {
        return if (isObject(data)) {
            deserializeObject(data)
        } else if (isList(data)) {
            deserializeList(data)
        } else {
            ValueDataStructure(data.trim().trim('"'))
        }
    }

    private fun isObject(data: String): Boolean {
        return data.startsWith('{')
    }

    private fun isList(data: String): Boolean {
        return data.startsWith('[')
    }

    private fun splitDataIntoParts(data: String): List<String> {
        val parts = mutableListOf<String>()
        var current = ""
        var open = 0

        for (char in data) {
            if (char == '{' || char == '[') {
                open++
            } else if (char == '}' || char == ']') {
                open--
            }

            if (char == ',' && open == 0) {
                parts.add(current)
                current = ""
            } else {
                current += char
            }
        }

        parts.add(current)
        return parts
    }

    override fun serialize(data: DataStructure, prettyPrint: Boolean): String {
        val prettyPrintContext = PrettyPrintContext(prettyPrint, 0)
        return serializeCorrect(data, prettyPrintContext)
    }

    private fun serializeValue(data: ValueDataStructure): String {
        val type = data.getType()

        return when (type) {
            Int::class -> data.asInt().toString()
            String::class -> "\"${data.asString()}\""
            Float::class -> data.asFloat().toString()
            Double::class -> data.asDouble().toString()
            Char::class -> "\"${data.asChar()}\""
            Boolean::class -> data.asBoolean().toString()
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    private fun serializeObject(data: ObjectDataStructure, ctx: PrettyPrintContext): String {
        val builder = StringBuilder()

        val newCtx = ctx.increaseIndent()

        builder.append(ctx.withNewLine("{"))
        for ((key, value) in data.content) {
            builder.append(newCtx.withNewLineAndIndent("\"$key\": ${serializeCorrect(value, newCtx)}, "))
        }

        if (ctx.isEnabled()) builder.delete(builder.length - 3, builder.length-1) else builder.delete(builder.length - 2, builder.length)
        builder.append(ctx.withIndent("}"))

        return builder.toString()
    }

    private fun serializeList(data: ListDataStructure, ctx: PrettyPrintContext): String {
        val builder = StringBuilder()

        val newCtx = ctx.increaseIndent()

        builder.append(ctx.withNewLine("["))
        for (value in data.values) {
            builder.append(newCtx.withNewLineAndIndent("${serializeCorrect(value, newCtx)}, "))
        }

        if (ctx.isEnabled()) builder.delete(builder.length - 3, builder.length-1) else builder.delete(builder.length - 2, builder.length)
        builder.append(ctx.withIndent("]"))

        return builder.toString()
    }

    private fun serializeCorrect(data: DataStructure, ctx: PrettyPrintContext): String {
        return when (data) {
            is ValueDataStructure -> serializeValue(data)
            is ObjectDataStructure -> serializeObject(data, ctx)
            is ListDataStructure -> serializeList(data, ctx)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}