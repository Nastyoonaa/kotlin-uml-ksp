package com.example.processor

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
private const val BASE_PACKAGE = "com.example"

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
            val allClasses = collectAllClasses(classDeclaration)
                .filter { it.packageName.asString().startsWith(BASE_PACKAGE) }
            allClasses.forEach { generateUml(it) }
        }
        return invalidSymbols
    }

    private fun generateUml(classDeclaration: KSClassDeclaration) {
        val containingFile = classDeclaration.containingFile ?: return
        val className = classDeclaration.simpleName.asString()

        val propertiesList = classDeclaration.primaryConstructor
            ?.parameters
            ?.map { parameter ->
                val name = parameter.name?.asString() ?: "unknown"
                val typeDecl = parameter.type.resolve().declaration
                val type = typeDecl.simpleName.asString()
                name to type
            }
            ?: emptyList()

        val properties = propertiesList.joinToString(",\n") { (name, type) ->
            """UmlProperty("$name", "$type")"""
        }

        val methodsList = classDeclaration.getDeclaredFunctions()
            .filterNot { it.simpleName.asString() == "<init>" }
            .map { function ->
                val name = function.simpleName.asString()

                val params = function.parameters.map { param ->
                    val paramName = param.name?.asString() ?: "param"
                    val typeDecl = param.type.resolve().declaration
                    val type = typeDecl.simpleName.asString()
                    paramName to type
                }

                val returnType = function.returnType
                    ?.resolve()
                    ?.declaration
                    ?.simpleName
                    ?.asString()
                    ?: "Unit"

                Triple(name, params, returnType)
            }

        val methods = methodsList.joinToString(",\n") { (name, params, returnType) ->
            val paramsString = params.joinToString(", ") { (n, t) -> "$n: $t" }
            """UmlMethod("$name", "$paramsString", "$returnType")"""
        }

        val dependenciesList = mutableListOf<String>()
        classDeclaration.primaryConstructor
            ?.parameters
            ?.forEach { param ->
                val type = param.type.resolve().declaration as? KSClassDeclaration ?: return@forEach
                val typeName = type.simpleName.asString()

                if (typeName != className &&
                    type.packageName.asString().startsWith("com.example")
                ) {
                    addDependency(dependenciesList, className, typeName, "*--")
                }
            }

        classDeclaration.getDeclaredFunctions()
            .filterNot { it.simpleName.asString() == "<init>" }
            .forEach { function ->
                function.parameters.forEach { param ->
                    val type = param.type.resolve().declaration as? KSClassDeclaration ?: return@forEach
                    val typeName = type.simpleName.asString()

                    if (typeName != className &&
                        type.packageName.asString().startsWith("com.example")
                    ) {
                        addDependency(dependenciesList, className, typeName, "-->")
                    }
                }
                val returnType = function.returnType
                    ?.resolve()
                    ?.declaration as? KSClassDeclaration

                val returnTypeName = returnType?.simpleName?.asString()

                if (returnTypeName != null &&
                    returnTypeName != className &&
                    returnType.packageName.asString().startsWith("com.example")
                ) {
                    addDependency(dependenciesList, className, returnTypeName, "-->")
                }
            }

        val dependenciesCode = if (dependenciesList.isEmpty()) {
            "emptyList()"
        } else {
            dependenciesList.distinct().joinToString(",\n", "listOf(\n", "\n)")
        }
        val code = """
package com.example.generated

import com.example.$className
import uml.UmlClass
import uml.UmlProperty
import uml.UmlMethod
import uml.UmlDependency

fun $className.uml(): UmlClass {
    return UmlClass(
        name = "$className",
        properties = listOf(
            $properties
        ),
        methods = listOf(
            $methods
        ),
        dependencies = $dependenciesCode
    )
}
""".trimIndent()

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, containingFile),
            packageName = "com.example.generated",
            fileName = "${className}Uml",
            extensionName = "kt"
        )

        file.writer().use { it.write(code) }
    }
    private fun collectAllClasses(
        root: KSClassDeclaration,
        visited: MutableSet<String> = mutableSetOf()
    ): List<KSClassDeclaration> {
        val result = mutableListOf<KSClassDeclaration>()

        fun visit(clazz: KSClassDeclaration) {
            val name = clazz.qualifiedName?.asString() ?: return
            if (name in visited) return
            visited.add(name)

            result += clazz

            val deps = clazz.primaryConstructor?.parameters.orEmpty()
                .mapNotNull { it.type.resolve().declaration as? KSClassDeclaration }

            deps.forEach { visit(it) }
        }

        visit(root)
        return result
    }
    private fun addDependency(
        dependenciesList: MutableList<String>,
        from: String,
        to: String,
        type: String
    ) {
        val alreadyHasStrongRelation = dependenciesList.any {
            it.contains(""""$from", "$to", "*--"""")
        }

        if (!alreadyHasStrongRelation) {
            dependenciesList += """UmlDependency("$from", "$to", "$type")"""
        }
    }
}
