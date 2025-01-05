package at.hannibal2.skyhanni.utils

/**
 * A generic, immutable circular list that cycles through its elements infinitely.
 *
 * @param T The type of elements in the circular list.
 * @property items A list of elements to be accessed in a circular manner.
 * @constructor Creates a CircularList with the given elements.
 * @throws IllegalArgumentException if the list is empty.
 */
class CircularList<T>(private val items: List<T>) {

    constructor(vararg elements: T) : this(elements.asList())

    init {
        require(items.isNotEmpty()) { "CircularList must not be empty" }
    }

    private var index = 0

    fun next(): T {
        val item = items[index]
        index = (index + 1) % items.size // Increment index and wrap around
        return item
    }
}
