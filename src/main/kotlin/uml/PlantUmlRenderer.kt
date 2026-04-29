package uml

object PlantUmlRenderer {
    fun render(clazz: UmlClass): String {
        val dsl = UmlDsl()
        dsl.start()
        dsl.clazz(clazz.name) {
            clazz.properties.forEach {
                property(it.name, it.type)
            }
            clazz.methods.forEach {
                method("${it.name}(${it.params}): ${it.returnType}")
            }
        }
        clazz.dependencies.forEach {
            dsl.dependency(it.from, it.to, it.type)
        }
        dsl.end()
        return dsl.build()
    }

    fun renderProject(project: UmlProject): String {
        val dsl = UmlDsl()
        dsl.start()

        val grouped = project.classes.groupBy { it.layer }

        grouped.forEach { (layer, classes) ->

            dsl.raw("package \"$layer\" {")

            classes.forEach { clazz ->
                dsl.clazz(clazz.name) {
                    clazz.properties.forEach {
                        property(it.name, it.type)
                    }
                    clazz.methods.forEach {
                        method("${it.name}(${it.params}): ${it.returnType}")
                    }
                }
            }

            dsl.raw("}")
        }

        project.dependencies
            .distinct()
            .forEach { dep ->
                dsl.dependency(dep.from, dep.to, dep.type)
            }

        dsl.end()
        return dsl.build()
    }
}
