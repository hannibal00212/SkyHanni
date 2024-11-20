import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import at.hannibal2.skyhanni.utils.compat.*

class TestLegacyColorFormat {
    @Test
    fun testLegacyColorFormatString() {
        val text = Text.literal("")
            .append(Text.literal("[").withColor(Formatting.DARK_GRAY))
            .append(Text.literal("302").withColor(Formatting.BLUE))
            .append(Text.literal("] ").withColor(Formatting.DARK_GRAY))
            .append(Text.literal("♫ ").withColor(Formatting.GOLD))
            .append(Text.literal("[MVP").withColor(Formatting.AQUA))
            .append(Text.literal("+").withColor(Formatting.LIGHT_PURPLE))
            .append(Text.literal("] lrg89").withColor(Formatting.AQUA))
            .append(Text.literal(": test").withColor(Formatting.WHITE))
        Assertions.assertEquals("§r§r§8[§r§9302§r§8] §r§6♫ §r§b[MVP§r§d+§r§b] lrg89§r§f: test§r", text.getFormattedTextCompat())
    }

}
