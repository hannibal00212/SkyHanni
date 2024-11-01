package at.hannibal2.skyhanni.utils

class CircularList<T>(private val items: List<T>) {

    constructor(vararg elements: T) : this(elements.asList())

    private var index = 0

    fun next(): T {
        if (items.isEmpty()) throw NoSuchElementException("CircularList is empty")
        val item = items[index]
        index = (index + 1) % items.size // Increment index and wrap around
        return item
    }
}
