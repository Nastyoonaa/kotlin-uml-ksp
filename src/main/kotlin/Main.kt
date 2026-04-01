import com.example.Order
import com.example.TestClass
import com.example.User
import com.example.generated.uml
import uml.PlantUmlRenderer

fun main() {
    val user = User("Iydyshka")
    val order = Order(user)
    val test = TestClass(order)

    val uml = test.uml()
    val plant = PlantUmlRenderer.render(uml)
    println(plant)
}