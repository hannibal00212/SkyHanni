package at.hannibal2.skyhanni.test.garden

import at.hannibal2.skyhanni.config.features.garden.GardenConfig
import at.hannibal2.skyhanni.features.garden.visitor.VisitorTooltipParser
import org.junit.jupiter.api.Test

class VisitorToolTipParserTest {
    private val lore = mutableListOf(
        "§7Items Required:",
        " §aEnchanted Hay Bale §8x28",
        "",
        "§7Rewards:",
        " §8+§37.2k §7Farming XP",
        " §8+§215 §7Garden Experience",
        " §8+§c23 Copper",
        " §8+§b10 Bits",
        " §aJacob's Ticket",
        " §9Flowering Bouquet",
        "",
        "§eClick to give!"
    )

    @Test
    fun testParseItemsNeeded() {
        val parsedData = VisitorTooltipParser.parse(lore, GardenConfig())
        assert(parsedData.itemsNeeded.isNotEmpty()) {
            "Visitor items needed is ${parsedData.itemsNeeded.count()} instead of 1"
        }
        assert(parsedData.itemsNeeded["§aEnchanted Hay Bale"] == 28) {
            "Visitor items needed does not contain '§aEnchanted Hay Bale'"
        }
    }

    @Test
    fun testParseRewards() {
        val parsedData = VisitorTooltipParser.parse(lore, GardenConfig())
        assert(parsedData.rewards.isNotEmpty()) {
            "Visitor rewards is ${parsedData.rewards.count()} instead of 6"
        }

        val assertions = mutableMapOf(
            "§7Farming XP" to 7200,
            "§7Garden Experience" to 15,
            "Copper" to 23,
            "Bits" to 10,
            "§aJacob's Ticket" to 1,
            "§9Flowering Bouquet" to 1
        )

        for ((itemName, amount) in assertions) {
            assert(parsedData.rewards[itemName] == amount) {
                "Visitor rewards does not contain '$itemName' with amount '$amount'"
            }
        }
    }

    @Test
    fun testParseCopper() {
        val parsedData = VisitorTooltipParser.parse(lore, GardenConfig())
        val copper = parsedData.rewards["Copper"]
        assert(copper == 23) {
            "Visitor rewards does not contain 'Copper' with amount '23'"
        }
    }
}
