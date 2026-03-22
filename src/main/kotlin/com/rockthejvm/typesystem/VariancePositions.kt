package com.rockthejvm.typesystem

object VariancePositions {

    abstract class Animal
    class Dog: Animal()
    class Cat: Animal()
    class Crocodile: Animal()

    // out = Covariant, in = Contravariant
    // ################### Case 1 ###############
    // this is illegal
    //class Vet<in A>(val favoriteAnimal: A){
    //    fun treat(animal: A): Boolean = true
    //}
    /*
        assume that this was legal
        class Vet<in A>(val favoriteAnimal: A)

        val garfield = Cat()
        val lassie = Dog()
        val theVet: Vet<Animal> = Vet<Animal>(garfield)
        // contravariance
        val theDogVet: Vet<Dog> = theVet    // Vet<Supertype of Dog> as per contravariance
        val favAnimal = theDogVet.favoriteAnimal    // guaranteed to be a dog but it's actually a Cat

        types of properties ( val or var ) are in "out" aka covariant position
        "in" types cannot be placed in "out" position
     */

    // ################### Case 2 ###############
    // class MutableContainer<out A>(var contents: A)  // var properties are also in Contravariant position ("in)

    /*
        class Liquid
        class Water: Liquid()
        class Gasoline: Liquid()

        val container: MutableContainer<Liquid> = MutableContainer<Water>(Water())
        container.contents = Gasoline() // Guarantee that I can add any liquid inside , but have to keep it to Water

        types of vars are in "in" position ( aka contravariant)
        => must be INVARIANT

     */

    // ################### Case 3 ###############
    //class LList<out A> {
    // illegal here
    //    fun add(elem: A): LList<A> = TODO()
    //}

    /*
        val myList: LList<Animal> = LList<Dog>()
        val newList = myList.add(Crocodile())   // guranteed to be able to add any animal but have to guarantee just Dog

        method arg types are in "in" ( aka contravariant position )
        => cannot use covariant types in method args
     */

    // ################### Case 3 ###############
    //class Vet<in A>{
    //    fun rescueAnimal(): A = TODO()
    //}

    /*
        assume this compiled
        class Vet<in A>{
            fun rescueAnimal(): A = TODO()
        }

        val myVet: Vet<Animal> = object: Vet<Animal> {
            override fun rescueAnimal(): Animal = Cat()
        }

        val dogVet: Vet<Dog> = myVet    // legal because of contravariance
        val rescueDog = dogVet.rescueAnimal()   // guaranteed to return a Dog but returns a Cat

        method return types are in "out" (aka covariant) position
     */

    /*
        Solve Variance Position Problem
     */
    // 1. Consume Elements in Covariant Type
    abstract class LList<out A>
    data object EmptyList: LList<Nothing>()
    data class Cons<out A>(val head: A, val tail: LList<A>): LList<A>()

    // how do we add an element?
    // [lassi, hachi, laika].add("togo) => LList<Dog>
    // [lassi, hachi, laika].add("garfield") => LList<Animal>
    // [lassi, hachi, laika].add(45) => List<Any>
    // solution = widening the type

    fun<B, A:B> LList<A>.add(elem: B): LList<B> = Cons(elem, this)

    //2. Return Elements in Contravariant type
    // solution - narrow the type
    abstract class Vehicle
    open class Car: Vehicle()
    class Supercar: Car()

    class RepairShop<in A: Vehicle> {
        fun<B:A> repair(elem: B): B = elem
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val myList: LList<Dog> = EmptyList
        myList.add(Dog()).add(Dog())    // LList<Dog>
        val animals = myList.add(Cat()) // LList<Animal>

        // contravariance
        val myRepairShop: RepairShop<Car> = RepairShop<Vehicle>()
        val myBeatUpVW = Car()
        val myFerrari = Supercar()

        val freshVW = myRepairShop.repair(myBeatUpVW)   // Car
        val freshFerrari = myRepairShop.repair(myFerrari)   // Supercar
    }
}