package net.integr.serialization.obj

import net.integr.processor.BuiltTypeMetadata
import net.integr.processor.annotation.annotation.Deconstruct
import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.parts.ListDataStructure
import net.integr.serialization.data.parts.ValueDataStructure
import net.integr.serialization.mapper.ObjectMapper
import net.integr.serialization.obj.ObjectFieldResolver.isBasicType
import net.integr.serialization.obj.ObjectFieldResolver.isCollectionType
import net.integr.serialization.obj.custom.Deconstructable
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object ObjectDeconstructor {
    private val deconstructableCache = mutableMapOf<KClass<*>, Boolean>()

    fun deconstruct(target: KClass<*>, obj: Any): DataStructure {
        val isDeconstructable = deconstructableCache.getOrPut(target) {
            target.java.annotations.any { it.annotationClass == Deconstruct::class.java }
        }

        if (isDeconstructable) {
            @Suppress("UNCHECKED_CAST")
            val deconstructable = target.companionObjectInstance as Deconstructable<Any>
            return deconstructable.deconstruct(obj)
        }

        ObjectFieldResolver.assertSerializable(target)
        val classMeta = ObjectFieldResolver.loadMetaData(target)

        val propertyMap = ObjectFieldResolver.loadPropertyMap(target, classMeta)

        val mapper = ObjectMapper()

        classMeta.properties.forEach { (name, meta, _) ->
            val type = meta.realType()

            if (type.isBasicType()) {
                mapper.map(name, ValueDataStructure(propertyMap[name]!!.invoke(obj)!!.toString()))
            } else if (type.isCollectionType()) {
                mapper.map(name, ListDataStructure(deconstructCollection(meta, propertyMap[name]!!.invoke(obj) as Collection<Any>)))
            } else if (type.java.isEnum) {
                mapper.map(name, ValueDataStructure((propertyMap[name]!!.invoke(obj) as Enum<*>).name))
            } else {
                mapper.map(name, deconstruct(type, propertyMap[name]!!.invoke(obj)!!))
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