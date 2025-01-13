package at.hannibal2.skyhanni.features.nether

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.RescueParkourJson
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.nether.reputationhelper.CrimsonIsleReputationHelper
import at.hannibal2.skyhanni.features.nether.reputationhelper.FactionType
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.ParkourHelper
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class RescueMissionWaypoints(private val reputationHelper: CrimsonIsleReputationHelper) {

    // Logic for the parkour location waypoint system is based on the Deep Caverns Guide by hannibal2

    private val config get() = SkyHanniMod.feature.crimsonIsle.reputationHelper.rescueMissionConfig
    private val patternGroup = RepoPattern.group("crimson.rescue")
    private val agentDialoguePattern by patternGroup.pattern(
        "start",
        "\\[NPC] Undercover Agent: Here's what you need to do."
    )
    private val questTierPattern by RepoPattern.pattern(
        "tier",
        "§\\w(?<tier>.) §[\\w ]+"
    )



    private var show = false
    private var parkourHelper: ParkourHelper? = null
    private var tier: String? = null
    private var data: RescueParkourJson? = null

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        show = false
        parkourHelper?.reset()
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        if (!IslandType.CRIMSON_ISLE.isInIsland()) return
        if (parkourHelper != null) return
        if (data == null) return

        questTierPattern.matchMatcher(event.itemStack.name) {
            tier = group("tier").toLetter()
        }

        if (tier == null || tier == "") return

        parkourHelper = when(reputationHelper.factionType) {
            FactionType.MAGE -> data!!.mage[tier]?.let {
                ParkourHelper(
                    it,
                    listOf(),
                    platformSize = 1.0,
                    detectionRange = 3.5,
                    onEndReach = {
                        show = false
                    }
                )
            }
            FactionType.BARBARIAN -> data!!.barb[tier]?.let {
                ParkourHelper(
                    it,
                    listOf(),
                    platformSize = 1.0,
                    detectionRange = 3.5,
                    onEndReach = {
                        show = false
                    }
                )
            }
            FactionType.NONE -> null
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        data = event.getConstant<RescueParkourJson>("RescueMissionWaypoints")
        updateConfig()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        agentDialoguePattern.matchMatcher(event.message.removeColor()) {
            if (!show) {
                startWaypoints()
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (!show) return

        parkourHelper?.render(event)
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.chroma, config.solidColor, config.lookAhead) {
            updateConfig()
        }
    }

    private fun startWaypoints() {
        show = true
        parkourHelper?.reset()
        if (parkourHelper == null) {
            ChatUtils.clickableChat(
                "RescueMissionWaypoints missing in SkyHanni Repo! Trying /shupdaterepo to fix it!",
                onClick = {
                    SkyHanniMod.repo.updateRepo()
                },
                prefixColor = "§c"
            )
        }
    }

    private fun String.toLetter(): String {
        return when(this) {
            "Ⓐ" -> "A"
            "Ⓑ" -> "B"
            "Ⓒ" -> "C"
            "Ⓓ" -> "D"
            else -> ""
        }
    }

    private fun updateConfig() {
        parkourHelper?.run {
            rainbowColor = config.chroma.get()
            monochromeColor = config.solidColor.get().toChromaColor()
            lookAhead = config.lookAhead.get() + 1
        }
    }

    fun isEnabled() = config.enabled
}
