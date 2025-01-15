package net.integr.serialization.converter

import net.integr.serialization.data.DataStructure

interface Converter {
    fun deserialize(data: String): DataStructure
    fun serialize(data: DataStructure, prettyPrint: Boolean): String
}