package com.rockthejvm.typesystem

import java.util.UUID
import kotlin.properties.Delegates
import kotlin.random.Random
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


    /*
        Standard Delegated Properties
     */

    // 1. lazy
    data class UserData(val name: String, val email: String)

    class Person(val id: String) {
        private fun fetchUserData(): UserData {
            // complex logic or some i/o call which takes a while
            println("Fetching User Data from remote server...")
            Thread.sleep(3000)
            return UserData("John Doe", "johndoes@gmail.com")
        }

        fun showUserData() {
            println("User Data: $userData")
        }

        val userData: UserData by lazy {    // lazy evaluation - a property is NOT computed until first use
            // perform long computation or network call
            // triggered on first use
            fetchUserData()
        }
    }

    fun demoLazy(){
        val person = Person("abc-123")
        println("User created.")    // at this point , fetchUserData() is not triggered
        println("About to show user data")
        person.showUserData()   // user data is first accessed, fetchUserData will be triggered
        // user data is fetched and cached
        println("Showing user data once more...")
        person.showUserData()   // fetchUserData will not be triggered anymore, user data exists
    }

    // 2. Vetoable
    class BankAccount(initialBalance: Double){  // NEVER use double for money
        var balance: Double by Delegates.vetoable(initialBalance) { prop, oldValue, newValue ->
            // must return a boolean
            // if true -> var will be changed, if not, the change will be DENIED
            newValue >= 0
        }
    }

    fun demoVeto(){
        val account = BankAccount(100.0)
        println("Initial balance: ${account.balance}")
        account.balance = 150.0 // this should succeed
        println("Updated balance: ${account.balance}")  // 150.0
        account.balance = -50.0 // this should be vetoed
        println("Final balance: ${account.balance}")    // 150.0

    }

    // 3. Observable - perform side effects on changing of your properties
    // Example: monitoring the staleness of a dataset
    enum class State {
        NONE, NEW, PROCESSED, STALE
    }

    class MonitoredDataset(name: String){
        var state: State by Delegates.observable(State.NONE) { prop, oldValue, newValue ->
            // can alert a system if state changes
            println("[Dataset - $name] State changed from $oldValue -> $newValue")
            if(newValue == State.STALE)
                println("[Dataset - $name] Alert : Dataset is now stale, refresh data")
        }
        private var data: List<String> = listOf()

        fun consumeData(){
            if(state == State.PROCESSED)
                state = State.STALE
            else if ( data.isNotEmpty())
                state = State.PROCESSED
                // we dump the data to a persistent store
                data = listOf()
        }

        fun fetchData(){
            if(Random.nextBoolean()){   // whether data exists upstream or not
                data = (1..5).map { UUID.randomUUID().toString() }  // get the data
                state = State.NEW   // reset the state
            }
        }
    }

    fun demoObservable(){
        val dataset = MonitoredDataset("sensored-data-incremental")
        dataset.fetchData()
        dataset.consumeData()
        dataset.fetchData()
        dataset.consumeData()
        dataset.consumeData()
    }

    // 4. map - bridge connection between Kotlin and weakly typed data e.g. JSON
    class WeakObject(val attributes: Map<String, Any>){
        val name: String by attributes  // this is a delegated property
        val size: Int by attributes
    }

    fun demoMapDelegated() {
        val myDict = WeakObject(mapOf(
            "size" to 123445,
            "name" to "Jane Doe"
        ))
        println("Name of dataset: ${myDict.name}")  // actually uses attributes.get("name) as String, beware this can crash if "name" is not a key in the map
        println("Size of dataset: ${myDict.size}")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        /*demoNaiveLogger()
        demoLogger()
        demoDelayed()
        demoLazy()
        demoVeto()*/
        demoObservable()
        demoMapDelegated()
    }
}