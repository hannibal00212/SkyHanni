package at.hannibal2.skyhanni.features.gui

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import java.util.regex.Pattern

enum class TabWidgetDisplay(
    private val configName: String?,
    vararg val widgets: TabWidget,
    var expectedLinePattern: Pattern? = null, // Todo: Remove when TabWidget data is no longer bad
) {
    SOULFLOW(null, TabWidget.SOULFLOW),
    COINS("Bank and Interest", TabWidget.BANK, TabWidget.INTEREST),
    SB_LEVEL("Skyblock Level", TabWidget.SB_LEVEL),
    PROFILE(null, TabWidget.PROFILE),
    PLAYER_LIST("Players", TabWidget.PLAYER_LIST),
    PET(null, TabWidget.PET),
    PET_TRAINING("Pet Upgrade Info", TabWidget.PET_SITTER, TabWidget.PET_TRAINING),
    STATS(null, TabWidget.STATS, TabWidget.DUNGEON_SKILLS_AND_STATS),
    DUNGEON_TEAM("Dungeon Info about every person", TabWidget.DUNGEON_PARTY),
    DUNGEON_PUZZLE("Dungeon Info about puzzles", TabWidget.DUNGEON_PUZZLE),
    DUNGEON_OVERALL("Dungeon General Info (very long)", TabWidget.DUNGEON_STATS),
    BESTIARY(null, TabWidget.BESTIARY),
    DRAGON("Dragon Fight Info", TabWidget.DRAGON),
    PROTECTOR("Protector State", TabWidget.PROTECTOR),
    SHEN_RIFT("Shen's Auction inside the Rift", TabWidget.RIFT_SHEN),
    MINION("Minion Info", TabWidget.MINION),
    COLLECTION(null, TabWidget.COLLECTION),
    TIMERS(null, TabWidget.TIMERS),
    FIRE_SALE(null, TabWidget.FIRE_SALE),
    RAIN("Park Rain", TabWidget.RAIN),
    PEST_TRAPS("Pest Traps", TabWidget.PEST_TRAPS),
    FULL_PROFILE_WIDGET(
        "Profile Widget",
        TabWidget.PROFILE,
        TabWidget.SB_LEVEL,
        TabWidget.BANK,
        TabWidget.INTEREST,
        TabWidget.SOULFLOW,
        TabWidget.FAIRY_SOULS,
    )
    ;

    val position get() = config.displayPositions[ordinal]

    override fun toString(): String {
        return configName ?: name.lowercase().allLettersFirstUppercase()
    }

    @SkyHanniModule
    companion object {

        private val patternGroup = RepoPattern.group("tabwidget")
        private val config get() = SkyHanniMod.feature.gui.tabWidget
        private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

        // Todo: Remove when TabWidget data is no longer bad
        // <editor-fold desc="Patterns">
        /**
         * REGEX-TEST: §6§lBestiary:
         * REGEX-TEST:  Crypt Ghoul 15§r§f: §r§b§lMAX
         * REGEX-TEST:  §r§6Golden Ghoul 13§r§f: §r§b2,409/3,000
         * REGEX-TEST:  Old Wolf 10§r§f: §r§b332/600
         * REGEX-TEST:  Wolf 7§r§f: §r§b834/1,400
         * REGEX-FAIL: §8[§d326§8] §boBlazin §6✿
         */
        private val bestiaryLinePattern by patternGroup.pattern(
            "lines.bestiary",
            "§6§lBestiary:| .*: (?:§.)+(?:MAX|[\\d,]+\\/[\\d,]+)"
        )
        // </editor-fold>

        @HandleEvent
        fun onRepoReload(event: RepositoryReloadEvent) {
            BESTIARY.expectedLinePattern = bestiaryLinePattern
        }

        @HandleEvent
        fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
            if (!isEnabled()) return
            if (config?.displayPositions == null) return
            config.display.forEach { widget ->
                widget.position.renderStrings(
                    widget.widgets.flatMap { subWidget ->
                        subWidget.lines.filter { line ->
                            // Todo: Remove when TabWidget data is no longer bad
                            widget.expectedLinePattern?.matches(line) ?: true
                        }
                    },
                    posLabel = "Display Widget: ${widget.name}",
                )
            }
        }

        @HandleEvent
        fun onJoin(event: ProfileJoinEvent) {
            // Validation that the displayPositions in the config is correct
            val sizeDiff = TabWidgetDisplay.entries.size - config.displayPositions.size
            if (sizeDiff == 0) return
            if (sizeDiff < 0) {
                ErrorManager.skyHanniError(
                    "Invalid State of config.displayPositions",
                    "Display" to TabWidgetDisplay.entries,
                    "Positions" to config.displayPositions,
                )
            } else {
                config.displayPositions.addAll(generateSequence { Position() }.take(sizeDiff))
            }
        }
    }
}
