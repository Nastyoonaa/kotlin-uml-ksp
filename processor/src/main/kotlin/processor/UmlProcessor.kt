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
            .filterIsInstance<KSClassDeclaration>()

        val invalid = symbols.filterNot { it.validate() }.toList()
        val valid = symbols.filter { it.validate() }.toList()

        val allClasses = valid
            .flatMap(::collectAllClasses)
            .filter { it.packageName.asString().startsWith(BASE_PACKAGE) }
            .toSet()

        allClasses.forEach(::generateUml)
        generateProjectUml(allClasses.toList())

        return invalid
    }

    private fun generateUml(classDeclaration: KSClassDeclaration) {
        val containingFile = classDeclaration.containingFile ?: return
        val className = classDeclaration.simpleName.asString()
        val functionName = className.replaceFirstChar { it.lowercase() } + "Uml"
        val packageName = classDeclaration.packageName.asString()
        val layer = resolveLayer(classDeclaration)

        val properties = classDeclaration.primaryConstructor
            ?.parameters
            .orEmpty()
            .joinToString(",\n") {
                val name = it.name?.asString() ?: "unknown"
                val type = it.type.resolve().declaration.simpleName.asString()
                """UmlProperty("$name", "$type")"""
            }

        val methods = classDeclaration.getDeclaredFunctions()
            .filterNot { it.simpleName.asString() == "<init>" }
            .joinToString(",\n") { function ->
                val name = function.simpleName.asString()

                val params = function.parameters.joinToString(", ") { param ->
                    val pName = param.name?.asString() ?: "param"
                    val pType = param.type.resolve().declaration.simpleName.asString()
                    "$pName: $pType"
                }

                val returnType = function.returnType
                    ?.resolve()
                    ?.declaration
                    ?.simpleName
                    ?.asString()
                    ?: "Unit"

                """UmlMethod("$name", "$params", "$returnType")"""
            }

        val dependencies = buildList {
            classDeclaration.primaryConstructor
                ?.parameters
                .orEmpty()
                .forEach {
                    val type = it.type.resolve().declaration as? KSClassDeclaration ?: return@forEach
                    if (type.simpleName.asString() != className &&
                        type.packageName.asString().startsWith(BASE_PACKAGE)
                    ) {
                        add("""UmlDependency("$className", "${type.simpleName.asString()}", "*--")""")
                    }
                }
            addAll(extractMethodDependencies(classDeclaration, className))
            classDeclaration.superTypes
                .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
                .firstOrNull { it.classKind == ClassKind.CLASS }
                ?.takeIf {
                    it.simpleName.asString() != className &&
                            it.packageName.asString().startsWith(BASE_PACKAGE)
                }
                ?.let {
                    add("""UmlDependency("$className", "${it.simpleName.asString()}", "--|>")""")
                }
            classDeclaration.superTypes
                .mapNotNull { it.resolve().declaration as? KSClassDeclaration }
                .filter { it.classKind == ClassKind.INTERFACE }
                .filter {
                    it.simpleName.asString() != className &&
                            it.packageName.asString().startsWith(BASE_PACKAGE)
                }
                .forEach {
                    add("""UmlDependency("$className", "${it.simpleName.asString()}", "..|>")""")
                }
        }.distinct()

        val dependenciesCode =
            if (dependencies.isEmpty()) "emptyList()"
            else dependencies.joinToString(",\n", "listOf(\n", "\n)")

        val code = """
package com.example.generated

import uml.*

fun $functionName(): UmlClass {
    return UmlClass(
        name = "$className",
        packageName = "$packageName",
        layer = "$layer",
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

        codeGenerator.createNewFile(
            dependencies = Dependencies(false, containingFile),
            packageName = "com.example.generated",
            fileName = "${className}Uml",
        ).writer().use { it.write(code) }
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
    private fun resolveLayer(clazz: KSClassDeclaration): String {
        val name = clazz.simpleName.asString()

        return when {
            name.endsWith("Controller") -> "controller"
            name.endsWith("Service") -> "service"
            name.endsWith("Repository") -> "repository"
            name.startsWith("Base") -> "infrastructure"
            clazz.classKind == ClassKind.CLASS &&
                    clazz.getDeclaredFunctions().none { it.simpleName.asString() != "<init>" } ->
                "domain"

            else -> "application"
        }
    }
    private fun generateProjectUml(classes: List<KSClassDeclaration>) {
        if (classes.isEmpty()) return

        val classEntries = classes.joinToString(",\n") {
            val name = it.simpleName.asString()
            val fn = name.replaceFirstChar { c -> c.lowercase() } + "Uml"
            "$fn()"
        }

        val code = """
package com.example.generated

import uml.*

fun projectUml(): UmlProject {
    val classes: List<UmlClass> = listOf(
        $classEntries
    )

    val dependencies = classes
        .flatMap { it.dependencies }
        .distinct()

    return UmlProject(
        classes = classes,
        dependencies = dependencies
    )
}
""".trimIndent()

        codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES, // 🔥 ВАЖНО
            packageName = "com.example.generated",
            fileName = "ProjectUml",
        ).writer().use { it.write(code) }
    }
    private fun extractMethodDependencies(
        clazz: KSClassDeclaration,
        className: String
    ): List<String> =
        clazz.getDeclaredFunctions()
            .filterNot { it.simpleName.asString() == "<init>" }
            .flatMap { function ->
                function.parameters
                    .mapNotNull { param ->
                        (param.type.resolve().declaration as? KSClassDeclaration)
                            ?.takeIf {
                                it.simpleName.asString() != className &&
                                        it.packageName.asString().startsWith(BASE_PACKAGE)
                            }
                    }
            }
            .distinctBy { it.simpleName.asString() }
            .map {
                """UmlDependency("$className", "${it.simpleName.asString()}", "-->")"""
            }
            .toList()
}
