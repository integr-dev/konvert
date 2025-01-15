package net.integr.serialization.data.parts

import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.DataStructureType

class ObjectDataStructure(val content: Map<String, DataStructure>) : DataStructure(DataStructureType.OBJECT) {
    override operator fun get(key: String): DataStructure {
        return content[key] ?: throw IllegalArgumentException("Key not found!")
    }

    override fun hasKey(key: String): Boolean {
        return content.containsKey(key)
    }

    override fun toString(): String {
        return content.toString()
    }
}