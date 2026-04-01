package uml

class UmlDsl {
    private val lines = mutableListOf<String>()

    fun start() {
        lines += "@startuml"
    }

    fun end() {
        lines += "@enduml"
    }

    fun clazz(
        name: String,
        block: ClassDsl.() -> Unit,
    ) {
        val classDsl = ClassDsl(name)
        classDsl.block()
        lines += classDsl.build()
    }

    fun build(): String {
        return lines.joinToString("\n")
    }

    fun dependency(from: String, to: String, type: String) {
        lines += "$from $type $to"
    }

    class ClassDsl(private val name: String) {
        private val body = mutableListOf<String>()

        fun property(
            name: String,
            type: String,
        ) {
            body += "  -$name : $type"
        }

        fun method(name: String) {
            body += "  +$name"
        }

        fun build(): String {
            return buildString {
                appendLine("class $name {")
                body.forEach { appendLine(it) }
                appendLine("}")
            }
        }
    }
}
