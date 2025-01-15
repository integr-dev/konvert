package net.integr.serialization

import net.integr.serialization.data.DataStructure
import net.integr.serialization.deserialization.mode.ConvertMode
import net.integr.serialization.obj.ObjectBuilder
import net.integr.serialization.obj.ObjectDeconstructor

object Konvert {
    fun deserialize(data: String, mode: ConvertMode): DataStructure {
        val converter = mode.getConverter()

        return converter.deserialize(data)
    }

    fun serialize(data: DataStructure, mode: ConvertMode, prettyPrint: Boolean = false): String {
        val converter = mode.getConverter()

        return converter.serialize(data, prettyPrint)
    }

    inline fun <reified T : Any> deserializeInto(data: String, mode: ConvertMode): T {
        val part = deserialize(data, mode)
        val obj = ObjectBuilder.build(T::class, part)

        return obj
    }

    inline fun <reified T : Any> deserializeInto(data: DataStructure): T {
        val obj = ObjectBuilder.build(T::class, data)

        return obj
    }

    inline fun <reified T : Any> serializeFrom(obj: T, mode: ConvertMode, prettyPrint: Boolean = false): String {
        val part = ObjectDeconstructor.deconstruct(T::class, obj)

        return serialize(part, mode, prettyPrint)
    }

    inline fun <reified T : Any> serializeFrom(obj: T): DataStructure {
        val part = ObjectDeconstructor.deconstruct(T::class, obj)

        return part
    }
}