//
// TODO LIST
// V2 RELEASE
//  - Bank API (actually maybe not, I like the current design)
//  - countdown events like fishing festival + fiesta when its not on tablist
//  - improve hide coin difference to also work with bits, motes, etc
//  - color options in the purse etc lines
//  - choose the amount of decimal places in shorten nums
//

package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.enums.OutsideSbFeature
import at.hannibal2.skyhanni.data.ScoreboardData
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiPositionMovedEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun.runDelayed
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.HorizontalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.VerticalAlignment
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

typealias ScoreboardElementType = Pair<String, HorizontalAlignment>

@SkyHanniModule
object CustomScoreboard {

    private var display = emptyList<ScoreboardElementType>()
    private var cache = emptyList<ScoreboardElementType>()
    private val guiName = "Custom Scoreboard"

    // Cached scoreboard data, only update after no change for 300ms
    var activeLines = emptyList<String>()

    // Most recent scoreboard state, not in use until cached
    private var mostRecentLines = emptyList<String>()
    private var lastScoreboardUpdate = SimpleTimeMark.farFuture()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (display.isEmpty()) return

        val render =
            if (LorenzUtils.inSkyBlock && !TabListData.fullyLoaded && displayConfig.cacheScoreboardOnIslandSwitch && cache.isNotEmpty()) {
                cache
            } else {
                display
            }

        val textRenderable = Renderable.verticalContainer(
            render.map { Renderable.string(it.first, horizontalAlign = it.second) },
            displayConfig.lineSpacing - 10,
            horizontalAlign = HorizontalAlignment.CENTER,
            verticalAlign = VerticalAlignment.CENTER,
        )

        val finalRenderable = RenderBackground.addBackground(textRenderable)

        RenderBackground.updatePosition(finalRenderable)

        config.position.renderRenderable(finalRenderable, posLabel = guiName)
    }

    @SubscribeEvent
    fun onGuiPositionMoved(event: GuiPositionMovedEvent) {
        if (event.guiName == guiName) {
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

        // We want to update the scoreboard as soon as we have new data, not 5 ticks delayed
        var dirty = false
        if (lastScoreboardUpdate.passedSince() > 300.milliseconds) {
            activeLines = mostRecentLines
            lastScoreboardUpdate = SimpleTimeMark.farFuture()
            dirty = true
        }

        // Creating the lines
        if (event.isMod(5) || dirty) {
            display = createLines().removeEmptyLinesFromEdges()
            if (TabListData.fullyLoaded) {
                cache = display.toList()
            }
        }

        // Remove Known Lines, so we can get the unknown ones
        if (LorenzUtils.inSkyBlock && displayConfig.useCustomLines) UnknownLinesHandler.handleUnknownLines()
    }

    @SubscribeEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        mostRecentLines = event.scoreboard
        lastScoreboardUpdate = SimpleTimeMark.now()
    }


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
        addAll(ScoreboardElement.TITLE.getVisiblePair())
        addAll(activeLines.map { it to HorizontalAlignment.LEFT })
    }

    private fun addDefaultSkyBlockLines() = buildList {
        add(ScoreboardData.objectiveTitle to displayConfig.titleAndFooter.alignTitleAndFooter)
        addAll(activeLines.map { it to HorizontalAlignment.LEFT })
    }

    private fun addCustomSkyBlockLines() = buildList<ScoreboardElementType> {
        for (element in config.scoreboardEntries) {
            val lines = element.getVisiblePair()
            if (lines.isEmpty()) continue

            if (
                informationFilteringConfig.hideConsecutiveEmptyLines &&
                lines.first().first == "<empty>" && lastOrNull()?.first?.isEmpty() == true
            ) {
                continue
            }

            if (lines.first().first == "<empty>") {
                add("" to HorizontalAlignment.LEFT)
                continue
            }

            if (lines.any { it.first == "<hidden>" }) {
                continue
            }

            addAll(lines)
        }
    }

    private fun List<ScoreboardElementType>.removeEmptyLinesFromEdges(): List<ScoreboardElementType> {
        if (config.informationFiltering.hideEmptyLinesAtTopAndBottom) {
            return this
                .dropWhile { it.first.isEmpty() }
                .dropLastWhile { it.first.isEmpty() }
        }
        return this
    }

    private var dirty = false

    // Thank you Apec for showing that the ElementType of the stupid scoreboard is FUCKING HELMET WTF
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
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        runDelayed(2.seconds) {
            if (!LorenzUtils.inSkyBlock || !(LorenzUtils.onHypixel && OutsideSbFeature.CUSTOM_SCOREBOARD.isSelected())) dirty = true
        }
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Custom Scoreboard")
        event.addIrrelevant {
            if (!config.enabled.get()) {
                add("Custom Scoreboard disabled.")
            } else {
                ScoreboardElement.entries.map { element ->
                    add(
                        "${element.name.firstLetterUppercase()} - " +
                            "${element.showWhen.invoke()} - " +
                            "${element.getVisiblePair().map { it.first }}",
                    )
                }
            }
        }
    }

    private fun isEnabled() =
        (LorenzUtils.inSkyBlock || (OutsideSbFeature.CUSTOM_SCOREBOARD.isSelected() && LorenzUtils.onHypixel)) && config.enabled.get()

    private fun isHideVanillaScoreboardEnabled() = isEnabled() && displayConfig.hideVanillaScoreboard.get()

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        val prefix = "gui.customScoreboard"
        val displayConfigPrefix = "$prefix.displayConfig"
        val displayPrefix = "$prefix.display"

        event.move(
            28,
            "$prefix.displayConfig.showAllActiveEvents",
            "$prefix.displayConfig.eventsConfig.showAllActiveEvents",
        )

        event.move(31, "$displayConfigPrefix.arrowAmountDisplay", "$displayPrefix.arrow.amountDisplay")
        event.move(31, "$displayConfigPrefix.colorArrowAmount", "$displayPrefix.arrow.colorArrowAmount")
        event.move(31, "$displayConfigPrefix.showMagicalPower", "$displayPrefix.maxwell.showMagicalPower")
        event.move(31, "$displayConfigPrefix.compactTuning", "$displayPrefix.maxwell.compactTuning")
        event.move(31, "$displayConfigPrefix.tuningAmount", "$displayPrefix.maxwell.tuningAmount")
        event.move(31, "$displayConfigPrefix.hideVanillaScoreboard", "$displayPrefix.hideVanillaScoreboard")
        event.move(31, "$displayConfigPrefix.displayNumbersFirst", "$displayPrefix.displayNumbersFirst")
        event.move(31, "$displayConfigPrefix.showUnclaimedBits", "$displayPrefix.showUnclaimedBits")
        event.move(31, "$displayConfigPrefix.showMaxIslandPlayers", "$displayPrefix.showMaxIslandPlayers")
        event.move(31, "$displayConfigPrefix.numberFormat", "$displayPrefix.numberFormat")
        event.move(31, "$displayConfigPrefix.lineSpacing", "$displayPrefix.lineSpacing")
        event.move(
            31,
            "$displayConfigPrefix.cacheScoreboardOnIslandSwitch",
            "$displayPrefix.cacheScoreboardOnIslandSwitch",
        )
        // Categories
        event.move(31, "$displayConfigPrefix.alignment", "$displayPrefix.alignment")
        event.move(31, "$displayConfigPrefix.titleAndFooter", "$displayPrefix.titleAndFooter")
        event.move(31, "$prefix.backgroundConfig", "$prefix.background")
        event.move(31, "$prefix.informationFilteringConfig", "$prefix.informationFiltering")
        event.move(31, "$displayConfigPrefix.eventsConfig", "$displayPrefix.events")
        event.move(31, "$prefix.mayorConfig", "$displayPrefix.mayor")
        event.move(31, "$prefix.partyConfig", "$displayPrefix.party")

        event.transform(37, "$displayPrefix.events.eventEntries") { element ->
            val array = element.asJsonArray
            array.add(JsonPrimitive(ScoreboardEvent.QUEUE.name))
            array
        }
        event.transform(40, "$displayPrefix.events.eventEntries") { element ->
            val jsonArray = element.asJsonArray
            val newArray = JsonArray()

            for (jsonElement in jsonArray) {
                val stringValue = jsonElement.asString
                if (stringValue !in listOf("HOT_DOG_CONTEST", "EFFIGIES")) {
                    newArray.add(jsonElement)
                }
            }

            if (jsonArray.any { it.asString in listOf("HOT_DOG_CONTEST", "EFFIGIES") }) {
                newArray.add(JsonPrimitive(ScoreboardEvent.RIFT.name))
            }

            newArray
        }

        event.move(43, "$displayPrefix.alignment.alignRight", "$displayPrefix.alignment.horizontalAlignment") {
            JsonPrimitive(
                if (it.asBoolean) {
                    HorizontalAlignment.RIGHT.name
                } else {
                    HorizontalAlignment.DONT_ALIGN.name
                },
            )
        }
        event.move(43, "$displayPrefix.alignment.alignCenterVertically", "$displayPrefix.alignment.verticalAlignment") {
            JsonPrimitive(
                if (it.asBoolean) {
                    VerticalAlignment.CENTER.name
                } else {
                    VerticalAlignment.DONT_ALIGN.name
                },
            )
        }
        event.transform(50, "$displayPrefix.events.eventEntries") { element ->
            val array = element.asJsonArray
            array.add(JsonPrimitive(ScoreboardEvent.ANNIVERSARY.name))
            array.add(JsonPrimitive(ScoreboardEvent.CARNIVAL.name))
            array
        }
        event.transform(51, "$displayPrefix.events.eventEntries") { element ->
            val array = element.asJsonArray
            array.add(JsonPrimitive(ScoreboardEvent.NEW_YEAR.name))
            array
        }
        event.move(
            57,
            "$displayPrefix.titleAndFooter.useHypixelTitleAnimation",
            "$displayPrefix.titleAndFooter.useCustomTitle",
        ) {
            JsonPrimitive(!it.asBoolean)
        }
    }
}
