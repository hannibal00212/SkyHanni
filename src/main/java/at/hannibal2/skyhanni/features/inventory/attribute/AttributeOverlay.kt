package at.hannibal2.skyhanni.features.inventory.attribute

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiRenderItemEvent
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeAPI.getAttributesLevels
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeAPI.getRollType
import at.hannibal2.skyhanni.features.inventory.attribute.AttributeAPI.isPartialRoll
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.StringUtils.width
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object AttributeOverlay {

    private const val SCALE = 0.5714286f
    private const val VERTICAL_OFFSET = (10 * SCALE).toInt()

    private val config get() = SkyHanniMod.feature.inventory.attributeOverlay

    private data class CachedData(
        val attributes: List<Attribute>,
        val rollType: RollType,
    )

    private val cachedData = TimeLimitedCache<ItemStack, CachedData>(5.seconds)

    @SubscribeEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost) {
        if (!isEnabled()) return

        val stack = event.stack ?: return
        val (attributes, rollType) = cachedData.getOrPut(stack) {
            val attributes = stack.getAttributesLevels() ?: return
            val internalName = stack.getInternalNameOrNull() ?: return

            val rollType = attributes.getRollType(internalName)
            val list = attributes.toList().filter { shouldHighlight(it, internalName, rollType) }
            CachedData(list, rollType)
        }
        attributes.forEachIndexed { index, attribute ->
            event.drawAttribute(attribute, rollType, index)
        }
    }

    private fun shouldHighlight(
        attribute: Attribute,
        internalName: NEUInternalName,
        rollType: RollType,
    ): Boolean {
        val (type, level) = attribute
        val isGood = rollType != RollType.BAD_ROLL
        if (level <= config.minimumLevel && isGood && !config.goodRollsOverrideLevel) return false

        val show = (config.ignoreList && isGood) || type in config.attributesList
        if (!show) return false
        return when {
            rollType == RollType.PARTIAL_ROLL && config.highlightGoodAttributes && attribute.type.isPartialRoll(internalName) -> true
            rollType == RollType.GOOD_ROLL -> config.highlightGoodRolls
            else -> !config.hideNonGoodRolls
        }
    }

    private fun GuiRenderItemEvent.RenderOverlayEvent.GuiRenderItemPost.drawAttribute(
        attribute: Attribute,
        rollType: RollType,
        index: Int,
    ) {
        val color = when {
            rollType == RollType.GOOD_ROLL && config.highlightGoodRolls -> "§e"
            rollType == RollType.PARTIAL_ROLL && config.highlightGoodAttributes -> "§a"
            else -> "§b"
        }
        val attributeString = color + attribute.type.shortName
        val attributeWidth = attributeString.width()
        val attributeX = x + attributeWidth + if (index == 1) 16 - (attributeWidth * SCALE).toInt() else 0
        val attributeY = y
        drawSlotText(attributeX, attributeY, attributeString, SCALE)

        val levelString = "§a${attribute.level}"
        val levelWidth = levelString.width()
        val levelX = x + levelWidth + if (index == 1) 16 - (levelWidth * SCALE).toInt() else 0
        val levelY = y + VERTICAL_OFFSET
        drawSlotText(levelX, levelY, levelString, SCALE)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
