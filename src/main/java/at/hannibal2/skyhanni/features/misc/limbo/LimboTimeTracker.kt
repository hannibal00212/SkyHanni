package at.hannibal2.skyhanni.features.misc.limbo

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.HypixelJoinEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.client.Minecraft
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@SkyHanniModule
object LimboTimeTracker {
    private val storage get() = ProfileStorageData.playerSpecific?.limbo
    private val config get() = SkyHanniMod.feature.misc

    private var limboJoinTime = SimpleTimeMark.farPast()
    var inLimbo = false
    private var inFakeLimbo = false
    private var shownPB = false
    private var oldPB: Duration = 0.seconds
    private var userLuck: Float = 0.0F
    private const val USER_LUCK_MULTIPLIER = 0.000810185F
    private const val FIRE_MULTIPLIER = 1.01F
    private var onFire = false

    private val bedWarsLobbyLimbo = AxisAlignedBB(-662.0, 43.0, -76.0, -619.0, 86.0, -27.0)

    private var doMigrate = false
    private var notMigratedPB = 0

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (event.message == "§cYou are AFK. Move around to return from AFK." || event.message == "§cYou were spawned in Limbo.") {
            limboJoinTime = SimpleTimeMark.now()
            inLimbo = true
            onFire = Minecraft.getMinecraft().thePlayer.isBurning
        }
    }

    @SubscribeEvent
    fun onMessageSendToServer(event: MessageSendToServerEvent) {
        if (event.message.startsWith("/playtime") && inLimbo) {
            event.isCanceled
            printStats(true)
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        val personalBest = storage?.personalBest ?: 0
        if (inLimbo && !shownPB && limboJoinTime.passedSince() >= personalBest.seconds && personalBest != 0) {
            shownPB = true
            oldPB = personalBest.seconds
            ChatUtils.chat("§d§lPERSONAL BEST§f! You've surpassed your previous record of §e$oldPB§f!")
            ChatUtils.chat("§fKeep it up!")
        }
        val lobbyName: String? = HypixelData.locrawData?.get("lobbyname")?.asString
        if (lobbyName.toString().startsWith("bedwarslobby")) {
            if (bedWarsLobbyLimbo.isPlayerInside()) {
                if (inFakeLimbo) return
                limboJoinTime = SimpleTimeMark.now()
                inLimbo = true
                inFakeLimbo = true
            } else {
                if (inLimbo) {
                    leaveLimbo()
                    inFakeLimbo = false
                }
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
        config.showTimeInLimboPosition.renderString("§eIn Limbo since §b$duration", posLabel = "Limbo Time Tracker")
    }

    private fun leaveLimbo() {
        inLimbo = false
        if (!isEnabled()) return
        val passedSince = limboJoinTime.passedSince()
        val duration = passedSince.format()
        val currentPB = (storage?.personalBest ?: 0).seconds
        val oldLuck = storage?.userLuck ?: 0f
        if (passedSince > currentPB) {
            oldPB = currentPB
            storage?.personalBest = passedSince.toInt(DurationUnit.SECONDS)
            userLuck = ((storage?.personalBest ?: 0) * USER_LUCK_MULTIPLIER).roundTo(2)
            if (onFire) userLuck *= FIRE_MULTIPLIER
            ChatUtils.chat("§fYou were in Limbo for §e$duration§f! §d§lPERSONAL BEST§r§f!")
            if (oldPB != 0.seconds) {
                ChatUtils.chat("§fYour previous Personal Best was §e$oldPB.")
            }

        } else ChatUtils.chat("§fYou were in Limbo for §e$duration§f.")
        if (userLuck > oldLuck) {
            if (onFire) {
                ChatUtils.chat("§fYour §aPersonal Bests§f perk is now granting you §a+${userLuck.roundTo(2)}§c✴ §aSkyHanni User Luck§f! ")
            } else {
                ChatUtils.chat("§fYour §aPersonal Bests§f perk is now granting you §a+${userLuck.roundTo(2)}✴ SkyHanni User Luck§f!")
            }
            storage?.userLuck = userLuck
        }
        storage?.playtime = storage?.playtime?.plus(passedSince.toInt(DurationUnit.SECONDS)) ?: 0
        onFire = false
        shownPB = false
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(27, oldPath = "misc.limboTimePB", newPath = "#player.personalBest")
    }

    fun printStats(onlyPlaytime: Boolean = false) {
        val timeInLimbo: Int = if (inLimbo) limboJoinTime.passedSince().inWholeSeconds.toInt() else 0
        val playtime: Int = if (inLimbo) (storage?.playtime
            ?: 0) + limboJoinTime.passedSince().inWholeSeconds.toInt() else storage?.playtime ?: 0
        if (onlyPlaytime) {
            ChatUtils.chat("§aYou have ${playtime / 3600} hours and ${playtime % 3600 / 60} minutes playtime!", false)
        } else {
            val currentPB = storage?.personalBest ?: 0
            val userLuck = storage?.userLuck ?: 0f
            val limboPB: Int = if (currentPB < timeInLimbo) timeInLimbo else currentPB
            var luckString = tryTruncateFloat(userLuck.roundTo(2))
            if (userLuck > 0) luckString = "+$luckString"
            var firstMessage =
                "§fYour current PB is §e${limboPB.seconds}§f, granting you §a$luckString✴ SkyHanni User Luck§f!"
            val secondMessage = "§fYou have §e${playtime.seconds} §fof playtime!"
            if (userLuck == Float.POSITIVE_INFINITY || userLuck == Float.NEGATIVE_INFINITY) {
                firstMessage = "$firstMessage §Zwhat"
            }
            ChatUtils.chat(firstMessage)
            ChatUtils.chat(secondMessage)
        }
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Limbo")
        if (!inLimbo) {
            event.addIrrelevant("not in limbo")
            return
        }

        event.addData {
            add("inLimbo: $inLimbo")
            add("isLimboFake: $inFakeLimbo")
            add("since: ${limboJoinTime.passedSince()}")
        }
    }

    fun workaroundMigration(personalBest: Int) {
        doMigrate = true
        notMigratedPB = personalBest
    }

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        if (!doMigrate) return
        if (notMigratedPB != 0) {
            ChatUtils.debug("Migrating limbo personalBest")
            storage?.personalBest = notMigratedPB
            storage?.userLuck = notMigratedPB * USER_LUCK_MULTIPLIER
        }
        if ((storage?.personalBest ?: 0) > (storage?.playtime ?: 0)) {
            ChatUtils.debug("Migrating limbo playtime")
            storage?.playtime = (storage?.personalBest ?: 0)
        }
        doMigrate = false
        notMigratedPB = 0
    }

    fun isEnabled() = config.showTimeInLimbo

    private fun tryTruncateFloat(input: Float): String {
        val string = input.toString()
        return if (string.endsWith(".0")) return string.dropLast(2)
        else string
    }
}
