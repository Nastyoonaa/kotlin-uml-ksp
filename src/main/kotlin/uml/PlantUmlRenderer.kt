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
}
