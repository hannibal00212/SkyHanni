package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.GetFromSackApi
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.MiningApi
import at.hannibal2.skyhanni.data.MiningApi.inGlaciteArea
import at.hannibal2.skyhanni.data.MiningApi.lastColdReset
import at.hannibal2.skyhanni.events.ColdUpdateEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.PrimitiveItemStack.Companion.makePrimitiveStack
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object MiningNotifications {

    private val ASCENSION_ROPE by lazy { "ASCENSION_ROPE".toInternalName().makePrimitiveStack(1) }

    enum class MiningNotificationList(val str: String, val notification: String) {
        MINESHAFT_SPAWN("§bGlacite Mineshaft", "§bMineshaft"),
        SCRAP("§9Suspicious Scrap", "§9Suspicious Scrap"),
        GOLDEN_GOBLIN("§6Golden Goblin", "§6Golden Goblin"),
        DIAMOND_GOBLIN("§bDiamond Goblin", "§bDiamond Goblin"),
        COLD("§bCold", "§bCold");

        override fun toString() = str
    }

    private val patternGroup = RepoPattern.group("mining.notifications")
    val mineshaftSpawn by patternGroup.pattern(
        "mineshaft.spawn",
        "§5§lWOW! §r§aYou found a §r§bGlacite Mineshaft §r§aportal!",
    )
    private val scrapDrop by patternGroup.pattern(
        "scrapdrop",
        "§6§lEXCAVATOR! §r§fYou found a §r§9Suspicious Scrap§r§f!",
    )
    val goldenGoblinSpawn by patternGroup.pattern(
        "goblin.goldspawn",
        "§6A Golden Goblin has spawned!",
    )
    val diamondGoblinSpawn by patternGroup.pattern(
        "goblin.diamondspawn",
        "§6A §r§bDiamond Goblin §r§6has spawned!",
    )

    private val config get() = SkyHanniMod.feature.mining.notifications

    private var hasSentCold = false
    private var hasSentAscensionRope = false

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!LorenzUtils.inMiningIsland()) return
        if (!config.enabled) return
        val message = event.message
        when {
            mineshaftSpawn.matches(message) -> sendNotification(MiningNotificationList.MINESHAFT_SPAWN)
            scrapDrop.matches(message) -> sendNotification(MiningNotificationList.SCRAP)
            goldenGoblinSpawn.matches(message) -> sendNotification(MiningNotificationList.GOLDEN_GOBLIN)
            diamondGoblinSpawn.matches(message) -> sendNotification(MiningNotificationList.DIAMOND_GOBLIN)
        }
    }

    @HandleEvent
    fun onColdUpdate(event: ColdUpdateEvent) {
        if (!inGlaciteArea()) return
        if (!config.enabled) return
        if (lastColdReset.passedSince() < 1.seconds) return

        if (event.cold >= config.coldThreshold.get() && !hasSentCold) {
            hasSentCold = true
            sendNotification(MiningNotificationList.COLD)
        }
        if (MiningApi.inMineshaft() && config.getAscensionRope && event.cold >= config.coldAmount && !hasSentAscensionRope) {
            hasSentAscensionRope = true
            DelayedRun.runDelayed(0.5.seconds) {
                GetFromSackApi.getFromChatMessageSackItems(ASCENSION_ROPE)
            }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        hasSentCold = false
        hasSentAscensionRope = false
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.coldThreshold) {
            if (MiningApi.cold != config.coldThreshold.get()) hasSentCold = false
        }
    }

    private fun sendNotification(type: MiningNotificationList) {
        if (type !in config.notifications) return
        LorenzUtils.sendTitle(type.notification, 1500.milliseconds)
        if (config.playSound) SoundUtils.playPlingSound()
    }
}
