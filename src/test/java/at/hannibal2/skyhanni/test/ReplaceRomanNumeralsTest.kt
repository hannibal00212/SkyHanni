package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.features.misc.ReplaceRomanNumerals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ReplaceRomanNumeralsTest {

    @Test
    fun testTransformLine() {
        // Simple case with no Roman numerals
        Assertions.assertEquals("hello", "hello".transformLine())

        // I should never be converted
        Assertions.assertEquals("I", "I".transformLine())

        // Single Roman numeral should be converted
        Assertions.assertEquals("5", "V".transformLine())

        // Multiple Roman numerals separated by spaces
        Assertions.assertEquals("5 10 world", "V X world".transformLine())

        // Roman numeral next to punctuation
        Assertions.assertEquals("5!", "V!".transformLine())
        Assertions.assertEquals("hello 14 you?", "hello XIV you?".transformLine())

        // Mixed with color codes
        Assertions.assertEquals("§c5!", "§cV!".transformLine())
        Assertions.assertEquals("hello 2 is this 5 you?", "hello II is this V you?".transformLine())

        // If invalid Roman numeral sequences are left unchanged
        // Assuming "IIII" is invalid, it remains as is
        Assertions.assertEquals("IIII", "IIII".transformLine())

        // Check sequences with punctuation and color codes interspersed
        Assertions.assertEquals("§d5 world", "§dV world".transformLine())
        Assertions.assertEquals("hello 10 and then 1 more", "hello X and then I more".transformLine())

        // Mixed complexity
        Assertions.assertEquals("Today 2023 was great!", "Today MMXXIII was great!".transformLine())
    }

    fun String.transformLine(): String = ReplaceRomanNumerals.replaceRomanNumerals(this)
}
