package analysis

import uml.UmlProject

object FlowAnalyzer {

    fun findAllFlows(project: UmlProject): List<String> {

        val graph = project.dependencies
            .groupBy { it.from }
            .mapValues { it.value.map { d -> d.to } }

        val flows = mutableListOf<String>()

        fun dfs(current: String, path: List<String>) {
            val next = graph[current] ?: return

            next.forEach { node ->
                val newPath = path + node

                if (newPath.size >= 3) {
                    flows += newPath.joinToString(" -> ")
                }

                dfs(node, newPath)
            }
        }

        project.classes.forEach {
            dfs(it.name, listOf(it.name))
        }

        return flows.distinct()
    }
}