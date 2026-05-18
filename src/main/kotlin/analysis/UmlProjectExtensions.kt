package analysis

import uml.UmlProject

fun UmlProject.toAnalysisText(): String {
    val classesText = classes.joinToString("\n") { clazz ->
        buildString {
            appendLine("Class: ${clazz.name} (${clazz.layer})")

            clazz.properties.forEach {
                appendLine("  - ${it.name}: ${it.type}")
            }

            clazz.methods.forEach {
                appendLine("  - ${it.name}(${it.params}): ${it.returnType}")
            }
        }
    }

    val depsText = dependencies.joinToString("\n") {
        "${it.from} ${it.type} ${it.to}"
    }

    return """
CLASSES:
$classesText

DEPENDENCIES:
$depsText
""".trimIndent()
}