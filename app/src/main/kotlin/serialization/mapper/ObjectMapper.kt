package net.integr.serialization.mapper

import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.parts.ObjectDataStructure

class ObjectMapper {
    private val data = mutableMapOf<String, DataStructure>()

    fun map(key: String, value: DataStructure){
        data[key] = value
    }

    fun build(): ObjectDataStructure {
        return ObjectDataStructure(data)
    }
}