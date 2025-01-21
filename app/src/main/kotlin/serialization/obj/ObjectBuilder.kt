package net.integr.serialization.obj

import net.integr.processor.BuiltClassMetadata
import net.integr.processor.BuiltTypeMetadata
import net.integr.processor.annotation.Construct
import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.parts.ListDataStructure
import net.integr.serialization.obj.ObjectFieldResolver.isBasicType
import net.integr.serialization.obj.ObjectFieldResolver.isCollectionType
import net.integr.serialization.obj.custom.Constructable
import java.lang.reflect.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.time.measureTime

object ObjectBuilder {
    private val constructableCache = mutableMapOf<KClass<*>, Boolean>()

    fun <T: Any> build(target: KClass<T>, data: DataStructure): T {
        val isConstructable = constructableCache.getOrPut(target) {
            target.java.annotations.any { it.annotationClass == Construct::class.java }
        }

        if (isConstructable) {
            @Suppress("UNCHECKED_CAST")
            val constructable = target.companionObjectInstance as Constructable<T>
            return constructable.construct(data)
        }

        ObjectFieldResolver.assertSerializable(target)

        var classMeta = ObjectFieldResolver.loadMetaDataWithCache(target)

        val params = mutableListOf<Any>()

        classMeta.properties.forEach { (name, meta, optional) ->
            val type = meta.realType()

            if (!data.isObject()) throw IllegalArgumentException("Data is not a valid object")
            if (data.hasKey(name)) {
                if (type.isBasicType()) {
                    if (!data[name].isValue()) throw IllegalArgumentException("Field $name is not in valid value form")
                    params += data[name].getAsBasicType(type)
                } else if (type.isCollectionType()) {
                    if (!data[name].isList()) throw IllegalArgumentException("Field $name is not in valid list form")
                    params += buildCollection(meta, data[name] as ListDataStructure)
                } else if (type.java.isEnum) {
                    @Suppress("UNCHECKED_CAST")
                    params += getEnumValueByName(type.java as Class<out Enum<*>>, data[name].asString())
                } else {
                    params += build(type, data[name])
                }
            } else if (!optional) {
                throw IllegalArgumentException("Field $name is required")
            }
        }

        @Suppress("UNCHECKED_CAST")
        return target.java.constructors[0].newInstance(*params.toTypedArray()) as T
    }

    private fun buildCollection(target: BuiltTypeMetadata, data: ListDataStructure): Collection<*> {
        val collection = mutableListOf<Any>()
        val typeArgument = target.arguments.first().realType()
        data.values.forEach {
            collection += if (typeArgument.isBasicType()) it else build(typeArgument, it)
        }

        return collection
    }

    private fun <T : Enum<T>> getEnumValueByName(enumClass: Class<T>, name: String): T {
        return java.lang.Enum.valueOf(enumClass, name)
    }
}