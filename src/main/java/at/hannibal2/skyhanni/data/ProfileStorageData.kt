package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.SackData
import at.hannibal2.skyhanni.config.storage.PlayerSpecificStorage
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.HypixelJoinEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TabListData
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ProfileStorageData {

    var playerSpecific: PlayerSpecificStorage? = null
    var profileSpecific: ProfileSpecificStorage? = null
    var loaded = false
    private var noTabListTime = SimpleTimeMark.farPast()

    private var sackPlayers: SackData.PlayerSpecific? = null
    var sackProfiles: SackData.ProfileSpecific? = null
    var hypixelDataLoaded = false

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onProfileJoin(event: ProfileJoinEvent) {
        val playerSpecific = playerSpecific
        val sackPlayers = sackPlayers
        val profileName = event.name
        if (playerSpecific == null) {
            DelayedRun.runDelayed(10.seconds) {
                workaroundIn10SecondsProfileStorage(profileName)
            }
            return
        }
        if (sackPlayers == null) {
            ErrorManager.skyHanniError("sackPlayers is null in ProfileJoinEvent!")
        }

        loadProfileSpecific(playerSpecific, sackPlayers, profileName)
        ConfigLoadEvent().postAndCatch()
    }

    private fun workaroundIn10SecondsProfileStorage(profileName: String) {
        println("workaroundIn10SecondsProfileStorage")
        val playerSpecific = playerSpecific
        val sackPlayers = sackPlayers

        if (playerSpecific == null) {
            ErrorManager.skyHanniError(
                "failed to load your profile data delayed ",
                "onHypixel" to LorenzUtils.onHypixel,
                "HypixelData.hypixelLive" to HypixelData.hypixelLive,
                "HypixelData.hypixelAlpha" to HypixelData.hypixelAlpha,
                "sidebarLinesFormatted" to ScoreboardData.sidebarLinesFormatted,
            )
        }
        if (sackPlayers == null) {
            ErrorManager.skyHanniError("sackPlayers is null in ProfileJoinEvent!")
        }
        loadProfileSpecific(playerSpecific, sackPlayers, profileName)
        ConfigLoadEvent().postAndCatch()
    }

    @SubscribeEvent
    fun onTabListUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.PROFILE)) return
        noTabListTime = if (event.isClear()) SimpleTimeMark.now() else SimpleTimeMark.farPast()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (noTabListTime == SimpleTimeMark.farPast()) return

        playerSpecific?.let {
            // do not try to load the data when hypixel has not yet send the profile loaded message
            if (it.multipleProfiles && !hypixelDataLoaded) return
        }

        if (noTabListTime.passedSince() < 5.seconds) return
        noTabListTime = SimpleTimeMark.now()
        val foundSkyBlockTabList = TabListData.getTabList().any { it.contains("§b§lArea:") }
        if (foundSkyBlockTabList) {
            ChatUtils.clickableChat(
                "§cCan not read profile name from tab list! Open /widget and enable Profile Widget. " +
                    "This is needed for the mod to function! And therefore this warning cannot be disabled",
                onClick = {
                    HypixelCommands.widget()
                },
                "§eClick to run /widget!",
            )
        } else {
            ChatUtils.chat(
                "§cExtra Information from Tab list not found! " +
                    "Enable it: SkyBlock Menu ➜ Settings ➜ Personal ➜ User Interface ➜ Player List Info",
            )
        }
    }

    private fun loadProfileSpecific(
        playerSpecific: PlayerSpecificStorage,
        sackProfile: SackData.PlayerSpecific,
        profileName: String,
    ) {
        noTabListTime = SimpleTimeMark.farPast()
        profileSpecific = playerSpecific.profiles.getOrPut(profileName) { ProfileSpecificStorage() }
        sackProfiles = sackProfile.profiles.getOrPut(profileName) { SackData.ProfileSpecific() }
        loaded = true
        ConfigLoadEvent().postAndCatch()
    }

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        val playerUuid = LorenzUtils.getRawPlayerUuid()
        playerSpecific = SkyHanniMod.feature.storage.players.getOrPut(playerUuid) { PlayerSpecificStorage() }
        sackPlayers = SkyHanniMod.sackData.players.getOrPut(playerUuid) { SackData.PlayerSpecific() }
        ConfigLoadEvent().postAndCatch()
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        hypixelDataLoaded = false
    }

    fun profileJoinMessage() {
        hypixelDataLoaded = true
        playerSpecific?.multipleProfiles = true
    }
}
