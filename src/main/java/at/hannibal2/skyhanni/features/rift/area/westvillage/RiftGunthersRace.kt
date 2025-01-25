package at.hannibal2.skyhanni.features.rift.area.westvillage

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.ParkourJson
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.EntityUtils.isNpc
import at.hannibal2.skyhanni.utils.ParkourHelper
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SpecialColor.toSpecialColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity

@SkyHanniModule
object RiftGunthersRace {

    private val config get() = RiftApi.config.area.westVillage.gunthersRace
    private var parkourHelper: ParkourHelper? = null

    private val patternGroup = RepoPattern.group("rift.area.westvillage.riftrace")

    /**
     * REGEX-TEST: §3§lRIFT RACING §r§eRace started! Good luck!
     */
    private val raceStartedPattern by patternGroup.pattern(
        "start",
        "§3§lRIFT RACING §r§eRace started! Good luck!",
    )

    /**
     * REGEX-TEST: §3§lRIFT RACING §r§eRace finished in 00:36.539!
     * REGEX-TEST: §3§lRIFT RACING §r§eRace finished in §r§300:32.794§r§e! §r§3§lPERSONAL BEST!
     */
    private val raceFinishedPattern by patternGroup.pattern(
        "finish",
        "§3§lRIFT RACING §r§eRace finished in (?:§.)*\\d+:\\d+.\\d+(?:§.)*!.*",
    )

    /**
     * REGEX-TEST: §3§lRIFT RACING §r§cRace cancelled!
     * REGEX-TEST: §3§lRIFT RACING §r§cRace cancelled! Time limit reached!
     * REGEX-TEST: §3§lRIFT RACING §r§cRace cancelled! You left the racing area!
     */
    private val raceCancelledPattern by patternGroup.pattern(
        "cancel",
        "§3§lRIFT RACING §r§cRace cancelled!.*",
    )

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        parkourHelper?.reset()
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        RiftApi.inRiftRace = false
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<ParkourJson>("rift/RiftRace")
        parkourHelper = ParkourHelper(
            data.locations,
            data.shortCuts,
            detectionRange = 5.0,
            goInOrder = true,
        )
        updateConfig()
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.rainbowColor, config.monochromeColor, config.lookAhead) {
            updateConfig()
        }
    }

    private fun updateConfig() {
        parkourHelper?.run {
            rainbowColor = config.rainbowColor.get()
            monochromeColor = config.monochromeColor.get().toSpecialColor()
            lookAhead = config.lookAhead.get() + 1
        }
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return

        raceStartedPattern.matchMatcher(event.message) {
            RiftApi.inRiftRace = true
        }
        raceCancelledPattern.matchMatcher(event.message) {
            parkourHelper?.reset()
            RiftApi.inRiftRace = false
        }
        raceFinishedPattern.matchMatcher(event.message) {
            parkourHelper?.reset()
            RiftApi.inRiftRace = false
        }
    }

    @HandleEvent
    fun onCheckRender(event: CheckRenderEntityEvent<Entity>) {
        if (!isEnabled()) return
        if (!config.hidePlayers) return
        if (!RiftApi.inRiftRace) return

        val entity = event.entity
        if (entity is EntityOtherPlayerMP && !entity.isNpc()) {
            event.cancel()
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled() || !RiftApi.inRiftRace) return

        parkourHelper?.render(event)
    }

    fun isEnabled() =
        RiftApi.inRift() && RiftApi.inWestVillage() && config.enabled
}
