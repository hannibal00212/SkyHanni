package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.features.event.carnival.DropType
import at.hannibal2.skyhanni.features.event.carnival.MyFruitDigging.ShovelType
import at.hannibal2.skyhanni.features.event.carnival.MyFruitDigging.directions
import at.hannibal2.skyhanni.features.event.carnival.amountOnTheBoard
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random
import at.hannibal2.skyhanni.features.event.carnival.MyFruitDigging as Solver

object CarnivalPirateSolverTest {

    private lateinit var fakeBoard: List<MutableList<DropType>>
    private lateinit var interactBoard: List<MutableList<FieldState>>

    private enum class FieldState {
        UNTOUCHED,
        MINED,
    }

    private val GRID_SIZE = 7

    @BeforeEach
    fun clearSolver() {
        Solver.reset()
    }

    @BeforeEach
    fun createBoard() {
        fun <T> initABoard(lambda: (Int) -> T) =
            (0..<GRID_SIZE).map {
                (0..<GRID_SIZE).map(lambda).toMutableList()
            }

        interactBoard = initABoard { FieldState.UNTOUCHED }
        rummed = false

        val probabilities = initABoard { Random.nextDouble() }
        val board = initABoard { DropType.NONE }

        val stack = probabilities.withIndex().map {
            val x = it.index
            it.value.withIndex().map {
                Triple(it.value, x, it.index)
            }
        }.flatten().sortedBy { it.first }

        val fruits = ArrayDeque(
            amountOnTheBoard.map {
                val drop = it.key
                Array(it.value) { drop }.toList()
            }.flatten(),
        )

        stack.forEach {
            board[it.second][it.third] = fruits.removeFirst()
        }

        fakeBoard = board
    }

    private fun <T> List<List<T>>.asBoardString(): String {
        val maxSize = amountOnTheBoard.keys.maxOf { it.toString().length }
        return fakeBoard.joinToString("\n") { l ->
            l.joinToString(" ") { i ->
                with(i.toString()) {
                    this + (0..(maxSize - this.length)).joinToString("") { " " }
                }
            }
        }
    }

    private fun printWithBoardState() = fakeBoard.withIndex().map { (x, it) ->
        it.withIndex().map { (y, it) ->
            if (interactBoard[x][y] == FieldState.MINED) {
                "X"
            } else {
                it.toString()
            }
        }
    }.asBoardString()

    @Test
    fun testTest() {
        println("Board: ")
        println(fakeBoard.asBoardString())

    }

    private var rummed = false

    private fun <T> List<List<T>>.getNeighbors(pos: Pair<Int, Int>): List<T> =
        directions.mapNotNull { getOrNull(it.first + pos.first)?.getOrNull(it.second + pos.second) }

    private fun getNeighborsWithCondition(pos: Pair<Int, Int>, predicate: (Triple<FieldState, Int, Int>) -> Boolean) =
        interactBoard.withIndex().map {
            val x = it.index
            it.value.withIndex().map {
                Triple(it.value, x, it.index)
            }
        }.getNeighbors(pos).filter(predicate)

    private fun simulatedDig(pos: Pair<Int, Int>, ability: ShovelType) {
        if (interactBoard[pos.first][pos.second] != FieldState.UNTOUCHED) {
            throw IllegalStateException("Can't Mine Block Twice")
        }
        interactBoard[pos.first][pos.second] = FieldState.MINED
        val reward = fakeBoard[pos.first][pos.second]

        val type = if (rummed) null else ability

        // TODO figure out if BOMB is happening first or ability
        if (reward == DropType.BOMB) {
            val fruitNeighbours =
                getNeighborsWithCondition(pos) { it.first == FieldState.UNTOUCHED && fakeBoard[it.second][it.third].isFruit() }
            val maxBomb = fruitNeighbours.count()

            val numberOfBomb = if (maxBomb == 0) 0 else (1..maxBomb).random()

            val mutableFruits = fruitNeighbours.toMutableList()

            for (i in 0..<numberOfBomb) {
                mutableFruits.random().let {
                    interactBoard[it.second][it.third] = FieldState.MINED
                    Solver.setBombed(it.second to it.third)
                    mutableFruits.remove(it)
                }
            }
        }

        val result: Any
        when (type) {
            ShovelType.MINES -> {
                result =
                    getNeighborsWithCondition(pos) { it.first == FieldState.UNTOUCHED && fakeBoard[it.second][it.third] == DropType.BOMB }.count()
            }

            ShovelType.TREASURE -> {
                result =
                    getNeighborsWithCondition(pos) { it.first == FieldState.UNTOUCHED && fakeBoard[it.second][it.third].isFruit() }.maxBy {
                        fakeBoard[it.second][it.third].absoluteRanking
                    }.let { fakeBoard[it.second][it.third] }
            }

            ShovelType.ANCHOR -> {
                val find =
                    getNeighborsWithCondition(pos) { it.first == FieldState.UNTOUCHED && fakeBoard[it.second][it.third].isFruit() }.minBy {
                        fakeBoard[it.second][it.third].absoluteRanking
                    }
                result = Triple(fakeBoard[find.second][find.third], find.second, find.third)
            }

            else -> {
                result = 0
            }
        }
        rummed = false
        if (reward == DropType.RUM) {
            rummed = true
        }

        Solver.onDig(pos, reward, type, result)

        if (reward == DropType.WATERMELON) {
            getNeighborsWithCondition(pos) { it.first == FieldState.UNTOUCHED && fakeBoard[it.second][it.third].isFruit() }.random().let {
                simulateWatermelon(it.second to it.third)
            }
        }
    }

    private fun simulateWatermelon(pos: Pair<Int, Int>) {
        if (interactBoard[pos.first][pos.second] != FieldState.UNTOUCHED) {
            throw IllegalStateException("Can't Mine Block Twice")
        }
        interactBoard[pos.first][pos.second] = FieldState.MINED
        val reward = fakeBoard[pos.first][pos.second]

        Solver.setWatermeloned(pos, reward)
    }

    @Test
    fun oneDig() {
        println(fakeBoard.asBoardString())
        println()
        simulatedDig(0 to 0, ShovelType.MINES)
        simulatedDig(0 to 6, ShovelType.MINES)
        simulatedDig(6 to 0, ShovelType.MINES)
        simulatedDig(6 to 6, ShovelType.MINES)
    }

    // TODO find a better way
    fun main() {
        clearSolver()
        createBoard()
        var shouldEnd = false
        var shovelType = ShovelType.MINES
        while (!shouldEnd) {
            println()
            println(printWithBoardState())
            println()
            val it = readlnOrNull()
            when (it) {
                "a" -> shovelType = ShovelType.ANCHOR
                "m" -> shovelType = ShovelType.MINES
                "t" -> shovelType = ShovelType.TREASURE
                "00" -> simulatedDig(0 to 0, shovelType)
                "01" -> simulatedDig(0 to 1, shovelType)
                "02" -> simulatedDig(0 to 2, shovelType)
                "03" -> simulatedDig(0 to 3, shovelType)
                "04" -> simulatedDig(0 to 4, shovelType)
                "05" -> simulatedDig(0 to 5, shovelType)
                "06" -> simulatedDig(0 to 6, shovelType)
                "10" -> simulatedDig(1 to 0, shovelType)
                "11" -> simulatedDig(1 to 1, shovelType)
                "12" -> simulatedDig(1 to 2, shovelType)
                "13" -> simulatedDig(1 to 3, shovelType)
                "14" -> simulatedDig(1 to 4, shovelType)
                "15" -> simulatedDig(1 to 5, shovelType)
                "16" -> simulatedDig(1 to 6, shovelType)
                "20" -> simulatedDig(2 to 0, shovelType)
                "21" -> simulatedDig(2 to 1, shovelType)
                "22" -> simulatedDig(2 to 2, shovelType)
                "23" -> simulatedDig(2 to 3, shovelType)
                "24" -> simulatedDig(2 to 4, shovelType)
                "25" -> simulatedDig(2 to 5, shovelType)
                "26" -> simulatedDig(2 to 6, shovelType)
                "30" -> simulatedDig(3 to 0, shovelType)
                "31" -> simulatedDig(3 to 1, shovelType)
                "32" -> simulatedDig(3 to 2, shovelType)
                "33" -> simulatedDig(3 to 3, shovelType)
                "34" -> simulatedDig(3 to 4, shovelType)
                "35" -> simulatedDig(3 to 5, shovelType)
                "36" -> simulatedDig(3 to 6, shovelType)
                "40" -> simulatedDig(4 to 0, shovelType)
                "41" -> simulatedDig(4 to 1, shovelType)
                "42" -> simulatedDig(4 to 2, shovelType)
                "43" -> simulatedDig(4 to 3, shovelType)
                "44" -> simulatedDig(4 to 4, shovelType)
                "45" -> simulatedDig(4 to 5, shovelType)
                "46" -> simulatedDig(4 to 6, shovelType)
                "50" -> simulatedDig(5 to 0, shovelType)
                "51" -> simulatedDig(5 to 1, shovelType)
                "52" -> simulatedDig(5 to 2, shovelType)
                "53" -> simulatedDig(5 to 3, shovelType)
                "54" -> simulatedDig(5 to 4, shovelType)
                "55" -> simulatedDig(5 to 5, shovelType)
                "56" -> simulatedDig(5 to 6, shovelType)
                "60" -> simulatedDig(6 to 0, shovelType)
                "61" -> simulatedDig(6 to 1, shovelType)
                "62" -> simulatedDig(6 to 2, shovelType)
                "63" -> simulatedDig(6 to 3, shovelType)
                "64" -> simulatedDig(6 to 4, shovelType)
                "65" -> simulatedDig(6 to 5, shovelType)
                "66" -> simulatedDig(6 to 6, shovelType)
                else -> shouldEnd = true
            }
        }
        println("Play Ended")
    }

}
