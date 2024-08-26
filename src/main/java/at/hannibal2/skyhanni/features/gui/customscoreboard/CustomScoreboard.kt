/**
 * TODO LIST
 *  - Bank API (actually maybe not, I like the current design)
 *  - countdown events like fishing festival + fiesta when its not on tablist
 *  - improve hide coin difference to also work with bits, motes, etc
 *  - color options in the purse etc lines
 *  - choose the amount of decimal places in shorten nums
 *  - heavily optimize elements and events by only updating them when absolutely needed
 */

package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.AlignmentConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.ArrowConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.BackgroundConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.ChunkedStatsConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.CustomScoreboardConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.DisplayConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.EventsConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.InformationFilteringConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.MaxwellConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.MayorConfig
import at.hannibal2.skyhanni.config.features.gui.customscoreboard.PartyConfig
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiPositionMovedEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.features.gui.customscoreboard.ScoreboardLine.Companion.align
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.Footer
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.ScoreboardElement
import at.hannibal2.skyhanni.features.gui.customscoreboard.elements.Title
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
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CustomScoreboard {

    private var display = listOf<ScoreboardLine>()
    private var cache = listOf<ScoreboardLine>()

    private var currentIslandEntries = listOf<ScoreboardElement>()
    var currentIslandEvents = listOf<ScoreboardEvent>()
        private set

    private const val GUI_NAME = "Custom Scoreboard"

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (display.isEmpty()) return

        val render = if (!TabListData.fullyLoaded && displayConfig.cacheScoreboardOnIslandSwitch && cache.isNotEmpty()) cache
        else display

        val textRenderable = Renderable.verticalContainer(
            render.map { Renderable.string(it.display, horizontalAlign = it.alignment) },
            displayConfig.lineSpacing - 10,
            horizontalAlign = HorizontalAlignment.CENTER,
            verticalAlign = VerticalAlignment.CENTER,
        )

        val finalRenderable = RenderBackground.addBackground(textRenderable)

        RenderBackground.updatePosition(finalRenderable)

        config.position.renderRenderable(finalRenderable, posLabel = GUI_NAME)
    }

    @SubscribeEvent
    fun onGuiPositionMoved(event: GuiPositionMovedEvent) {
        if (event.guiName == GUI_NAME) {
            with(alignmentConfig) {
                if (horizontalAlignment != HorizontalAlignment.DONT_ALIGN ||
                    verticalAlignment != VerticalAlignment.DONT_ALIGN
                ) {
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

        // Creating the lines
        if (event.isMod(5)) {
            display = createLines().removeEmptyLinesFromEdges()
            if (TabListData.fullyLoaded) {
                cache = display.toList()
            }
        }

        // Remove Known Lines, so we can get the unknown ones
        if (LorenzUtils.inSkyBlock && displayConfig.useCustomLines) UnknownLinesHandler.handleUnknownLines()
    }

    val config: CustomScoreboardConfig get() = SkyHanniMod.feature.gui.customScoreboard
    val displayConfig: DisplayConfig get() = config.display
    val alignmentConfig: AlignmentConfig get() = displayConfig.alignment
    val arrowConfig: ArrowConfig get() = displayConfig.arrow
    val chunkedConfig: ChunkedStatsConfig get() = displayConfig.chunkedStats
    val eventsConfig: EventsConfig get() = displayConfig.events
    val mayorConfig: MayorConfig get() = displayConfig.mayor
    val partyConfig: PartyConfig get() = displayConfig.party
    val maxwellConfig: MaxwellConfig get() = displayConfig.maxwell
    val informationFilteringConfig: InformationFilteringConfig get() = config.informationFiltering
    val backgroundConfig: BackgroundConfig get() = config.background

    private fun createLines() = when {
        !LorenzUtils.inSkyBlock -> addAllNonSkyBlockLines()
        !displayConfig.useCustomLines -> addDefaultSkyBlockLines()
        else -> addCustomSkyBlockLines()
    }

    private fun addAllNonSkyBlockLines() = buildList {
        addAll(Title.getLines())
        addAll(ScoreboardData.sidebarLinesFormatted.dropLast(1).map { it.align() })
        addAll(Footer.getLines())
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

    private fun List<ScoreboardLine>.removeEmptyLinesFromEdges(): List<ScoreboardLine> =
        takeIf { !informationFilteringConfig.hideEmptyLinesAtTopAndBottom }
            ?: dropWhile { it.display.isBlank() }.dropLastWhile { it.display.isBlank() }

    private var dirty = false

    // The ElementType for the Vanilla Scoreboard is called HELMET
    // Thanks to APEC for showing this
    @SubscribeEvent
    fun onRenderScoreboard(event: RenderGameOverlayEvent.Post) {
        if (event.type == RenderGameOverlayEvent.ElementType.HELMET) {
            if (isHideVanillaScoreboardEnabled()) {
                GuiIngameForge.renderObjective = false
            }
            if (dirty) {
                GuiIngameForge.renderObjective = true
                dirty = false
            }
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(
            config.enabled,
            displayConfig.hideVanillaScoreboard,
            SkyHanniMod.feature.misc.showOutsideSB,
        ) {
            if (!isHideVanillaScoreboardEnabled()) dirty = true
        }
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
            if (!LorenzUtils.inSkyBlock && !OutsideSbFeature.CUSTOM_SCOREBOARD.isSelected()) dirty = true
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
                ScoreboardEntry.entries.forEach { entry ->
                    add(
                        "   ${entry.name.firstLetterUppercase()} - " +
                            "island: ${entry.element.showIsland()} - " +
                            "show: ${entry.element.showWhen()} - " +
                            "${entry.element.getLines().map { it.display }}",
                    )
                }
                pastUnknownLines.toSet().takeIfNotEmpty()?.let { set ->
                    add("Recent Unknown Lines:")
                    set.forEach { add("   $it") }
                }
            }
        }
    }

    private fun isEnabled() = (LorenzUtils.inSkyBlock || OutsideSbFeature.CUSTOM_SCOREBOARD.isSelected()) && config.enabled.get()

    private fun isHideVanillaScoreboardEnabled() = isEnabled() && displayConfig.hideVanillaScoreboard.get()
}
