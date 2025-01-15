package net.integr.serialization.obj

import net.integr.processor.BuiltTypeMetadata
import net.integr.serialization.data.DataStructure
import net.integr.serialization.data.parts.ListDataStructure
import net.integr.serialization.obj.ObjectFieldResolver.isBasicType
import net.integr.serialization.obj.ObjectFieldResolver.isCollectionType
import net.integr.serialization.obj.custom.Constructable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

object ObjectBuilder {
    fun <T: Any> build(target: KClass<T>, data: DataStructure): T {
        val usesCustomConstructable = target.companionObject != null && Constructable::class.java.isAssignableFrom(target.companionObject!!.java)

        if (usesCustomConstructable) {
            @Suppress("UNCHECKED_CAST")
            val constructable = target.companionObjectInstance as Constructable<T>
            return constructable.construct(data)
        }

        ObjectFieldResolver.assertSerializable(target)
        val classMeta = ObjectFieldResolver.loadMetaData(target)

        val paramsMap = ObjectFieldResolver.loadParameterMap(target)
        val params = mutableMapOf<KParameter, Any?>()

        classMeta.properties.forEach { (name, meta) ->
            val type = meta.realType()
            val param = paramsMap[name] ?: throw IllegalArgumentException("Parameter $name not found in constructor")

            if (!data.isObject()) throw IllegalArgumentException("Data is not a valid object")
            if (data.hasKey(name)) {
                if (type.isBasicType()) {
                    if (!data[name].isValue()) throw IllegalArgumentException("Field $name is not in valid value form")
                    params[param] = data[name].getAsBasicType(type)
                } else if (type.isCollectionType()) {
                    if (!data[name].isList()) throw IllegalArgumentException("Field $name is not in valid list form")
                    params[param] = buildCollection(meta, data[name] as ListDataStructure)
                } else if (type.java.isEnum) {
                    @Suppress("UNCHECKED_CAST")
                    params[param] = getEnumValueByName(type.java as Class<out Enum<*>>, data[name].asString())
                } else {
                    params[param] = build(type, data[name])
                }
            } else if (!param.isOptional) {
                throw IllegalArgumentException("Field $name is required")
            }
        }

        return target.constructors.first().callBy(params)
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