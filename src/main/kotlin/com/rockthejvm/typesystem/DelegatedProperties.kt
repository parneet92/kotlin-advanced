package com.rockthejvm.typesystem

import kotlin.reflect.KProperty

object DelegatedProperties {

    // goal of delegated properties is to access (get/set) properties and trigger side effects

    class LoggingClassNaive(val id: Int){
        var property: Int = 0
            get() {
                // logging of the change of the value
                println("[logging $id] getting property")
                return field
            }

            set(value) {
                println("[logging $id] setting property to new value $value")
                field = value
            }
    }

    fun demoNaiveLogger(){
        val logger  = LoggingClassNaive(42)
        logger.property = 2
        val x = logger.property
        println(x)
        logger.property = 3
        val y  = logger.property
        println(y)
    }

    // Now What if you want to add logging to 2 of the properties in a class but not the other properties
    // One option is to repeat the above code for each of the properties
    // Other option is to use delegated properties to achieve that

    // Delegated Properties
    class LoggingProp<A>(val id: String, val default: A) {
        var property: A = default

        operator fun getValue(currentRef: Any, prop: KProperty<*>): A {
            // logging of the change of the value
            println("[logging $id] getting property")
            return property
        }

        operator fun setValue(currentRef: Any, prop: KProperty<*>, value: A) {
            println("[logging $id] setting property to new value $value")
            property = value
        }
    }

    class LoggingClass(id: Int) {
        var firstProperty: Int by LoggingProp("$id-firstProperty", 0)  // <-- delegated property
        var secondProperty: Int by LoggingProp("$id-secondProperty", 0)    // same behavior, reused
        var stringProperty: String by LoggingProp("$id-stringProperty", "")
    }

    fun demoLogger(){
        val loggingClass = LoggingClass(43)
        loggingClass.firstProperty = 32
        val x = loggingClass.firstProperty
        loggingClass.secondProperty = 54
        val y = x + loggingClass.secondProperty
        val z = loggingClass.stringProperty
    }

    // Now how does the delegating actually work internally ?
    class LoggingClass_v2(val id: Int) {
        var myProperty: Int by LoggingProp("$id-myProperty", 0)
    }

    // the above code translates to
    class LoggingClass_v2_Expanded(val id: Int) {
        private var prop_delegate = LoggingProp("$id-myProperty", 0)
        var myProperty: Int
            get() {
                return prop_delegate.getValue(this, this::prop_delegate)
                //                                                      ^^^^^^^^^^^^^^^^^^ reflective call
            }
            set(value) {
                prop_delegate.setValue(this, this::prop_delegate, value)
                //                                              ^^^^^^^^^^^^^^^^^^ reflective call
            }
    }

    /*
        Exercise: implement a class Delayed
     */

    class Delayed<A>(private val func: () -> A) {
        // TODO 1 : Add a variable "content", which is nullable A and starts at NULL
        private var content: A? = null
        operator fun getValue(currentRef: Any, prop: KProperty<*>): A {
            // TODO 2 : check if the current value is NULL, if not, invoke the `func` constructor arg
            // and return the content
            if( content == null)
                content = func()
            return content!!
        }
    }

    // TODO 3: use it and find out what it means?
    // lazy evaluation = variable is not set until first use
    class DelayedClass(){
        val delayedInt: Int by Delayed{ // usage as delegated property
            println("I am setting up ")
            4542
        }
    }

    fun demoDelayed(){
        val delayed = DelayedClass()
        val x = delayed.delayedInt  // first time accessed, so logger print appears
        val y = delayed.delayedInt  // no more prints
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoNaiveLogger()
        demoLogger()
        demoDelayed()
    }
}