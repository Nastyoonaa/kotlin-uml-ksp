package com.example.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter

class UmlProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.example.processor.UmlDiagram")
            .filterIsInstance<KSClassDeclaration>()

        symbols.filter { !it.validate() }.forEach { it.validate() }
        symbols.filter { it.validate() }.forEach { generateUml(it) }

        return emptyList()
    }

    private fun generateUml(classDeclaration: KSClassDeclaration) {
        val properties = classDeclaration.getAllProperties()
            .joinToString("\n") {
                "  -${it.simpleName.asString()} : ${it.type.resolve().declaration.simpleName.asString()}"
            }
        val packageName = classDeclaration.containingFile!!.packageName.asString()
        val className = classDeclaration.simpleName.asString()

        val methods = classDeclaration.getAllFunctions()
            .filter { it.simpleName.asString() != "<init>" }
            .joinToString("\n") { "  +${it.simpleName.asString()}()" }

        val uml = """
@startuml
class $className {
$properties

$methods
}
@enduml
""".trimIndent()

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, classDeclaration.containingFile!!),
            packageName = "",
            fileName = "${className}Diagram",
            extensionName = "puml"
        )

        OutputStreamWriter(file).use { it.write(uml) }

        logger.warn("Сгенерирован UML для $className", classDeclaration)
    }
}
