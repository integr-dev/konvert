package net.integr.serialization.data.parts

import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.DataStructureType
import kotlin.reflect.KClass

class ValueDataStructure(private val value: Any) : DataStructure(DataStructureType.VALUE) {
    override fun asInt(): Int {
        return value.toString().toInt()
    }

    override fun asString(): String {
        return value.toString()
    }

    override fun asBoolean(): Boolean {
        return value.toString().toBoolean()
    }

    override fun asDouble(): Double {
        return value.toString().toDouble()
    }

    override fun asFloat(): Float {
        return value.toString().toFloat()
    }

    override fun asChar(): Char {
        return value.toString().toCharArray()[0]
    }

    override fun getType(): KClass<*> {
        return if (value.toString().toIntOrNull() != null) {
            Int::class
        } else if (value.toString().toFloatOrNull() != null) {
            Float::class
        } else if (value.toString().toDoubleOrNull() != null) {
            Double::class
        } else if (value.toString().toBooleanStrictOrNull() != null) {
            Boolean::class
        } else if (value.toString().length == 1) {
            Char::class
        } else {
            String::class
        }
    }

    override fun getAsBasicType(clazz: KClass<*>): Any {
        return when (clazz) {
            Int::class -> asInt()
            String::class -> asString()
            Boolean::class -> asBoolean()
            Double::class -> asDouble()
            Float::class -> asFloat()
            Char::class -> asChar()
            else -> throw IllegalArgumentException("Unsupported type!")
        }
    }

    override fun toString(): String {
        return value.toString()
    }
}