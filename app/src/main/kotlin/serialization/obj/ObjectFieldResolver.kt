package net.integr.serialization.obj

import net.integr.processor.BuiltClassMetadata
import net.integr.processor.annotation.Serialize
import net.integr.processor.annotation.Named
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.*

object ObjectFieldResolver {
    fun assertSerializable(clazz: KClass<*>) {
        if (!checkSerializable(clazz)) throw IllegalArgumentException("Class is not serializable (missing @Serialize annotation)")
    }

    private fun checkSerializable(clazz: KClass<*>): Boolean {
        return clazz.hasAnnotation<Serialize>()
    }

    fun loadMetaData(clazz: KClass<*>): BuiltClassMetadata {
        val metaName = "net.integr.serialization.built_metadata.built_${clazz.simpleName}_metadata"

        val meta = getMetadataByClassName(metaName)
        return meta
    }

    private fun getMetadataByClassName(className: String): BuiltClassMetadata {
        val clazz = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Metadata class not found")
        }

        val metadataProperty = clazz.kotlin.memberProperties.find { it.name == "metadata" }
        return metadataProperty?.getter?.call(clazz.kotlin.objectInstance) as BuiltClassMetadata
    }


    fun loadParameterMap(clazz: KClass<*>): Map<String, KParameter> {
        val params = mutableMapOf<String, KParameter>()

        clazz.primaryConstructor!!.parameters.forEach {
            if (it.hasAnnotation<Named>()) {
                params[it.findAnnotation<Named>()!!.name] = it
            } else params[it.name!!] = it
        }

        return params
    }

    fun loadPropertyMap(clazz: KClass<*>): Map<String, KProperty<*>> {
        val fields = mutableMapOf<String, KProperty<*>>()

        val params = clazz.primaryConstructor!!.parameters

        params.forEach {
            val foundMatch = clazz.declaredMemberProperties.first { prop -> prop.name == it.name }

            if (it.hasAnnotation<Named>()) {
                fields[it.findAnnotation<Named>()!!.name] = foundMatch
            } else fields[it.name!!] = foundMatch
        }

        return fields
    }

    fun KClass<*>.isBasicType(): Boolean {
        return when (this) {
            Int::class -> true
            String::class -> true
            Boolean::class -> true
            Float::class -> true
            Double::class -> true
            Char::class -> true
            else -> false
        }
    }

    fun KClass<*>.isCollectionType(): Boolean {
        return Collection::class.java.isAssignableFrom(this.java)
    }
}