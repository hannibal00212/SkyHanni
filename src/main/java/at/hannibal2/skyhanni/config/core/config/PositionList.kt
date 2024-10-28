package at.hannibal2.skyhanni.config.core.config

import io.github.notenoughupdates.moulconfig.annotations.ConfigLink

class PositionList() : ArrayList<Position>(), MutableList<Position> {

    constructor(init: Collection<Position>) : this() {
        this.addAll(init)
    }

    constructor(size: Int) : this() {
        this.addAll(List(size) { Position(0, 0) })
    }

    fun setLink(configLink: ConfigLink) {
        this.configLink = configLink
        forEach {
            it.setLink(configLink)
        }
    }

    private var configLink: ConfigLink? = null

    override fun add(element: Position): Boolean {
        configLink?.let {
            element.setLink(it)
        }
        return super.add(element)
    }

    override fun addAll(elements: Collection<Position>): Boolean {
        configLink?.let { link ->
            elements.forEach {
                it.setLink(link)
            }
        }
        return super.addAll(elements)
    }

    override fun set(index: Int, element: Position): Position {
        configLink?.let {
            element.setLink(it)
        }
        return super.set(index, element)
    }
}
