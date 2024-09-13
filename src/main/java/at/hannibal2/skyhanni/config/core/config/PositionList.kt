package at.hannibal2.skyhanni.config.core.config

import io.github.notenoughupdates.moulconfig.annotations.ConfigLink

class PositionList() : ArrayList<Position>(), MutableList<Position> {

    constructor(init: Collection<Position>) : this() {
        this.addAll(init)
    }

    constructor(size: Int) : this() {
        this.addAll((0..<size).map { Position() })
    }

    @Throws(NoSuchFieldException::class)
    fun setLink(configLink: ConfigLink) {
        this.configLink = configLink
        forEach {
            it.setLink(configLink)
        }
    }

    private var configLink: ConfigLink? = null

    override fun add(element: Position): Boolean {
        if (configLink != null) {
            element.setLink(configLink!!)
        }
        return super.add(element)
    }

    override fun addAll(elements: Collection<Position>): Boolean {
        if (configLink != null) {
            elements.forEach {
                it.setLink(configLink!!)
            }
        }
        return super.addAll(elements)
    }

    override fun set(index: Int, element: Position): Position {
        if (configLink != null) {
            element.setLink(configLink!!)
        }
        return super.set(index, element)
    }
}
