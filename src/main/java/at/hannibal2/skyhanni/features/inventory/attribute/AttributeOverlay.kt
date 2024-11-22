package at.hannibal2.skyhanni.features.inventory.attribute

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiRenderItemEvent
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeAPI.getAttributesLevels
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeAPI.isGoodRoll
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.StringUtils.width
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object AttributeOverlay {

    private const val SCALE = 0.5714286f

    private val config get() = SkyHanniMod.feature.inventory.attributeOverlay

    @SubscribeEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        if (!isEnabled()) return

        val stack = event.stack ?: return
        val attributes = stack.getAttributesLevels() ?: return
        val internalName = stack.getInternalNameOrNull() ?: return

        val isGoodRoll = attributes.isGoodRoll(internalName)
        attributes.toList().filter { (attr, level) ->
            val inConfig = attr in config.attributesList
            val isLevel = level >= config.minimumLevel
            val goodRoll = !config.hideNonGoodRolls || isGoodRoll
            inConfig && goodRoll && (!config.goodRollsOverrideLevel || isLevel)
        }.forEachIndexed { index, attribute ->
            event.drawAttribute(attribute, isGoodRoll && config.highlightGoodRolls, index)
        }
    }

    private fun GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost.drawAttribute(
        attribute: AttributeAPI.Attribute,
        goodRoll: Boolean,
        index: Int,
    ) {
        val color = if (goodRoll) "§e" else "§b"
        val attributeString = color + attribute.type.shortName
        val attributeWidth = attributeString.width()
        val attributeX = x + attributeWidth + (if (index == 1) 16 - attributeWidth * SCALE else 0).toInt()
        val attributeY = y
        drawSlotText(attributeX, attributeY, attributeString, SCALE)

        val levelString = "§a${attribute.level}"
        val levelWidth = levelString.width()
        val levelX = x + levelWidth + (if (index == 1) 16 - levelWidth * SCALE else 0).toInt()
        val levelY = y + (10 * SCALE).toInt()
        drawSlotText(levelX, levelY, levelString, SCALE)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
