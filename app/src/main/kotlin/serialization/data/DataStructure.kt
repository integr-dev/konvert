package net.integr.serialization.data

import net.integr.serialization.data.parts.ListDataStructure
import net.integr.serialization.data.parts.ValueDataStructure
import kotlin.reflect.KClass

open class DataStructure(private val type: DataStructureType) {
    override fun toString(): String {
        throw IllegalArgumentException("Data is used in raw form")
    }

    open operator fun get(key: String): DataStructure {
        throw IllegalArgumentException("Data is not a valid object")
    }

    open fun hasKey(key: String): Boolean {
        throw IllegalArgumentException("Data is not a valid object")
    }

    open operator fun get(index: Int): DataStructure {
        throw IllegalArgumentException("Data is not a valid list")
    }

    fun isValue(): Boolean {
        return type == DataStructureType.VALUE
    }

    fun isObject(): Boolean {
        return type == DataStructureType.OBJECT
    }

    fun isList(): Boolean {
        return type == DataStructureType.LIST
    }

    open fun asInt(): Int {
        throw IllegalArgumentException("Data is not in value form")
    }

    open fun asString(): String {
        throw IllegalArgumentException("Data is not in value form")
    }

    open fun asBoolean(): Boolean {
        throw IllegalArgumentException("Data is not in value form")
    }

    open fun asDouble(): Double {
        throw IllegalArgumentException("Data is not in value form")
    }

    open fun asFloat(): Float {
        throw IllegalArgumentException("Data is not in value form")
    }

    open fun asChar(): Char {
        throw IllegalArgumentException("Data is not in value form")
    }

    open fun getType(): KClass<*> {
        throw IllegalArgumentException("Data is not in value form")
    }

    open fun getAsBasicType(clazz: KClass<*>): Any {
        throw IllegalArgumentException("Data is not in value form")
    }

    fun getPath(path: String): DataStructure {
        if (path.contains(".")) {
            val parts = path.split(".")
            var data = this

            for (part in parts) {
                data = if (part.toIntOrNull() != null) data[part.toInt()] else data[part]
            }

            return data
        } else throw IllegalArgumentException("Path is not valid")
    }
}