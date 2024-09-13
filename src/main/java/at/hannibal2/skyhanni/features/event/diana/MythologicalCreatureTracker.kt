package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.MayorAPI.getElectionYear
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.CollectionUtils.addSearchString
import at.hannibal2.skyhanni.utils.CollectionUtils.sumAllValues
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.renderables.Searchable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker
import at.hannibal2.skyhanni.utils.tracker.TrackerData
import com.google.gson.annotations.Expose
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

@SkyHanniModule
object MythologicalCreatureTracker {

    private val config get() = SkyHanniMod.feature.event.diana.mythologicalMobtracker

    private val patternGroup = RepoPattern.group("event.diana.mythological.tracker")
    private val minotaurPattern by patternGroup.pattern(
        "minotaur",
        ".* §r§eYou dug out a §r§2Minotaur§r§e!",
    )
    private val gaiaConstructPattern by patternGroup.pattern(
        "gaiaconstruct",
        ".* §r§eYou dug out a §r§2Gaia Construct§r§e!",
    )
    private val minosChampionPattern by patternGroup.pattern(
        "minoschampion",
        ".* §r§eYou dug out a §r§2Minos Champion§r§e!",
    )
    private val siameseLynxesPattern by patternGroup.pattern(
        "siameselynxes",
        ".* §r§eYou dug out §r§2Siamese Lynxes§r§e!",
    )
    private val minosHunterPattern by patternGroup.pattern(
        "minoshunter",
        ".* §r§eYou dug out a §r§2Minos Hunter§r§e!",
    )
    private val minosInquisitorPattern by patternGroup.pattern(
        "minosinquisitor",
        ".* §r§eYou dug out a §r§2Minos Inquisitor§r§e!",
    )

    private val tracker = SkyHanniTracker(
        "Mythological Creature Tracker", { Data() }, { it.diana.mythologicalMobTracker },
        SkyHanniTracker.DisplayMode.MAYOR to {
            it.diana.mythologicalMobTrackerPerElectionSeason.getOrPut(
                SkyBlockTime.now().getElectionYear(), ::Data,
            )
        },
    ) { drawDisplay(it) }

    class Data : TrackerData() {

        override fun reset() {
            count.clear()
            creaturesSinceLastInquisitor = 0
        }

        @Expose
        var creaturesSinceLastInquisitor: Int = 0

        @Expose
        var count: MutableMap<MythologicalCreatureType, Int> = mutableMapOf()
    }

    enum class MythologicalCreatureType(val displayName: String, val pattern: Pattern) {
        MINOTAUR("§2Minotaur", minotaurPattern),
        GAIA_CONSTRUCT("§2Gaia Construct", gaiaConstructPattern),
        MINOS_CHAMPION("§2Minos Champion", minosChampionPattern),
        SIAMESE_LYNXES("§2Siamese Lynxes", siameseLynxesPattern),
        MINOS_HUNTER("§2Minos Hunter", minosHunterPattern),
        MINOS_INQUISITOR("§cMinos Inquisitor", minosInquisitorPattern),
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        for (creatureType in MythologicalCreatureType.entries) {
            if (creatureType.pattern.matches(event.message)) {
                BurrowAPI.lastBurrowRelatedChatMessage = SimpleTimeMark.now()
                tracker.modify {
                    it.count.addOrPut(creatureType, 1)

                    // TODO migrate to abstract feature in the future
                    if (creatureType == MythologicalCreatureType.MINOS_INQUISITOR) {
                        event.chatComponent = ChatComponentText(event.message + " §e(${it.creaturesSinceLastInquisitor})")
                        it.creaturesSinceLastInquisitor = 0
                    } else it.creaturesSinceLastInquisitor++
                }
                if (config.hideChat) event.blockedReason = "mythological_creature_dug"
            }
        }
    }

    private fun drawDisplay(data: Data): List<Searchable> = buildList {
        addSearchString("§7Mythological Creature Tracker:")
        val total = data.count.sumAllValues()
        for ((creatureType, amount) in data.count.entries.sortedByDescending { it.value }) {
            val percentageSuffix = if (config.showPercentage.get()) {
                val percentage = LorenzUtils.formatPercentage(amount.toDouble() / total)
                " §7$percentage"
            } else ""

            addSearchString(
                " §7- §e${amount.addSeparators()} ${creatureType.displayName}$percentageSuffix",
                searchText = creatureType.displayName,
            )
        }
        addSearchString(" §7- §e${total.addSeparators()} §7Total Mythological Creatures")
        addSearchString(" §7- §e${data.creaturesSinceLastInquisitor.addSeparators()} §7Creatures since last Minos Inquisitor")
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.showPercentage) {
            tracker.update()
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return

        tracker.renderDisplay(config.position)
    }

    fun resetCommand() {
        tracker.resetCommand()
    }

    private fun isEnabled() = DianaAPI.isDoingDiana() && config.enabled
}
