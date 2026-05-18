package analysis

data class ArchitectureReport(
    val summary: String,
    val problems: List<String>,
    val suggestions: List<String>,
    val couplings: List<Coupling>,
    val dataFlows: List<String>
)

data class Coupling(
    val from: String,
    val to: String,
    val type: String
) {
    val isStrong: Boolean
        get() = type.contains("tight", ignoreCase = true)

    override fun toString(): String =
        "$from -> $to ($type)"
}