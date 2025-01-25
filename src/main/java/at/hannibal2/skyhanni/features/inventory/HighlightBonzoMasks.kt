package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.interpolate
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * @author Linnea Gräf
 */
@SkyHanniModule
object HighlightBonzoMasks {

    private val config get() = SkyHanniMod.feature.inventory.itemAbilities

    private val maskTimers = mutableMapOf<MaskType, SimpleTimeMark>()

    private val patternGroup = RepoPattern.group("inventory.masks.timers")
    private val bonzoMaskPattern by patternGroup.pattern(
        "bonzo",
        "Your Bonzo's Mask saved your life!",
    )
    private val spiritMaskPattern by patternGroup.pattern(
        "spirit",
        "Second Wind Activated! Your Spirit Mask saved your life!",
    )

    private val greenHue = Color.RGBtoHSB(0, 255, 0, null)[0].toDouble()
    private val redHue = Color.RGBtoHSB(255, 0, 0, null)[0].toDouble()

    @HandleEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!config.depletedBonzosMasks) return
        for (slot in event.gui.inventorySlots.inventorySlots) {
            val internalName = slot.stack?.getInternalName() ?: continue
            val maskType = MaskType.getByInternalName(internalName) ?: continue
            val readyAt = maskTimers[maskType] ?: continue

            if (readyAt.isInFuture()) {
                val hue = interpolate(redHue, greenHue, maskType.percentageComplete(readyAt.timeUntil()))
                slot.highlight(Color(Color.HSBtoRGB(hue.toFloat(), 1F, 1F)))
            }
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val message = event.message.removeColor()
        // TODO move pattern into enum
        if (bonzoMaskPattern.matches(message)) {
            maskTimers[MaskType.BONZO_MASK] = SimpleTimeMark.now() + MaskType.BONZO_MASK.cooldown
        } else if (spiritMaskPattern.matches(message)) {
            maskTimers[MaskType.SPIRIT_MASK] = SimpleTimeMark.now() + MaskType.SPIRIT_MASK.cooldown
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        maskTimers.clear()
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "inventory.highlightDepletedBonzosMasks", "itemAbilities.depletedBonzosMasks")
    }

    // This timer is overestimating since mage level affects the cooldown
    private enum class MaskType(val internalNames: List<NeuInternalName>, val cooldown: Duration) {
        BONZO_MASK(listOf("BONZO_MASK".toInternalName(), "STARRED_BONZO_MASK".toInternalName()), 6.minutes),
        SPIRIT_MASK(listOf("SPIRIT_MASK".toInternalName(), "STARRED_SPIRIT_MASK".toInternalName()), 30.seconds),
        ;

        fun percentageComplete(timeUntil: Duration): Double {
            return timeUntil.inWholeMilliseconds / cooldown.inWholeMilliseconds.toDouble()
        }

        companion object {
            fun getByInternalName(internalName: NeuInternalName): MaskType? {
                return entries.firstOrNull { internalName in it.internalNames }
            }
        }
    }
}
