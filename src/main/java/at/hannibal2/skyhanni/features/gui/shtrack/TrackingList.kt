package at.hannibal2.skyhanni.features.gui.shtrack

import java.util.function.Predicate

class TrackingList : ArrayList<TrackingElement<*>>(), MutableList<TrackingElement<*>> {

    var isActive = false

    fun activate() {
        if (isActive) return
        isActive = true
        forEach {
            it.atAdd()
            it.line = it.generateLine()
        }
        ShTrack.updateDisplay()
    }

    fun deactivate() {
        if (!isActive) return
        isActive = false
        forEach {
            it.atRemove()
            it.line = emptyList()
        }
    }

    override fun clear() {
        forEach {
            it.atRemove()
        }
        super.clear()
        ShTrack.updateDisplay()
    }

    override fun addAll(elements: Collection<TrackingElement<*>>): Boolean {
        elements.forEach { it.atAdd() }
        val r = super.addAll(elements)
        ShTrack.updateDisplay()
        return r
    }

    override fun remove(element: TrackingElement<*>): Boolean = indexOf(element).let {
        if (it == -1) false else {
            removeAt(it)
            true
        }
    }

    override fun removeAt(index: Int): TrackingElement<*> {
        this[index].atRemove()
        val r = super.removeAt(index)
        ShTrack.updateDisplay()
        return r
    }

    override fun set(index: Int, element: TrackingElement<*>): TrackingElement<*> {
        this.getOrNull(index)?.atRemove()
        element.atAdd()
        val r = super.set(index, element)
        ShTrack.updateDisplay()
        return r
    }

    override fun add(element: TrackingElement<*>): Boolean {
        element.atAdd()
        val r = super.add(element)
        ShTrack.updateDisplay()
        return r
    }

    override fun add(index: Int, element: TrackingElement<*>) {
        element.atAdd()
        val r = super.add(index, element)
        ShTrack.updateDisplay()
        return r
    }

    override fun addAll(index: Int, elements: Collection<TrackingElement<*>>): Boolean {
        elements.forEach { it.atAdd() }
        val r = super.addAll(index, elements)
        ShTrack.updateDisplay()
        return r
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        (fromIndex..<toIndex).forEach { this[it].atRemove() }
        super.removeRange(fromIndex, toIndex)
        ShTrack.updateDisplay()
    }

    override fun removeAll(elements: Collection<TrackingElement<*>>): Boolean {
        var r = true
        elements.forEach { if (!remove(it)) r = false }
        return r
    }

    override fun removeIf(filter: Predicate<in TrackingElement<*>>): Boolean {
        var r = false
        val iter = iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            if (filter.test(it)) {
                r = true
                it.atRemove()
                iter.remove()
            }
        }
        ShTrack.updateDisplay()
        return r
    }
}
