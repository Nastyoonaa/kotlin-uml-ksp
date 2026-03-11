package uml

data class UmlClass(
    val name: String,
    val properties: List<UmlProperty>,
    val methods: List<UmlMethod>,
)

data class UmlProperty(
    val name: String,
    val type: String,
)

data class UmlMethod(
    val name: String,
)
