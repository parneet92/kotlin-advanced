package com.rockthejvm.typesystem

// type aliases have to be declared top-level
typealias Phonebook = Map<String, String>

// type aliases can have generic type arguments
typealias Table<A> = Map<String, A>

// example
class Either<E, A>
// variance modifiers carry over to the type alias
typealias ErrorOr<A> = Either<Throwable, A>

object TypeAliases {

    val phonebook: Phonebook = mapOf("Steven" to "324234234")
    val theMap: Map<String, String> = phonebook
    val theStringTable: Table<String> = phonebook
    @JvmStatic
    fun main(args: Array<String>) {

    }
}