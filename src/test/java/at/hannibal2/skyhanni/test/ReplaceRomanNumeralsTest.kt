package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.features.misc.ReplaceRomanNumerals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ReplaceRomanNumeralsTest {

    @Test
    fun `Regular words shouldn't be modified`() {
        Assertions.assertEquals("hello", "hello".transformLine())
    }

    @Test
    fun `'I' should never be converted`() {
        Assertions.assertEquals("I", "I".transformLine())
    }

    @Test
    fun `Single Roman numeral should be converted`() {
        Assertions.assertEquals("5", "V".transformLine())
    }

    @Test
    fun `Multiple Roman numerals separated by spaces should be converted`() {
        Assertions.assertEquals("5 10 world", "V X world".transformLine())
    }

    @Test
    fun `Roman numeral next to punctuation should be converted`() {
        Assertions.assertEquals("5!", "V!".transformLine())
        Assertions.assertEquals("hello 14 you?", "hello XIV you?".transformLine())
    }

    @Test
    fun `Mixed with color codes should be converted`() {
        Assertions.assertEquals("§c5!", "§cV!".transformLine())
        Assertions.assertEquals("hello 2 is this 5 you?", "hello II is this V you?".transformLine())
    }

    @Test
    fun `Invalid Roman numeral sequences are left unchanged`() {
        Assertions.assertEquals("IIII", "IIII".transformLine())
    }

    @Test
    fun `Sequences with punctuation and color codes interspersed should be converted`() {
        Assertions.assertEquals("§d5 world", "§dV world".transformLine())
        Assertions.assertEquals("hello 10 and then 1 more", "hello X and then I more".transformLine())
    }

    @Test
    fun `Mixed complexity should be converted`() {
        Assertions.assertEquals("Today 2023 was great!", "Today MMXXIII was great!".transformLine())
    }

    private fun String.transformLine(): String = ReplaceRomanNumerals.replaceLine(this, checkIfEnabled = false)
}
