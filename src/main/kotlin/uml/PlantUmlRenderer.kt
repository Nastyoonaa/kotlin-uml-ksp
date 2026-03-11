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
                method(it.name)
            }
        }
        dsl.end()
        return dsl.build()
    }
}
