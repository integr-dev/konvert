package net.integr.processor

import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.load.kotlin.KotlinClassFinder
import kotlin.reflect.jvm.internal.impl.load.kotlin.KotlinClassFinder.Result.KotlinClass
import kotlin.reflect.jvm.internal.impl.load.kotlin.KotlinClassFinderKt

data class ClassMetadata(val name: String, val properties: List<PropertyMetadata>) {
    fun toCode(): String {
        return "BuiltClassMetadata(\"$name\", listOf(${properties.joinToString { it.toCode() }}))"
    }
}
data class PropertyMetadata(val name: String, val type: TypeMetadata, val optional: Boolean) {
    fun toCode(): String {
        return "BuiltPropertyMetadata(\"$name\", ${type.toCode()}, $optional)"
    }
}
data class TypeMetadata(val type: KSType, val arguments: List<TypeMetadata>) {
    fun toCode(): String {
        return "BuiltTypeMetadata(\"${type.declaration.qualifiedName!!.asString()}\", listOf(${arguments.joinToString { it.toCode() }}))"
    }
}

data class BuiltClassMetadata(val name: String, val properties: List<BuiltPropertyMetadata>)
data class BuiltPropertyMetadata(val name: String, val type: BuiltTypeMetadata, val optional: Boolean)
data class BuiltTypeMetadata(val type: String, val arguments: List<BuiltTypeMetadata>) {
    fun realType(): KClass<*> {
        return loadKotlinClass(type) ?: throw IllegalArgumentException("Type $type not found")
    }

    private fun loadKotlinClass(className: String): KClass<*>? {
        return try {
            Class.forName(className).kotlin
        } catch (e: ClassNotFoundException) {
            when (className) {
                "kotlin.String" -> String::class
                "kotlin.Int" -> Int::class
                "kotlin.Boolean" -> Boolean::class
                "kotlin.Double" -> Double::class
                "kotlin.Float" -> Float::class
                "kotlin.Long" -> Long::class
                "kotlin.Short" -> Short::class
                "kotlin.Byte" -> Byte::class
                "kotlin.collections.List" -> List::class
                "kotlin.collections.Set" -> Set::class
                "kotlin.collections.Map" -> Map::class
                "kotlin.collections.Collection" -> Collection::class
                "kotlin.collections.Iterable" -> Iterable::class
                "kotlin.collections.Sequence" -> Sequence::class
                "kotlin.collections.MutableList" -> MutableList::class
                "kotlin.collections.MutableSet" -> MutableSet::class
                "kotlin.collections.MutableMap" -> MutableMap::class
                "kotlin.collections.MutableCollection" -> MutableCollection::class
                "kotlin.collections.MutableIterable" -> MutableIterable::class
                "kotlin.collections.ListIterator" -> ListIterator::class
                "kotlin.collections.MutableListIterator" -> MutableListIterator::class
                "kotlin.collections.MutableIterator" -> MutableIterator::class
                else -> null
            }
        }
    }
}