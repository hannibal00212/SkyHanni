package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.features.commands.LimboCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class LimboTimeTracker {
    private val config get() = SkyHanniMod.feature.misc

    private var limboJoinTime = SimpleTimeMark.farPast()
    private var inLimbo = false
    private var inFakeLimbo = false
    private var shownPB = false
    private var oldPB: Duration = 0.seconds
    private var userLuck: Double = 0.0
    private val userLuckMultiplier = 0.000810185


    //bedwars limbo coords, maybe move this somewhere else
    val minX = -662.0
    val minY = 43.0
    val minZ = -76.0
    val maxX = -619.0
    val maxY = 86.0
    val maxZ = -27.0

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (event.message == "§cYou are AFK. Move around to return from AFK." || event.message == "§cYou were spawned in Limbo.") {
            limboJoinTime = SimpleTimeMark.now()
            inLimbo = true
            LimboCommands.enterLimbo(limboJoinTime)
        }
    }

    @SubscribeEvent
    fun catchPlaytime(event: MessageSendToServerEvent) {
        if (event.message.startsWith("/playtime") && inLimbo) {
            event.isCanceled
            LimboCommands.printPlaytime(true)
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (inLimbo && !shownPB && limboJoinTime.passedSince() >= config.limboTimePB.seconds && config.limboTimePB != 0) {
            shownPB = true
            oldPB = config.limboTimePB.seconds
            LorenzUtils.chat("§d§lPERSONAL BEST§f! You've surpassed your previous record of §e$oldPB§f!")
            LorenzUtils.chat("§fKeep it up!")
        }
        val lobbyName: String? = HypixelData.locrawData?.get("lobbyname")?.asString
        val player: EntityPlayer? = Minecraft.getMinecraft().thePlayer
        if (lobbyName.toString().startsWith("bedwarslobby") && player != null) {
            val playerX: Double = player.posX
            val playerY: Double = player.posY
            val playerZ: Double = player.posZ
            if (playerX in minX..maxX && playerY in minY..maxY && playerZ in minZ..maxZ) {
                if (inFakeLimbo) return
                limboJoinTime = SimpleTimeMark.now()
                inLimbo = true
                LimboCommands.enterLimbo(limboJoinTime)
                inFakeLimbo = true
            }
            else {
                if (!inLimbo) return
                leaveLimbo()
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        if (!inLimbo) return
        leaveLimbo()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!inLimbo) return

        if (LorenzUtils.inSkyBlock) {
            leaveLimbo()
            return
        }

        val duration = limboJoinTime.passedSince().format()
        config.showTimeInLimboPosition.renderString("§eIn limbo since §b$duration", posLabel = "Limbo Time Tracker")
    }

    private fun leaveLimbo() {
        inLimbo = false
        if (!isEnabled()) return
        val passedSince = limboJoinTime.passedSince()
        val duration = passedSince.format()
        val currentPB = config.limboTimePB.seconds
        if (passedSince > currentPB) {
            oldPB = currentPB
            config.limboTimePB = passedSince.toInt(DurationUnit.SECONDS)
            LorenzUtils.chat("§fYou were in Limbo for §e$duration§f! §d§lPERSONAL BEST§r§f!")
            LorenzUtils.chat("§fYour previous Personal Best was §e$oldPB.")
            userLuck = config.limboTimePB * userLuckMultiplier
            LorenzUtils.chat("§fYour §aPersonal Bests§f perk is now granting you §a+${userLuck.round(2)}✴ SkyHanni User Luck§f!")
        } else LorenzUtils.chat("§fYou were in Limbo for §e$duration§f.")
        config.limboPlaytime += passedSince.toInt(DurationUnit.SECONDS)
        shownPB = false
    }

    fun isEnabled() = config.showTimeInLimbo
}
