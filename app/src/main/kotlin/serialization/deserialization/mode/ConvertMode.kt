package net.integr.serialization.deserialization.mode

import net.integr.serialization.converter.Converter
import net.integr.serialization.converter.impl.JsonConverter

enum class ConvertMode {
    JSON {
        override fun getConverter(): Converter {
            return jsonConverter
        }
    };

    abstract fun getConverter(): Converter

    val jsonConverter = JsonConverter()
}