package at.hannibal2.skyhanni.features.mining.fossilexcavator.solver

data class FossilShape(val tiles: List<FossilTile>) {
    fun width() = tiles.maxOf { it.x } - tiles.minOf { it.x }
    fun height() = tiles.maxOf { it.y } - tiles.minOf { it.y }

    fun moveTo(x: Int, y: Int): FossilShape {
        return FossilShape(tiles.map { FossilTile(it.x + x, it.y + y) })
    }

    fun rotate(degree: Int): FossilShape {
        val width = this.width()
        val height = this.height()
        return when (degree) {
            90 -> FossilShape(tiles.map { FossilTile(it.y, width - it.x) })
            180 -> FossilShape(tiles.map { FossilTile(width - it.x, height - it.y) })
            270 -> FossilShape(tiles.map { FossilTile(height - it.y, it.x) })
            else -> this
        }
    }

    fun flipShape(): FossilShape {
        val height = this.height()
        return FossilShape(tiles.map { FossilTile(it.x, height - it.y) })
    }
}
