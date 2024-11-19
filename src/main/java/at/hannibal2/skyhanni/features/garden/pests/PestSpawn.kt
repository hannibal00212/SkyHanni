package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.pests.PestSpawnConfig
import at.hannibal2.skyhanni.config.features.garden.pests.PestSpawnConfig.ChatMessageFormatEntry
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.garden.pests.PestSpawnEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PestSpawn {

    private val config get() = PestAPI.config.pestSpawn

    private val patternGroup = RepoPattern.group("garden.pests.spawn")

    /**
     * REGEX-TEST: §6§lGROSS! §7A §2Pest §7has appeared in §aPlot §7- §b4§7!
     */
    private val onePestPattern by patternGroup.pattern(
        "one",
        "§6§l.*! §7A §2Pest §7has appeared in §aPlot §7- §b(?<plot>.*)§7!"
    )

    /**
     * REGEX-TEST: §6§lEWW! §22 Pests §7have spawned in §aPlot §7- §b2§7!
     * REGEX-TEST: §6§lYUCK! §23 Pests §7have spawned in §aPlot §7- §bR1§7!
     */
    private val multiplePestsSpawn by patternGroup.pattern(
        "multiple",
        "§6§l.*! §2(?<amount>\\d) Pests §7have spawned in §aPlot §7- §b(?<plot>.*)§7!"
    )

    /**
     * REGEX-TEST: §6§lGROSS! §7While you were offline, §6Pests §7spawned in §aPlots §r§b12§r§7, §r§b9§r§7, §r§b5§r§7, §r§b11§r§7 and §r§b3§r§r§7!
     */
    private val offlinePestsSpawn by patternGroup.pattern(
        "offline",
        "§6§l.*! §7While you were offline, §6Pests §7spawned in §aPlots (?<plots>.*)!"
    )
    private var plotNames = mutableListOf<String>()

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!GardenAPI.inGarden()) return
        val message = event.message
        var blocked = false

        plotNames.clear()

        onePestPattern.matchMatcher(message) {
            val plotName = group("plot")
            plotNames.add(plotName)
            pestSpawn(1, plotNames, false)
            blocked = true
        }
        multiplePestsSpawn.matchMatcher(message) {
            val plotName = group("plot")
            plotNames.add(plotName)
            val amount = group("amount").toInt()
            pestSpawn(amount, plotNames, false)
            blocked = true
        }
        offlinePestsSpawn.matchMatcher(message) {
            val plots = group("plots")
            plotNames = plots.removeColor().split(", ", " and ").toMutableList()
            pestSpawn(0, plotNames, true)
            // blocked = true
        }

        if (event.message == "  §r§e§lCLICK HERE §eto teleport to the plot!") {
            if (PestSpawnTimer.lastSpawnTime.passedSince() < 1.seconds) {
                blocked = true
            }
        }

        if (blocked && config.chatMessageFormat != PestSpawnConfig.ChatMessageFormatEntry.HYPIXEL) {
            event.blockedReason = "pests_spawn"
        }
    }

    private fun pestSpawn(amount: Int, plotNames: List<String>, unknownAmount: Boolean) {
        PestSpawnEvent(amount, plotNames, unknownAmount).post()

        if (unknownAmount) return // todo make this work with offline pest spawn messages
        val plotName = plotNames.firstOrNull() ?: error("first plot name is null")
        val pestName = StringUtils.pluralize(amount, "Pest")
        val message = "§e$amount §a$pestName Spawned in §b$plotName§a!"

        if (config.showTitle) {
            LorenzUtils.sendTitle(message, 7.seconds)
        }

        if (config.chatMessageFormat == PestSpawnConfig.ChatMessageFormatEntry.COMPACT) {
            ChatUtils.clickableChat(
                message,
                onClick = {
                    HypixelCommands.teleportToPlot(plotName)
                },
                "§eClick to run /plottp $plotName!",
            )
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(15, "garden.pests.pestSpawn.chatMessageFormat") { element ->
            ConfigUtils.migrateIntToEnum(element, ChatMessageFormatEntry::class.java)
        }
    }
}
