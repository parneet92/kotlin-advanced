package com.rockthejvm.typesystem

object LambdasWithReceivers {

    // create a behavior
    // option 1 - OOP
    data class Person(val name: String, val age: Int){
        fun greet() = "Hi I am ${name}"
    }

    // option 2 - procedural way - create a function that takes a person
    fun greet(p: Person) =
        "Hi I am ${p.name}"

    // option 3 - extension method (Kotlin/Scala)
    fun Person.greetExt() = "Hi I am ${name}"
    //  ^^^^^^^ RECEIVER Type => gives us access to 'this' reference

    // option 4 - function value (lambda)
    val greetFun: (Person) -> (String) = { p -> "Hi I am ${p.name}"}

    // option 5 - lambdas with receiver ( "an extension lambda")
    val greetFunRec: Person.() -> String = { "Hi I am ${name} "}
    //              ^^^^^^^ RECEIVER type => gives us access to 'this' reference
    val simpleLambda: () -> String = { "Kotlin" }

    // Why do we need this
    // APIs that looked "baked into Kotlin" aka DSL
    // example: ktor, arrow, coroutines

    // So we will be creating mini library for JSON serialisation
    // supports number(Int), Strings and JSON Objects
    // { "name": "Daniel", "age": 12 }

    sealed interface JsonValue
    data class JsonNumber(val value: Int): JsonValue {
        override fun toString(): String = value.toString()
    }

    data class JsonString(val value: String): JsonValue {
        override fun toString(): String = "\"${value}\""
    }

    data class JsonObject(val attributes: Map<String, JsonValue>): JsonValue{
        override fun toString(): String =
            attributes.toList().joinToString(",", "{", "}") {
                pair -> "\"${pair.first}\" : ${pair.second}"
            }
    }

    //mutable builder of Json Object
    class JSON {
        private var props: MutableMap<String, JsonValue> = mutableMapOf()

        fun toValue(): JsonValue = JsonObject(props)

        // not so nice api
        fun addString(name: String, value: String) {
            props[name] = JsonString(value)
        }

        fun addInt(name: String, value: Int) {
            props[name] = JsonNumber(value)
        }

        fun addValue(name: String, value: JsonValue) {
            props[name] = value
        }

        // nice api
        infix fun String.to(value: String) {    // "name" to "Daniel
            props[this] = JsonString(value)
        }

        infix fun String.to(value: Int) {   // "age" to 12
            props[this] = JsonNumber(value)
        }

        infix fun String.to(value: JsonValue) {
            props[this] = value
        }

    }

    fun jsonNotSoNice(init: (JSON) -> Unit): JsonValue {
        val obj = JSON()
        init(obj)
        return obj.toValue()
    }

    fun json(init: JSON.() -> Unit): JsonValue {
        val obj = JSON()
        obj.init()
        return obj.toValue()
    }


    @JvmStatic
    fun main(args: Array<String>) {

        val jsonObject = JsonObject(
            mapOf(
                "user" to JsonObject(
                    mapOf(
                        "name" to JsonString("Daniel"),
                        "age" to JsonNumber(12)
                    )
                ),
                "credentials" to JsonObject(
                    mapOf(
                        "type" to JsonString("password"),
                        "value" to JsonString("rockthejvm")
                    )
                )
            )
        )

        val json_Obj_v2 = jsonNotSoNice { j ->
            j.addValue("user", jsonNotSoNice { j ->
                j.addString("name", "Daniel")
                j.addInt("age", 12)
            })
            j.addValue("credentials", jsonNotSoNice { j ->
                j.addString("type", "password")
                j.addString("value", "rockthejvm")
            })
        }

        val json_Obj_v3 = json {    // In this scope I have access to all the extension methods defined earlier
            "user" to json {
                "name" to "Daniel"
                "age" to 12
            }
            "credentials" to json {
                "type" to "password"
                "value" to "rockthejvm"
            }
        }
    }
}