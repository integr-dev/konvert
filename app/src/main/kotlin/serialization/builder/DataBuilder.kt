package net.integr.serialization.builder

import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.parts.ListDataStructure
import net.integr.serialization.data.parts.ValueDataStructure
import net.integr.serialization.mapper.ObjectMapper

class DataBuilder {
    private val mapper = ObjectMapper()

    fun obj(key: String, value: DataBuilder.() -> Unit) {
        val dataBuilder = DataBuilder()
        dataBuilder.value()
        mapper.map(key, dataBuilder.build())
    }

    fun value(key: String, value: Any) {
        mapper.map(key, ValueDataStructure(value))
    }

    fun arr(key: String, value: ArrayDataStructure.() -> Unit) {
        val dataStructure = ArrayDataStructure()
        dataStructure.value()
        mapper.map(key, ListDataStructure(dataStructure.build()))
    }

    fun build(): DataStructure {
        return mapper.build()
    }

    companion object {
        fun make(root: DataBuilder.() -> Unit): DataStructure {
            val dataBuilder = DataBuilder()
            dataBuilder.root()

            return dataBuilder.build()
        }
    }
}