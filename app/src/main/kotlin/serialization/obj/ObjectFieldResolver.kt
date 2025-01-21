package net.integr.serialization.obj

import net.integr.processor.BuiltClassMetadata
import net.integr.processor.annotation.Serialize
import net.integr.processor.annotation.Named
import sun.misc.Unsafe
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.collections.get
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.*
import kotlin.reflect.jvm.kotlinFunction
import kotlin.text.get
import kotlin.text.set

object ObjectFieldResolver {
    private val metadataCache = mutableMapOf<KClass<*>, BuiltClassMetadata>()
    private val parameterMapCache = mutableMapOf<KClass<*>, Map<String, Parameter>>()

    fun assertSerializable(clazz: KClass<*>) {
        if (!checkSerializable(clazz)) throw IllegalArgumentException("Class is not serializable (missing @Serialize annotation)")
    }

    private fun checkSerializable(clazz: KClass<*>): Boolean {
        return clazz.java.isAnnotationPresent(Serialize::class.java)
    }

    fun loadMetaDataWithCache(clazz: KClass<*>): BuiltClassMetadata {
        if (metadataCache.containsKey(clazz)) {
            return metadataCache[clazz]!!
        }

        val meta = loadMetaData(clazz)
        metadataCache[clazz] = meta
        return meta
    }

    fun loadMetaData(clazz: KClass<*>): BuiltClassMetadata {
        val className = clazz.java.simpleName
        val metaName = "net.integr.serialization.built_metadata.built_${className}_metadata"

        val meta = getMetadataByClassName(metaName)
        return meta
    }

    private fun getMetadataByClassName(className: String): BuiltClassMetadata {
        val clazz = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Metadata class not found")
        }

        val metadataField = clazz.getDeclaredField("metadata")
        metadataField.isAccessible = true
        val metadata = metadataField.get(null) // Pass null for static fields
        return metadata as BuiltClassMetadata
    }

    fun loadPropertyMap(clazz: KClass<*>, meta: BuiltClassMetadata): Map<String, Method> {
        val fields = mutableMapOf<String, Method>()

        val params = meta.properties.map { it.name }

        params.forEach {
            val objName = "get${it.replaceFirstChar { c -> c.uppercase() }}"
            val foundMatch = clazz.java.declaredMethods.first { method -> method.name == objName }

            if (foundMatch.isAnnotationPresent(Named::class.java)) {
                fields[foundMatch.getAnnotation(Named::class.java).name] = foundMatch
            } else fields[it] = foundMatch
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