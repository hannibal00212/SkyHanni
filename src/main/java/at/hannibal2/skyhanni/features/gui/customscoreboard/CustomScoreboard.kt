/**
 * TODO LIST
 *  - countdown events like fishing festival + fiesta when its not on tablist
 *  - improve hide coin difference to also work with bits, motes, etc
 *  - choose the amount of decimal places in shorten nums
 *  - heavily optimize elements and events by only updating them when absolutely needed
 */

package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiPositionMovedEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine.Companion.align
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElementTitle
import at.hannibal2.skyhanni.features.gui.customscoreboard.events.ScoreboardEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.takeIfNotEmpty
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun.runDelayed
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CustomScoreboard {

    private var display: Renderable? = null
    private var cache: Renderable? = null

    private var currentIslandEntries = listOf<ScoreboardElement>()
    var currentIslandEvents = listOf<ScoreboardEvent>()
        private set

    private const val GUI_NAME = "Custom Scoreboard"

    private var nextScoreboardUpdate = SimpleTimeMark.farFuture()

    private var dirty = false

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        display ?: return

        val render =
            if (LorenzUtils.inSkyBlock && !TabListData.fullyLoaded && displayConfig.cacheScoreboardOnIslandSwitch && cache != null) cache
            else display

        render ?: return

        // We want to update the background every time, so we can have a smooth transition when using chroma as the color
        val finalRenderable = RenderBackground.addBackground(render)

        RenderBackground.updatePosition(finalRenderable)

        config.position.renderRenderable(finalRenderable, posLabel = GUI_NAME)
    }

    @SubscribeEvent
    fun onGuiPositionMoved(event: GuiPositionMovedEvent) {
        if (event.guiName == GUI_NAME) {
            with(alignmentConfig) {
                if (horizontalAlignment != HorizontalAlignment.DONT_ALIGN || verticalAlignment != VerticalAlignment.DONT_ALIGN) {
                    val tempHori = horizontalAlignment
                    val tempVert = verticalAlignment

                    horizontalAlignment = HorizontalAlignment.DONT_ALIGN
                    verticalAlignment = VerticalAlignment.DONT_ALIGN
                    ChatUtils.clickableChat(
                        "Disabled Custom Scoreboard auto-alignment. Click here to undo this action!",
                        oneTimeClick = true,
                        onClick = {
                            horizontalAlignment = tempHori
                            verticalAlignment = tempVert
                            ChatUtils.chat("Enabled Custom Scoreboard auto-alignment.")
                        },
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return

        if (dirty || nextScoreboardUpdate.isInPast()) {
            nextScoreboardUpdate = 250.milliseconds.fromNow()
            dirty = false
            display = createLines().removeEmptyLinesFromEdges().createRenderable()
            if (TabListData.fullyLoaded) {
                cache = display
            }
        }

        // Remove Known Lines, so we can get the unknown ones
        if (LorenzUtils.inSkyBlock && displayConfig.useCustomLines) UnknownLinesHandler.handleUnknownLines()
    }

    @SubscribeEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        dirty = true
    }

    // TODO move those into their respective classes and make them private
    internal val config get() = SkyHanniMod.feature.gui.customScoreboard
    internal val displayConfig get() = config.display
    internal val alignmentConfig get() = displayConfig.alignment
    internal val arrowConfig get() = displayConfig.arrow
    internal val chunkedConfig get() = displayConfig.chunkedStats
    internal val eventsConfig get() = displayConfig.events
    internal val mayorConfig get() = displayConfig.mayor
    internal val partyConfig get() = displayConfig.party
    internal val maxwellConfig get() = displayConfig.maxwell
    internal val informationFilteringConfig get() = config.informationFiltering
    internal val backgroundConfig get() = config.background

    private fun createLines() = when {
        !LorenzUtils.inSkyBlock -> addAllNonSkyBlockLines()
        !displayConfig.useCustomLines -> addDefaultSkyBlockLines()
        else -> addCustomSkyBlockLines()
    }

    private fun addAllNonSkyBlockLines() = buildList {
        addAll(ScoreboardElementTitle.getLines())
        addAll(ScoreboardData.sidebarLinesFormatted.map { it.align() })
    }

    private fun addDefaultSkyBlockLines() = buildList {
        add(ScoreboardData.objectiveTitle align displayConfig.titleAndFooter.alignTitle)
        addAll(ScoreboardData.sidebarLinesFormatted.map { it.align() })
    }

    private fun addCustomSkyBlockLines() = buildList<ScoreboardLine> {
        for (element in currentIslandEntries) {
            val lines = element.getLines()
            if (lines.isEmpty()) continue

            if (
                informationFilteringConfig.hideConsecutiveEmptyLines &&
                lines.first().display.isEmpty() &&
                lastOrNull()?.display?.isEmpty() == true
            ) {
                continue
            }

            addAll(lines)
        }
    }

    private fun List<ScoreboardLine>.createRenderable() = Renderable.verticalContainer(
        map { Renderable.string(it.display, horizontalAlign = it.alignment) },
        displayConfig.lineSpacing - 10,
        horizontalAlign = HorizontalAlignment.CENTER,
        verticalAlign = VerticalAlignment.CENTER,
    )

    private fun List<ScoreboardLine>.removeEmptyLinesFromEdges(): List<ScoreboardLine> =
        takeIf { !informationFilteringConfig.hideEmptyLinesAtTopAndBottom }
            ?: dropWhile { it.display.isBlank() }.dropLastWhile { it.display.isBlank() }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(
            config.scoreboardEntries,
            eventsConfig.eventEntries,
        ) {
            updateIslandEntries()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        runDelayed(2.seconds) {
            if (!LorenzUtils.inSkyBlock || !(LorenzUtils.onHypixel && OutsideSbFeature.CUSTOM_SCOREBOARD.isSelected())) dirty = true
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        updateIslandEntries()
    }

    private fun updateIslandEntries() {
        currentIslandEntries = config.scoreboardEntries.get().map { it.element }.filter { it.showIsland() }
        currentIslandEvents = eventsConfig.eventEntries.get().map { it.event }.filter { it.showIsland() }
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Custom Scoreboard")
        event.addIrrelevant {
            if (!config.enabled.get()) {
                add("Custom Scoreboard disabled.")
            } else {
                add("Custom Scoreboard Lines:")
                ScoreboardConfigElement.entries.forEach { entry ->
                    add(
                        "   ${entry.name.firstLetterUppercase()} - " +
                            "island: ${entry.element.showIsland()} - " +
                            "show: ${entry.element.showWhen()} - " +
                            "${entry.element.getLines().map { it.display }}",
                    )
                }
                allUnknownLines.takeIfNotEmpty()?.let { set ->
                    add("Recent Unknown Lines:")
                    set.forEach { add("   ${it.line}") }
                }
            }
        }
    }

    @JvmStatic
    fun resetAppearance() {
        with(config.scoreboardEntries) {
            get().clear()
            get().addAll(ScoreboardConfigElement.defaultOptions)
            notifyObservers()
        }
    }

    private fun isEnabled() =
        (LorenzUtils.inSkyBlock || (OutsideSbFeature.CUSTOM_SCOREBOARD.isSelected() && LorenzUtils.onHypixel)) && config.enabled.get()

    @JvmStatic
    fun isHideVanillaScoreboardEnabled() = isEnabled() && displayConfig.hideVanillaScoreboard.get()
}
