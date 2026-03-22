package com.rockthejvm.typesystem

object Variance {

    abstract class Pet
    class Dog(name: String): Pet()
    class Cat(name: String): Pet()


    // Dog extends Pet => List<Dog> "extends" List<Pet>
    // Variance question for List type: A extends B => List<A> extends List<B> ???
    // yes - List is a COVARIANT TYPE
    // Dog is a Pet => List<Dog> is a List<Pet>

    val lassie = Dog("Lassie")
    val hachi = Dog("Hachi")
    val laika = Dog("Laika")
    val myDogs: List<Dog> = listOf(lassie, hachi, laika)
    val myPets: List<Pet> = myDogs  // legal

    // Yes - COVARIANT Generic type
    class MyList<out A>     // <out A> => COVARIANT IN A
    val aListOfPets: MyList<Pet> = MyList<Cat>()    // legal

    // No - INVARIANT
    interface Combiner<A> { // semigroup
        fun combine(a:A, b:A): A
    }
    // Java standard library has INVARIANT
    // All Java Generics are invariant
    // val aJavaList: java.util.List<Pet> = java.util.ArrayList<Dog>() // type mismatch

    // HELL NO - CONTRAVARIANCE (opposite)
    // Dog is a Pet ( Dog Extends Pet ), then Vet<Pet> is a Vet<Dog>
    class Vet<in A> {
        fun heal(pet: Pet): Boolean = true
    }

    val myVet: Vet<Dog> = Vet<Pet>()

    // covariant types "produce" or "get" elements => "output elements" ( for example List get an element from a particular index )
    // contravariance types "consume" or "act" on elements => "input elements" ( for example Vet heals the Pet by consuming the Pet type )

    /*
        Rule of thumb, how to decide variance
        - if it "output" elements => Covariant (out)
        - if it "consumes" element => Contravariance(in)
        - otherwise, invariant (no modifier)
     */

    /*
        Exercises : Add Variance Modifiers
     */

    class RandomGenerator<out A>
    class MyOption<out A>   // holds at most 1 item
    class JSONSerializer<in A>     // turns values of type A into JSONs
    interface MyFunction<in A, out B>   // take a value of type A and returns B

    /*
        Exercise
        1. Add Variance modifier where appropriate
        2. EmptyList should be empty regardless of the type - can you make it an object ?
        3. Add an "add" method to the generic list type
            fun add(element A): LList<A>
     */

    abstract class LList<out A> {
        abstract fun head(): A      // 1st item in the list
        abstract fun tail(): LList<A>   // rest of the list without the head
    }

    data object EmptyList: LList<Nothing>() {
        override fun head(): Nothing = throw NoSuchElementException()

        override fun tail(): LList<Nothing> = throw NoSuchElementException()

    }

    data class Cons<out A>(val h: A, val t: LList<A>): LList<A>(){
        override fun head(): A = h

        override fun tail(): LList<A> = t

    }

    val myPetsL: LList<Pet> = EmptyList
    val myStringsL: LList<String> = EmptyList

    @JvmStatic
    fun main(args: Array<String>) {

    }
}