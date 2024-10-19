package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.RegexUtils.matchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matches

@SkyHanniModule
object MyFruitDigging {

    private val config get() = SkyHanniMod.feature.event.carnival.fruitDigging

    class BlockInfo(val pos: Pair<Int, Int>) {

        val possibilities = mutableListOf<DropType>()

        var diggable = true

        var bombed = false

        init {
            clear()
        }

        fun clear() {
            possibilities.clear()
            possibilities.addAll(
                listOf(
                    DropType.MANGO,
                    DropType.APPLE,
                    DropType.WATERMELON,
                    DropType.POMEGRANATE,
                    DropType.COCONUT,
                    DropType.CHERRY,
                    DropType.DURIAN,
                    DropType.DRAGONFRUIT,
                    DropType.RUM,
                    DropType.BOMB,
                ),
            )
            diggable = true
            bombed = false
        }

        fun setResult(drop: DropType) {
            possibilities.clear()
            possibilities.add(drop)
            diggable = false
        }

        fun setResolve(drop: DropType) {
            possibilities.clear()
            possibilities.add(drop)
        }

        fun setBombed() {
            diggable = false
            bombed = true
        }

        override fun toString(): String {
            return (if (diggable) "" else "!") + possibilities.toString()
        }

        var area: AreaInfo? = null

        fun isResolvedTo(drop: DropType): Boolean = diggable && possibilities.contains(drop)

        fun fancyPrint() = possibilities.joinToString(" or ") { it.display }

        fun isOnlyFruit() = possibilities.all { it.isFruit() }

        fun isOnlyBombOrRum() = possibilities.all { it == DropType.BOMB || it == DropType.RUM }

        fun isBombed() = bombed
    }

    interface AreaInfo {
        fun interact(other: AreaInfo)
        val pos: Pair<Int, Int>
        val fields: Set<Pair<Int, Int>>
            get() = directions.map { (it.first + pos.first) to (it.second + pos.second) }.toSet()

        fun overlap(other: AreaInfo): Pair<Set<Pair<Int, Int>>, OverlapInfo> {
            val myFields = fields
            val otherFields = other.fields
            val intersect = myFields.intersect(otherFields)
            //val relativeIntersect = intersect.map { (it.first - pos.first) to (it.second - pos.second) }.toSet()
            val relativePos = (other.pos.first - pos.first) to (other.pos.second - pos.second)
            val info: OverlapInfo = OverlapInfo.entries.firstOrNull { it.relation.contains(relativePos) } ?: OverlapInfo.NONE
            return intersect to info
        }

        enum class OverlapInfo(val relation: Set<Pair<Int, Int>>) {
            CORNER(
                setOf(
                    2 to 2,
                    -2 to -2,
                    2 to -2,
                    -2 to 2,
                ),
            ),
            PARALLEL(
                setOf(
                    2 to 0,
                    0 to 2,
                    -2 to 0,
                    0 to -2,
                ),
            ),
            DIAGONAL(
                setOf(
                    1 to 1,
                    -1 to -1,
                    1 to -1,
                    -1 to 1,
                ),
            ),
            TWO(
                setOf(
                    1 to 2,
                    -1 to -2,
                    1 to -2,
                    -1 to 2,
                    2 to 1,
                    -2 to -1,
                    2 to -1,
                    -2 to 1,
                ),
            ),
            NEIGHBOUR(
                setOf(
                    1 to 0,
                    0 to 1,
                    -1 to 0,
                    0 to -1,
                ),
            ),
            NONE(setOf()),
        }
    }

    private class NoFruitInfo(override val pos: Pair<Int, Int>) : AreaInfo {
        override fun interact(other: AreaInfo) {
            return
        }
    }

    private class FruitInfo(override val pos: Pair<Int, Int>, val fruit: DropType) : AreaInfo {

        val foundNearby get() = fields.any { board.getInfo(it).isResolvedTo(fruit) }

        val possibleSpace get() = fields.filter { board.getInfo(it).possibilities.contains(fruit) }

        override fun interact(other: AreaInfo) {
            if (other !is FruitInfo) return
            if (other.fruit != fruit) return
            val (overlap, info) = overlap(other)
            if (!foundNearby && overlap.size == 1 && board.getRemaining(fruit) == 1) {
                board.setResolve(overlap.first(), fruit)
            }
        }
    }

    private class BombInfo(override val pos: Pair<Int, Int>, var bombGuess: Int) : AreaInfo {
        override fun interact(other: AreaInfo) {
            if (other is NoFruitInfo) return
        }
    }

    enum class ShovelType {
        MINES {
            override fun getResult(message: String): Int? = FruitDigging.minesPattern.matchGroup(message, "amount")?.toInt()

        },
        ANCHOR {
            override fun getResult(message: String): Triple<DropType, Int, Int>? =
                if (FruitDigging.noFruitPattern.matches(message)) Triple(DropType.NONE, -1, -1) else null

        },
        TREASURE {
            override fun getResult(message: String): DropType? =
                if (FruitDigging.noFruitPattern.matches(message)) DropType.NONE else FruitDigging.treasurePattern.matchGroup(
                    message,
                    "fruit",
                )?.let { FruitDigging.convertToType(it, null) }
        },

        ;

        abstract fun getResult(message: String): Any?

        companion object {
            var active = MINES
        }
    }

    /** @param result is a [Int] for [ShovelType.MINES], is a [DropType] for [ShovelType.TREASURE] and a Triple<DropType,Int,Int> for [ShovelType.ANCHOR]*/
    fun onDig(pos: Pair<Int, Int>, drop: DropType, ability: ShovelType?, result: Any) {

        ChatUtils.chat("Digged $drop @${pos.first} ${pos.second} with $ability $result")
        board.setResult(pos, drop)
        val areaInfo: AreaInfo?
        when (ability) {
            ShovelType.MINES -> {
                if (result !is Int) throw IllegalStateException("Expected Int as result type for MINES")
                val neighbours = board.getNeighbors(pos)
                areaInfo = BombInfo(pos, result)
                if (result == 0) {
                    neighbours.forEach { it.possibilities.remove(DropType.BOMB) }
                } else {
                    val bombNeighbors = neighbours.filter { it.possibilities.contains(DropType.BOMB) && it.diggable }
                    if (bombNeighbors.size == result) {
                        bombNeighbors.forEach { board.setResolve(it.pos, DropType.BOMB) }
                    }
                }
            }

            ShovelType.ANCHOR -> {
                if (result !is Triple<*, *, *>) throw IllegalStateException("Expected Triple<DropType,Int,Int> as result type for ANCHOR")
                val item =
                    result.first as? DropType ?: throw IllegalStateException("Expected Triple<DropType,Int,Int> as result type for ANCHOR")
                val nPos = (result.second as? Int)?.let { f -> (result.third as? Int)?.let { f to it } }
                    ?: throw IllegalStateException("Expected Triple<DropType,Int,Int> as result type for ANCHOR")
                val neighbours = board.getNeighbors(pos)
                if (item == DropType.NONE) {
                    areaInfo = NoFruitInfo(pos)
                    neighbours.forEach { it.possibilities.removeIf { it != DropType.RUM && it != DropType.BOMB } }
                } else {
                    areaInfo = null
                    board.setResolve(nPos, item)
                    val notPossible = item.below
                    neighbours.forEach {
                        if (it.possibilities.size != 1) {
                            it.possibilities.removeIf { notPossible.contains(it) }
                        }
                    }
                }
            }

            ShovelType.TREASURE -> {
                if (result !is DropType) throw IllegalStateException("Expected DropType as result type for TREASURE")
                val neighbours = board.getNeighbors(pos)
                if (result == DropType.NONE) {
                    areaInfo = NoFruitInfo(pos)
                    neighbours.forEach { it.possibilities.removeIf { it != DropType.RUM && it != DropType.BOMB } }
                } else {
                    areaInfo = FruitInfo(pos, result)
                    val amount = neighbours.count { it.possibilities.contains(drop) }
                    if (amount == 1) {
                        neighbours.first { it.possibilities.contains(drop) }.let {
                            it.possibilities.clear()
                            it.possibilities.add(drop)
                        }
                    }
                    val notPossible = result.above
                    neighbours.forEach {
                        if (it.possibilities.size != 1) {
                            it.possibilities.removeIf { notPossible.contains(it) }
                        }
                    }
                }
            }

            else -> {
                areaInfo = null
            } // RUM
        }
        board.setAreaInfo(pos, areaInfo)
        println(board)
    }

    fun setBombed(pos: Pair<Int, Int>) {
        ChatUtils.chat("Bombed @${pos.first} ${pos.second}")
        board.setBombed(pos)
    }

    fun setWatermeloned(pos: Pair<Int, Int>, drop: DropType) {
        ChatUtils.chat("Watermeloned $drop @${pos.first} ${pos.second}")
        board.setResult(pos, drop)
    }

    private val board = object : Iterable<BlockInfo> {

        private val GRID_SIZE = 7

        private val cells = Array<Array<BlockInfo>>(GRID_SIZE) {
            val x = it
            Array(GRID_SIZE) {
                val z = it
                BlockInfo(x to z)
            }
        }

        private val found = mutableMapOf<DropType, Int>()

        var multiplier = 1.0

        fun getPointsForDrop(drop: DropType): Int = when (drop) {
            DropType.APPLE -> 100 * (found[DropType.APPLE] ?: 0)
            DropType.CHERRY -> found[DropType.CHERRY]?.let { 500 } ?: 200
            else -> drop.basePoints
        }

        fun clear() {
            cells.forEach { it.forEach { it.clear() } }
            found.clear()
        }

        fun setResult(pos: Pair<Int, Int>, drop: DropType) {
            cells[pos.first][pos.second].setResult(drop)
            foundLogic(drop)
        }

        fun setResolve(pos: Pair<Int, Int>, drop: DropType) {
            cells[pos.first][pos.second].setResolve(drop)
            foundLogic(drop)
        }

        fun setAreaInfo(pos: Pair<Int, Int>, areaInfo: AreaInfo?) {
            if (areaInfo == null) return
            cells[pos.first][pos.second].area = areaInfo
            getFarNeighbors(pos).forEach {
                it.area?.interact(areaInfo)
            }
        }

        private fun foundLogic(drop: DropType) {
            found.addOrPut(drop, 1)
            if (found[drop] == amountOnTheBoard[drop]) {
                cells.forEach {
                    it.forEach {
                        if (it.possibilities.size != 1) {
                            it.possibilities.remove(drop)
                        }
                    }
                }
            }
        }

        fun setBombed(pos: Pair<Int, Int>) {
            cells[pos.first][pos.second].setBombed()
        }

        fun getNeighbors(pos: Pair<Int, Int>): List<BlockInfo> =
            directions.mapNotNull { cells.getOrNull(it.first + pos.first)?.getOrNull(it.second + pos.second) }

        fun getFarNeighbors(pos: Pair<Int, Int>): List<BlockInfo> =
            farDirections.mapNotNull { cells.getOrNull(it.first + pos.first)?.getOrNull(it.second + pos.second) }

        override fun iterator(): Iterator<BlockInfo> = cells.flatten().iterator()

        override fun toString(): String {
            return "$found ${cells.contentDeepToString()}"
        }

        fun getInfo(pos: Pair<Int, Int>) = cells[pos.first][pos.second]

        fun getRemaining(fruit: DropType) = amountOnTheBoard[fruit]?.minus(found[fruit] ?: 0) ?: 0
    }

    fun getBoardState() = board.iterator()

    val directions = listOf(
        1 to 0,
        -1 to 0,
        0 to 1,
        0 to -1,
        1 to 1,
        1 to -1,
        -1 to 1,
        -1 to -1,
    )

    val farDirections = listOf(
        *directions.toTypedArray(),
        2 to -2,
        2 to -1,
        2 to 0,
        2 to 1,
        2 to 2,
        1 to 2,
        0 to 2,
        -1 to 2,
        -2 to 2,
        -2 to 1,
        -2 to 0,
        -2 to -1,
        -2 to -2,
        -1 to -2,
        0 to -2,
        1 to -2,
    )

    interface Strategy {
        fun getNextBlock(): Pair<Int, Int>
    }

    fun reset() {
        board.clear()
    }

    fun printBoard() = board.toString()

}
