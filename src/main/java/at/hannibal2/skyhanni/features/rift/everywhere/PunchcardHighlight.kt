package at.hannibal2.skyhanni.features.rift.everywhere

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.MobData
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.EntityClickEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.EntityUtils.isNPC
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PunchcardHighlight {
    private val config get() = SkyHanniMod.feature.rift.punchcard
    private var lastRiftServer: String = ""

    private var listening = false

    private val patternGroup = RepoPattern.group("rift.punchcard")

    /**
     * REGEX-TEST: §5§lPUNCHCARD! §r§eYou punched §r§b[MVP§r§c+§r§b] ThorQOM§r§f §r§eand both regained §r§a+25ф Rift Time§r§e!
     * REGEX-TEST: §5§lPUNCHCARD! §r§eYou punched §r§7Metafighter§r§7 §r§eand both regained §r§a+25ф Rift Time§r§e!
     * REGEX-TEST: §5§lPUNCHCARD! §r§eYou punched §r§a[VIP] RickyLafleur22§r§f §r§eand both regained §r§a+25ф Rift Time§r§e!
     */
    private val punchedPattern by patternGroup.pattern(
        "new",
        "§5§lPUNCHCARD! §r§eYou punched §r§.(?:.*?)?(?<name>\\w+)§r§. §r§eand both regained §r§a\\+25ф Rift Time§r§e!",
    )

    /**
     * REGEX-TEST:  §c§lAWKWARD! §r§cThis player has already been punched by you... somehow!
     */
    private val repeatPattern by patternGroup.pattern(
        "repeat",
        "§c§lAWKWARD! §r§cThis player has already been punched by you\\.\\.\\. somehow!",
    )

    /**
     * REGEX-TEST:  §c§lUH OH! §r§cYou reached the limit of 20 players you can punch in one session!
     */
    private val limitPattern by patternGroup.pattern(
        "limit",
        "§c§lUH OH! §r§cYou reached the limit of 20 players you can punch in one session!",
    )

    private val playerList: MutableSet<String> = mutableSetOf()
    private var playerQueue = mutableListOf<String>()

    private val displayIcon by lazy { "PUNCHCARD_ARTIFACT".asInternalName().getItemStack() }
    private var display: Renderable = Renderable.string("hello")

    @SubscribeEvent
    fun onPlayerSpawn(event: MobEvent.Spawn.Player) {
        if (!config.enabled.get()) return
        if (!IslandType.THE_RIFT.isInIsland()) return
        if (config.reverse.get()) return
        val size = playerList.size
        if (size >= 20) return
        val entity = event.mob
        if (!playerList.contains(entity.name)) {
            colorPlayer(entity.baseEntity)
        }
    }

    @SubscribeEvent
    fun onToggle(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(
            config.enabled,
            config.color,
            config.reverse,
        ) {
            reloadColors()
        }
        ConditionalUtils.onToggle(
            config.compact,
            config.reverseGUI,
        ) {
            display = drawDisplay()
        }
    }

    @SubscribeEvent
    fun onWorldSwitch(event: IslandChangeEvent) {
        if (!config.enabled.get()) return
        if (event.newIsland != IslandType.THE_RIFT) return

        display = drawDisplay()
        DelayedRun.runDelayed(1500.milliseconds) {
            reloadColors()
            if (IslandType.THE_RIFT.isInIsland() &&
                HypixelData.server.isNotEmpty() &&
                lastRiftServer != HypixelData.server
            ) {
                lastRiftServer = HypixelData.server
                playerList.clear()
            }
        }
    }

    private fun colorPlayer(entity: EntityLivingBase) {
        val color = config.color.get().toChromaColor()
        val alpha = when (color.alpha) {
            0 -> 0
            255 -> 1
            else -> 255 - color.alpha
        }
        RenderLivingEntityHelper.setEntityColor(entity, color.withAlpha(alpha)) { IslandType.THE_RIFT.isInIsland() }
    }

    private fun uncolorPlayer(entity: EntityLivingBase) {
        RenderLivingEntityHelper.removeEntityColor(entity)
    }

    fun clearList() {
        playerList.clear()
        playerQueue.clear()
        if (config.reverse.get()) {
            MobData.players.forEach {
                colorPlayer(it.baseEntity)
            }
        } else {
            MobData.players.forEach {
                uncolorPlayer(it.baseEntity)
            }
        }
    }

    @SubscribeEvent
    fun onPunch(event: EntityClickEvent) {
        if (!RiftAPI.inRift()) return
        if (!config.enabled.get()) return
        val entity = event.clickedEntity
        if (entity !is AbstractClientPlayer) return
        if (entity.isNPC()) return
        val name = entity.name
        if (name in playerList || name in playerQueue) return
        playerQueue.add(name)
        listening = true
        DelayedRun.runDelayed(1.seconds) {
            if (name in playerQueue) playerQueue.remove(name)
            listening = false
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!IslandType.THE_RIFT.isInIsland()) return
        if (!listening) return
        if (playerQueue.isEmpty()) return
        val message = event.message
        val queuedName = playerQueue[0]
        punchedPattern.matchMatcher(message) {
            val name = group("name")
            if (queuedName == name) {
                addPunch(name)
            } else ErrorManager.logErrorStateWithData(
                "Error finding punched player", "queuedName and capturedName were different",
                "queuedName" to queuedName,
                "capturedName" to name,
                noStackTrace = true,
                betaOnly = true,
            )
            return
        }
        if (limitPattern.matches(message) || repeatPattern.matches(message)) addPunch(queuedName)
    }

    private fun addPunch(playerName: String) {
        playerList.add(playerName)
        playerQueue.remove(playerName)
        val player = MobData.players.firstOrNull { it.name == playerName } ?: return
        if (!config.reverse.get()) uncolorPlayer(player.baseEntity)
        else colorPlayer(player.baseEntity)
        display = drawDisplay()
    }

    @SubscribeEvent
    fun onRenderUI(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.gui) return
        if (!RiftAPI.inRift()) return

        config.position.renderRenderable(display, "Punchcard Overlay")
    }

    private fun drawDisplay(): Renderable {
        var string = ""
        if (!config.compact.get()) string += "Punchcard Artifact: "
        string += "§d" + if (!config.reverseGUI.get()) playerList.size
        else 20 - playerList.size

        return Renderable.horizontalContainer(
            listOf(
                Renderable.itemStack(displayIcon),
                Renderable.string(string),
            ),
            spacing = 1,
        )
    }

    private fun reloadColors() {
        MobData.players.forEach {
            uncolorPlayer(it.baseEntity)
        }
        if (!config.enabled.get()) return
        val reverse = config.reverse.get()
        for (player in MobData.players.filter { (reverse && it.name in playerList) || (!reverse && it.name !in playerList) }) {
            colorPlayer(player.baseEntity)
        }
    }
}
