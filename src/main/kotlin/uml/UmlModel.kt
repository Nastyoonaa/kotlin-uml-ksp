package uml

data class UmlClass(
    val name: String,
    val properties: List<UmlProperty>,
    val methods: List<UmlMethod>,
    val dependencies: List<UmlDependency>,
)

data class UmlProperty(
    val name: String,
    val type: String,
)

data class UmlMethod(
    val name: String,
    val params: String,
    val returnType: String
)

data class UmlDependency(
    val from: String,
    val to: String,
    val type: String // "-->", "*--", "o--"
)
