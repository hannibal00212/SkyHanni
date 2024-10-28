package at.hannibal2.skyhanni.features.gui

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

enum class TabWidgetDisplay(private val configName: String?, vararg val widgets: TabWidget) {
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
    ;

    val position get() = config.displayPositions[ordinal]

    override fun toString(): String {
        return configName ?: name.lowercase().allLettersFirstUppercase()
    }

    @SkyHanniModule
    companion object {

        private val config get() = SkyHanniMod.feature.gui.tabWidget

        private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

        @SubscribeEvent
        fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
            if (!isEnabled()) return
            if (config?.displayPositions == null) return
            config.display.forEach { widget ->
                widget.position.renderStrings(
                    widget.widgets.flatMap { it.lines },
                    posLabel = "Display Widget: ${widget.name}",
                )
            }
        }

        @SubscribeEvent
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
                config.displayPositions.addAll(List(sizeDiff) { Position(0, 0) })
            }
        }
    }
}
