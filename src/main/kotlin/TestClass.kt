package com.example

import com.example.processor.UmlDiagram

class User(val name: String)

class Order(val user: User)

@UmlDiagram
class TestClass(
    val order: Order
) {
    fun process(user: User): Order {
        return order
    }
}
