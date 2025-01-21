package net.integr.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import net.integr.processor.annotation.Named

object ClassMetadataCollector {
    @OptIn(KspExperimental::class)
    fun generateMetadata(symbol: KSClassDeclaration): ClassMetadata {
        val props = symbol.primaryConstructor?.parameters ?: throw IllegalStateException("No primary constructor found for ${symbol.simpleName.asString()}")

        val propertiesMeta = mutableListOf<PropertyMetadata>()

        var curr = 0
        for (prop in props) {
            val paramName = if (prop.isAnnotationPresent(Named::class)) prop.getAnnotationsByType(Named::class).first().name else prop.name?.asString() ?: throw IllegalStateException("Parameter name not found")

            val typeMeta = resolveType(prop.type.resolve())

            propertiesMeta += PropertyMetadata(paramName, typeMeta, prop.hasDefault)
            curr++;
        }

        val metaObject = ClassMetadata(symbol.simpleName.asString(), propertiesMeta)
        return metaObject
    }

    private fun resolveType(type: KSType): TypeMetadata {
        val arguments = type.arguments
        val resolvedArguments = mutableListOf<TypeMetadata>()

        for (arg in arguments) {
            resolvedArguments += resolveType(arg.type!!.resolve())
        }

        return TypeMetadata(type, resolvedArguments)
    }
}