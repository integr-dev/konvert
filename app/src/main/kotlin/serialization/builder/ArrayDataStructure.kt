package net.integr.serialization.builder

import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.parts.ValueDataStructure

class ArrayDataStructure {
    private val list = mutableListOf<DataStructure>()

    fun value(value: DataBuilder.() -> Unit) {
        val dataBuilder = DataBuilder()
        dataBuilder.value()
        list.add(dataBuilder.build())
    }

    fun value(value: Any) {
        list.add(ValueDataStructure(value))
    }

    fun build(): MutableList<DataStructure> {
        return list
    }
}