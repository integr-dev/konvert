package net.integr.serialization.data.parts

import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.DataStructureType

class ListDataStructure(val values: MutableList<DataStructure>) : DataStructure(DataStructureType.LIST) {
    override operator fun get(index: Int): DataStructure {
        return values[index]
    }

    override fun toString(): String {
        return values.toString()
    }
}