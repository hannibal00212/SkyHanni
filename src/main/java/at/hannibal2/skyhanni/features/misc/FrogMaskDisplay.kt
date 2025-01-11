package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchAll
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FrogMaskDisplay {

    private val config get() = SkyHanniMod.feature.misc

    private var display: Renderable? = null

    private var currentRegion: String? = null
    private var timeRemaining = SimpleTimeMark.farPast()

    private val patternGroup = RepoPattern.group("misc.frogmask")

    /**
     * REGEX-TEST: §7Today's region: §aDark Thicket
     */
    private val activeRegionPattern by patternGroup.pattern(
        "description.active",
        "§7Today's region: (?<region>.+)",
    )

    private val frogMask by lazy { "FROG_MASK".toInternalName().getItemStack() }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled() && display != null) return

        config.frogMaskDisplayPos.renderRenderable(display, posLabel = "Frog Mask Display")
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (timeRemaining.isInFuture()) return

        val helmet = InventoryUtils.getHelmet() ?: return
        if (helmet.getInternalName() != "FROG_MASK".toInternalName()) return

        activeRegionPattern.matchAll(helmet.getLore()) {
            currentRegion = group("region")
            val now = SkyBlockTime.now()
            timeRemaining = SkyBlockTime(year = now.year, month = now.month, day = now.day + 1).asTimeMark()
        } ?: run {
            currentRegion = null
            timeRemaining = SimpleTimeMark.farPast()
        }

        display = if (currentRegion != null) updateDisplay() else null
    }

    private fun updateDisplay(): Renderable {
        val until = timeRemaining.timeUntil()
        val timeString = until.format()

        return Renderable.horizontalContainer(
            listOf(
                Renderable.itemStack(frogMask),
                Renderable.string(
                    "§5Frog Mask§6 - $currentRegion §6for §e$timeString",
                ),
            ),
            spacing = 1,
            verticalAlign = RenderUtils.VerticalAlignment.CENTER,
        )
    }

    private fun isEnabled() = IslandType.THE_PARK.isInIsland() && config.frogMaskDisplay
}
