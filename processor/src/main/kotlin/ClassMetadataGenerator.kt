package net.integr.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import net.integr.processor.annotation.Serialize
import java.io.OutputStream

class ClassMetadataGenerator(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Serialize::class.qualifiedName!!)

        symbols.forEach { symbol ->
            if (symbol is KSClassDeclaration) {
                environment.logger.warn("Generating metadata for ${symbol.simpleName.asString()}")

                val meta = ClassMetadataCollector.generateMetadata(symbol)
                val file = environment.codeGenerator.createNewFile(
                    dependencies = Dependencies(true, symbol.containingFile!!),
                    packageName = "net.integr.serialization.built_metadata",
                    fileName = "built_${symbol.simpleName.asString()}_metadata"
                )

                file.write("package net.integr.serialization.built_metadata\n\n")

                file.write("import net.integr.processor.BuiltTypeMetadata\n")
                file.write("import net.integr.processor.BuiltPropertyMetadata\n\n")
                file.write("import net.integr.processor.BuiltClassMetadata\n\n")

                file.write("object built_${symbol.simpleName.asString()}_metadata {\n")
                file.write("    val metadata = ")
                file.write(meta.toCode() + "\n")
                file.write("}\n")

                file.close()
            }
        }

        return emptyList()
    }

    fun OutputStream.write(str: String) {
        write(str.toByteArray())
    }
}

// Related: resources/META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider
class ClassMetadataGeneratorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return ClassMetadataGenerator(environment)
    }
}
