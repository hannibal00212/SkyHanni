package at.hannibal2.skyhanni.features.itemabilities

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChickenHeadTimer {
    private val config get() = SkyHanniMod.feature.inventory.itemAbilities.chickenHead

    private var hasChickenHead = false
    private var lastTime = SimpleTimeMark.farPast()
    private val cooldown = 5.seconds

    private val chickenHead = "CHICKEN_HEAD".toInternalName()

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(5)) return

        hasChickenHead = InventoryUtils.getHelmet()?.getInternalName() == chickenHead
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        if (!hasChickenHead) return
        if (event.message == "§aYou laid an egg!") {
            lastTime = SimpleTimeMark.now()
            if (config.hideChat) {
                event.blockedReason = "chicken_head_timer"
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!hasChickenHead) return

        val remainingTime = cooldown - lastTime.passedSince()
        val displayText = if (remainingTime.isNegative()) {
            "Chicken Head Timer: §aNow"
        } else {
            val formatDuration = remainingTime.format()
            "Chicken Head Timer: §b$formatDuration"
        }

        config.position.renderString(displayText, posLabel = "Chicken Head Timer")
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.chickenHeadTimerHideChat", "itemAbilities.chickenHead.hideChat")
        event.move(2, "misc.chickenHeadTimerPosition", "itemAbilities.chickenHead.position")
        event.move(2, "misc.chickenHeadTimerDisplay", "itemAbilities.chickenHead.displayTimer")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.displayTimer
}
