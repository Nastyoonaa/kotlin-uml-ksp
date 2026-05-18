import com.example.generated.projectUml
import uml.PlantUmlRenderer

import analysis.ArchitectureAnalyzer
import analysis.LlmClient
import analysis.FlowAnalyzer

fun main() {
    val project = projectUml()

    val flows = FlowAnalyzer.findAllFlows(project)
    val diagram = PlantUmlRenderer.renderWithFlows(project, flows)
    println(diagram)

    println("\n===== AI ANALYSIS =====")

    val analyzer = ArchitectureAnalyzer(LlmClient())
    val report = analyzer.analyze(project)

    println("Summary: ${report.summary}")

    println("\nProblems:")
    report.problems.forEach { println("❌ $it") }

    println("\nSuggestions:")
    report.suggestions.forEach { println("💡 $it") }

    println("\nCouplings:")
    report.couplings.forEach {
        val emoji = if (it.isStrong) "🔴" else "🟡"
        println("$emoji $it")
    }

    println("\nData Flow:")
    report.dataFlows.forEach {
        println("➡️ $it")
    }
    println("\n=== STATIC DATA FLOW ===")
    flows.forEach {
        println("➡️ $it")
    }
}