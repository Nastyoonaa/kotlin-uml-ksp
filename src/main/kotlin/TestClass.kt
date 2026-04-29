package com.example

import com.example.processor.UmlDiagram

interface Repository
open class BaseService
class User(val name: String)

class Order(val user: User)

@UmlDiagram
class TestClass(
    val order: Order,
) : BaseService(), Repository {

    fun process(user: User): Order {
        return order
    }
}