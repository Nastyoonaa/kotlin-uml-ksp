import com.example.generated.projectUml
import uml.PlantUmlRenderer

fun main() {
    val project = projectUml()
    val diagram = PlantUmlRenderer.renderProject(project)
    println(diagram)
}