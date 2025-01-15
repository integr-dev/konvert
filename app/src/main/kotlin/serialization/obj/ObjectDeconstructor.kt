package net.integr.serialization.obj

import net.integr.processor.BuiltTypeMetadata
import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.parts.ListDataStructure
import net.integr.serialization.data.parts.ValueDataStructure
import net.integr.serialization.mapper.ObjectMapper
import net.integr.serialization.obj.ObjectFieldResolver.isBasicType
import net.integr.serialization.obj.ObjectFieldResolver.isCollectionType
import net.integr.serialization.obj.custom.Deconstructable
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

object ObjectDeconstructor {
    fun deconstruct(target: KClass<*>, obj: Any): DataStructure {
        val usesCustomDeconstructable =
            target.companionObject != null && Deconstructable::class.java.isAssignableFrom(target.companionObject!!.java)

        if (usesCustomDeconstructable) {
            @Suppress("UNCHECKED_CAST")
            val constructable = target.companionObjectInstance as Deconstructable<Any>
            return constructable.deconstruct(obj)
        }

        ObjectFieldResolver.assertSerializable(target)
        val classMeta = ObjectFieldResolver.loadMetaData(target)

        val propertyMap = ObjectFieldResolver.loadPropertyMap(target)

        val mapper = ObjectMapper()

        classMeta.properties.forEach { (name, meta) ->
            val type = meta.realType()

            if (type.isBasicType()) {
                mapper.map(name, ValueDataStructure(propertyMap[name]!!.getter.call(obj)!!.toString()))
            } else if (type.isCollectionType()) {
                mapper.map(name, ListDataStructure(deconstructCollection(meta, propertyMap[name]!!.getter.call(obj) as Collection<Any>)))
            } else if (type.java.isEnum) {
                mapper.map(name, ValueDataStructure((propertyMap[name]!!.getter.call(obj) as Enum<*>).name))
            } else {
                mapper.map(name, deconstruct(type, propertyMap[name]!!.getter.call(obj)!!))
            }
        }

        return mapper.build()
    }

    private fun deconstructCollection(target: BuiltTypeMetadata, data: Collection<Any>): MutableList<DataStructure> {
        val collection = mutableListOf<DataStructure>()

        data.forEach {
            collection += if (target.arguments.first().realType().isBasicType()) ValueDataStructure(it) else deconstruct(target.arguments.first().realType(), it)
        }

        return collection
    }
}