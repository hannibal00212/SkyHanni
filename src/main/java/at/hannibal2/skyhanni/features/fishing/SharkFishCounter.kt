package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.renderString

@SkyHanniModule
object SharkFishCounter {

    private var counter = mutableListOf(0, 0, 0, 0)
    private var display = ""
    private var hasWaterRodInHand = false

    @HandleEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        if (!SkyHanniMod.feature.fishing.sharkFishCounter) return

        val name = event.seaCreature.name
        if (!name.contains("Shark")) return
        counter[sharkIndex(name)] += if (event.doubleHook) 2 else 1
        display = "§7Sharks caught: §e${
            counter.sum().addSeparators()
        } §7(§a${counter[0]} §9${counter[1]} §5${counter[2]} §6${counter[3]}§7)"
    }

    private fun sharkIndex(name: String): Int = when {
        name.contains("Blue") -> 1
        name.contains("Tiger") -> 2
        name.contains("Great") -> 3
        else -> 0
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: SkyHanniTickEvent) {
        if (!SkyHanniMod.feature.fishing.sharkFishCounter) return

        if (event.isMod(10)) {
            hasWaterRodInHand = isWaterFishingRod()
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (event.message != "§b§lFISHING FESTIVAL §r§eThe festival has concluded! Time to dry off and repair your rods!") return
        val count = counter.sum()
        if (count == 0) return

        val n = counter[0] // Nurse
        val b = counter[1] // Blue
        val t = counter[2] // Tiger
        val g = counter[3] // Great White
        val total = count.addSeparators()
        val funnyComment = funnyComment(count)
        ChatUtils.chat("You caught $total §f(§a$n §9$b §5$t §6$g§f) §esharks during this fishing festival. $funnyComment")
        counter = mutableListOf(0, 0, 0, 0)
        display = ""
    }

    private fun funnyComment(count: Int): String = when {
        count < 50 -> "Well done!"
        count < 100 -> "Nice!"
        count < 150 -> "Really nice!"
        count < 200 -> "Super cool!"
        count < 250 -> "Mega cool!"
        count < 350 -> "Like a pro!"
        else -> "How???"
    }

    private fun isWaterFishingRod() = FishingApi.isFishing() && !FishingApi.holdingLavaRod

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!SkyHanniMod.feature.fishing.sharkFishCounter) return
        if (!hasWaterRodInHand) return

        SkyHanniMod.feature.fishing.sharkFishCounterPos.renderString(display, posLabel = "Shark Fish Counter")
    }
}
