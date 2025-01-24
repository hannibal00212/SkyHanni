package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.findStyledMatcher
import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.intoSpan
import at.hannibal2.skyhanni.utils.ComponentMatcherUtils.matchStyledMatcher
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class ComponentSpanTest {
    private fun text(string: String, init: ChatComponentText.() -> Unit = {}) = ChatComponentText(string).also(init)

    @Test
    fun testComponent() {
        val component = text("12345") {
            appendSibling(text("12345") {
                chatStyle = ChatStyle().setColor(EnumChatFormatting.RED)
            })
            appendSibling(text("12345"))
        }
        val span = component.intoSpan()
        require(span.sampleStyleAtStart()?.isEmpty == true)
        require(span.slice(5, 8).sampleStyleAtStart()?.color == EnumChatFormatting.RED)
        require(span.slice(8, 12).sampleStyleAtStart()?.color == EnumChatFormatting.RED)
        require(span.slice(10, 12).sampleStyleAtStart()?.isEmpty == true)
        require(span.slice(4, 11).intoComponent().formattedText == "§r5§r§c12345§r1§r")
    }

    @Test
    fun testRemovePrefix() {
        val component = text("12345") {
            appendSibling(text("12345"))
            appendSibling(text("12345§r"))
        }.intoSpan()
        val prefixRemoved = component.removePrefix("123")
        require(prefixRemoved.getText() == "451234512345§r")
        require(component.stripHypixelMessage().getText() == "123451234512345")
        require(component.stripHypixelMessage().slice().getText() == "123451234512345")
        require(component.slice(0, 0).slice(0, 0).getText() == "")
    }

    @Test
    fun testRegex() {
        val component = text("12345") {
            appendSibling(text("abcdef") {
                chatStyle = ChatStyle().setColor(EnumChatFormatting.RED)
            })
            appendSibling(text("12345"))
        }
        Pattern.compile("[0-9]*(?<middle>[a-z]+)[0-9]*").matchStyledMatcher(component) {
            require(groupOrThrow("middle")?.sampleStyleAtStart()?.color == EnumChatFormatting.RED)
        }
        val middlePartExtracted =
            Pattern.compile("[0-9]*(?<middle>[0-9][a-z]+[0-9])[0-9]*").matchStyledMatcher(component) {
                require(groupOrThrow("middle")?.sampleComponents()?.size == 3)
                require(
                    groupOrThrow("middle")?.sampleStyles()?.find { it.color != null }?.color == EnumChatFormatting.RED
                )
                groupOrThrow("middle")
            }!!
        Pattern.compile("(?<whole>c)").findStyledMatcher(middlePartExtracted) {
            require(groupOrThrow("whole")?.sampleStyleAtStart()?.color == EnumChatFormatting.RED)
        }
    }

}
