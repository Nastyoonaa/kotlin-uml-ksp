package uml

data class UmlProject(
    val classes: List<UmlClass>,
    val dependencies: List<UmlDependency>,
)
