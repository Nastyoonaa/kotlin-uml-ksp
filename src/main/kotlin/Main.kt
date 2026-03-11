import com.example.TestClass
import com.example.generated.uml
import uml.PlantUmlRenderer

fun main() {
    val test = TestClass("Iydyshka", 25)
    val uml = test.uml()
    val plant = PlantUmlRenderer.render(uml)
    println(plant)
}
