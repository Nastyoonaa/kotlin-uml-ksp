package analysis

import uml.UmlProject

class ArchitectureAnalyzer(
    private val llm: LlmClient
) {

    fun analyze(project: UmlProject): ArchitectureReport {
        val couplings = findCouplings(project)
        val violations = findLayerViolations(project)
        val cycles = findCycles(project)

        val couplingProblems = couplings
            .groupBy { it.from }
            .filter { it.value.size >= 2 }
            .map { (clazz, deps) ->
                "$clazz is highly coupled (${deps.size} dependencies)"
            }

        val staticProblems = couplingProblems + violations + cycles

        val staticSuggestions = buildList {
            if (couplings.isNotEmpty()) add("Reduce coupling using interfaces")
            if (violations.isNotEmpty()) add("Fix layer dependencies")
            if (cycles.isNotEmpty()) add("Break cyclic dependencies")
        }

        val staticReport = ArchitectureReport(
            summary = "Static architecture analysis",
            problems = staticProblems,
            suggestions = staticSuggestions,
            couplings = couplings,
            dataFlows = emptyList()
        )
        val aiReport = try {
            llm.ask(project.toAnalysisText())
        } catch (e: Exception) {
            println("LLM failed: ${e.message}")
            null
        }
        return merge(staticReport, aiReport)
    }

    private fun merge(
        static: ArchitectureReport,
        ai: ArchitectureReport?
    ): ArchitectureReport {

        if (ai == null) return static

        return ArchitectureReport(
            summary = static.summary + " + AI insights",
            problems = (static.problems + ai.problems).distinct(),
            suggestions = (static.suggestions + ai.suggestions).distinct(),
            couplings = static.couplings,
            dataFlows = ai.dataFlows
        )
    }
}
private fun findCouplings(project: UmlProject): List<Coupling> {
    return project.dependencies
        .groupBy { it.from }
        .filter { it.value.size >= 2 }
        .flatMap { (from, deps) ->
            deps.map { dep ->
                Coupling(
                    from = from,
                    to = dep.to,
                    type = "static"
                )
            }
        }
}

private fun findLayerViolations(project: UmlProject): List<String> {
    val classMap = project.classes.associateBy { it.name }

    return project.dependencies.mapNotNull { dep ->
        val fromLayer = classMap[dep.from]?.layer
        val toLayer = classMap[dep.to]?.layer

        if (fromLayer == "application" && toLayer == "infrastructure") {
            "${dep.from} should not depend on ${dep.to} (layer violation)"
        } else null
    }
}

private fun findCycles(project: UmlProject): List<String> {
    val graph = project.dependencies.groupBy { it.from }
        .mapValues { it.value.map { d -> d.to } }

    val visited = mutableSetOf<String>()
    val stack = mutableSetOf<String>()
    val cycles = mutableListOf<String>()

    fun dfs(node: String) {
        if (node in stack) {
            cycles += "Cycle detected at $node"
            return
        }
        if (node in visited) return

        visited += node
        stack += node

        graph[node]?.forEach { dfs(it) }

        stack -= node
    }

    project.classes.forEach { dfs(it.name) }

    return cycles
}