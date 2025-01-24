package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.ItemUtils
import org.junit.jupiter.api.Test

class ItemUtilsTest {

    private val items: MutableMap<String, Pair<String, Int>> = mutableMapOf(
        "§5Hoe of Greatest Tilling" to Pair("§5Hoe of Greatest Tilling", 1),
        "§fSilver medal §8x2" to Pair("§fSilver medal", 2),
        "§aJacob's Ticket §8x32" to Pair("§aJacob's Ticket", 32),
        "§9Delicate V" to Pair("§9Delicate V", 1),
        "  §81x §9Enchanted Sugar Cane" to Pair("§9Enchanted Sugar Cane", 1),
        "§6Gold medal" to Pair("§6Gold medal", 1),
        " §8+§319k §7Farming XP" to Pair("§7Farming XP", 19_000),
        " §8+§215 §7Garden Experience" to Pair("§7Garden Experience", 15),
        " §8+§c21 Copper" to Pair("Copper", 21),
        " §8+§b10 Bits" to Pair("Bits", 10),
        " §8+§37.2k §7Farming XP" to Pair("§7Farming XP", 7_200),
    )

    @Test
    fun testReadItemAmount() {
        for ((itemString, expected) in items) {
            val results = ItemUtils.readItemAmount(itemString)
            assert(results != null) {
                "Could not read item '$itemString'"
            }
            assert(results?.equals(expected) == true) {
                "'${results.toString()}' does not match '$expected'"
            }
        }
    }
}
