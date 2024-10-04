package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.features.event.carnival.DropType
import at.hannibal2.skyhanni.features.event.carnival.FruitDigging
import at.hannibal2.skyhanni.features.event.carnival.amountOnTheBoard
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CarnivalPirateSolverTest {

    lateinit var fakeBoard: List<MutableList<DropType>>

    private val GRID_SIZE = 7

    @BeforeEach
    fun createBoard() {
        fun <T> initABoard(lambda: (Int) -> T) =
            (0..GRID_SIZE).map {
                (0..GRID_SIZE).map(lambda).toMutableList()
            }

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

    @Test
    fun testTest(){
        println(fakeBoard)
    }

}
