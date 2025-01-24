package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RemoveColorTest {
    @Test
    fun testEdging() {
        Assertions.assertEquals("", "§".removeColor())
        Assertions.assertEquals("a", "a§".removeColor())
        Assertions.assertEquals("b", "§ab§".removeColor())
    }

    @Test
    fun `testDouble§`() {
        Assertions.assertEquals("1", "§§1".removeColor())
        Assertions.assertEquals("1", "§§1".removeColor(true))
        Assertions.assertEquals("k", "§§k".removeColor(true))
    }

    @Test
    fun testKeepNonColor() {
        Assertions.assertEquals("§k§l§m", "§f§k§l§m".removeColor(true))
    }

    @Test
    fun testColorToResetIfFormatted() {
        // Replace color code with §r if a formatting style is being applied
        Assertions.assertEquals("§k§l§m§r§o", "§k§l§m§f§o".removeColor(true))

        // Remove the color code otherwise
        Assertions.assertEquals("§m§r§l", "§m§r§f§l".removeColor(true))
    }

    @Test
    fun testPlainString() {
        Assertions.assertEquals("bcdefgp", "bcdefgp")
        Assertions.assertEquals("", "")
    }

    @Test
    fun testSomeNormalTestCases() {
        Assertions.assertEquals(
            "You are not currently in a party.",
            "§r§cYou are not currently in a party.§r".removeColor()
        )
        Assertions.assertEquals(
            "Ancient Necron's Chestplate ✪✪✪✪",
            "§dAncient Necron's Chestplate §6✪§6✪§6✪§6✪".removeColor()
        )
        Assertions.assertEquals(
            "PROMOTE ➜ [158] Manager",
            "§5§o§a§lPROMOTE §8➜ §7[158§7] §5Manager".removeColor()
        )
        Assertions.assertEquals(
            "§o§r§lPROMOTE §r➜ [158] Manager",
            "§5§o§a§lPROMOTE §8➜ §7[158§7] §5Manager".removeColor(true)
        )
    }
}
