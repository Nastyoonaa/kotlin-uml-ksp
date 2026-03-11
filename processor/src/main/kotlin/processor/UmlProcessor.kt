package com.example.processor

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate

class UmlProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation("com.example.processor.UmlDiagram")
        val invalidSymbols = mutableListOf<KSAnnotated>()
        symbols.forEach { symbol ->
            if (!symbol.validate()) {
                invalidSymbols += symbol
                return@forEach
            }
            val classDeclaration = symbol as? KSClassDeclaration ?: return@forEach
            generateUml(classDeclaration)
        }
        return invalidSymbols
    }

    private fun generateUml(classDeclaration: KSClassDeclaration) {
        val containingFile = classDeclaration.containingFile ?: return
        val className = classDeclaration.simpleName.asString()

        val properties = classDeclaration.primaryConstructor
            ?.parameters
            ?.map { parameter ->
                val name = parameter.name?.asString() ?: "unknown"
                val type = parameter.type.resolve().declaration.simpleName.asString()
                """UmlProperty("$name", "$type")"""
            }
            ?.joinToString(",\n")
            ?: ""

        val methods = classDeclaration.getDeclaredFunctions()
            .map { function ->
                """UmlMethod("${function.simpleName.asString()}")"""
            }
            .joinToString(",\n")
        val code = """
package com.example.generated

import com.example.$className
import uml.UmlClass
import uml.UmlProperty
import uml.UmlMethod

fun $className.uml(): UmlClass {
    return UmlClass(
        name = "$className",
        properties = listOf(
            $properties
        ),
        methods = listOf(
            $methods
        )
    )
}
""".trimIndent()

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, containingFile),
            packageName = "com.example.generated",
            fileName = "${className}Uml"
        )

        file.writer().use { it.write(code) }
    }
}
