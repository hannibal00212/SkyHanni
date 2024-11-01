package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.EntityHealthUpdateEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.diana.InquisitorFoundEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.hasGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.cleanPlayerName
import at.hannibal2.skyhanni.utils.StringUtils.stripHypixelMessage
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Matcher
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object InquisitorWaypointShare {

    private val config get() = SkyHanniMod.feature.event.diana.inquisitorSharing

    private val patternGroup = RepoPattern.group("diana.waypoints")

    /**
     * REGEX-TEST: §9Party §8> User Name§f: §rx: 2.3, y: 4.5, z: 6.7
     */
    private val partyOnlyCoordsPattern by patternGroup.pattern(
        "party.onlycoords",
        "(?<party>§9Party §8> )?(?<playerName>.+)§f: §rx: (?<x>[^ ,]+),? y: (?<y>[^ ,]+),? z: (?<z>[^ ,]+)"
    )

    // Support for https://www.chattriggers.com/modules/v/inquisitorchecker
    /**
     * REGEX-TEST: §9Party §8> UserName§f: §rA MINOS INQUISITOR has spawned near [Foraging Island ] at Coords 1 2 3
     */
    @Suppress("MaxLineLength")
    private val partyInquisitorCheckerPattern by patternGroup.pattern(
        "party.inquisitorchecker",
        "(?<party>§9Party §8> )?(?<playerName>.+)§f: §rA MINOS INQUISITOR has spawned near \\[(?<area>.*)] at Coords (?<x>[^ ]+) (?<y>[^ ]+) (?<z>[^ ]+)"
    )

    /**
     * REGEX-TEST: §9Party §8> §b[MVP§9+§b] _088§f: §rx: 86, y: 73, z: -29 I dug up an inquisitor come over here!
     */
    @Suppress("MaxLineLength")
    private val odinPattern by patternGroup.pattern(
        "party.odin",
        "(?<party>§9Party §8> )?(?<playerName>.+)§f: §rx: (?<x>[^ ]+), y: (?<y>[^ ]+), z: (?<z>[^ ]+) I dug up an inquisitor come over here!"
    )

    /**
     * REGEX-TEST: §9Party §8> §6[MVP§0++§6] scaryron§f: §rx: -67, y: 75, z: 116 | Minos Inquisitor spawned at [ ⏣ Mountain ]!
     */
    @Suppress("MaxLineLength")
    private val sboPattern by patternGroup.pattern(
        "party.sbo",
        "(?<party>§9Party §8> )?(?<playerName>.+)§f: §rx: (?<x>[^ ]+), y: (?<y>[^ ]+), z: (?<z>[^ ]+) \\| Minos Inquisitor spawned at (?<area>.*)!"
    )

    private val diedPattern by patternGroup.pattern(
        "died",
        "(?<party>§9Party §8> )?(?<playerName>.*)§f: §rInquisitor dead!"
    )

    /**
     * REGEX-TEST: §c§lUh oh! §r§eYou dug out a §r§2Minos Inquisitor§r§e!
     */
    private val inquisitorFoundChatPattern by patternGroup.pattern(
        "inquisitor.dug",
        ".* §r§eYou dug out a §r§2Minos Inquisitor§r§e!"
    )

    private var inquisitor = -1
    private var lastInquisitor = -1
    private var lastShareTime = SimpleTimeMark.farPast()
    private var inquisitorsNearby = emptyList<EntityOtherPlayerMP>()

    var waypoints = mapOf<String, SharedInquisitor>()

    class SharedInquisitor(
        val fromPlayer: String,
        val displayName: String,
        val location: LorenzVec,
        val spawnTime: SimpleTimeMark,
    )

    private var test = false

    fun test() {
        test = !test
        ChatUtils.chat("Inquisitor Test " + if (test) "Enabled" else "Disabled")
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        if (event.repeatSeconds(3)) {
            inquisitorsNearby = inquisitorsNearby.editCopy { removeIf { it.isDead } }
        }

        waypoints = waypoints.editCopy { values.removeIf { it.spawnTime.passedSince() > 75.seconds } }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        waypoints = emptyMap()
        inquisitorsNearby = emptyList()
    }

    private val inquisitorTime = mutableListOf<SimpleTimeMark>()

    @HandleEvent
    fun onInquisitorFound(event: InquisitorFoundEvent) {
        val inquisitor = event.inquisitorEntity
        inquisitorsNearby = inquisitorsNearby.editCopy { add(inquisitor) }
        GriffinBurrowHelper.update()

        lastInquisitor = inquisitor.entityId
        checkInquisFound()
    }

    // We do not know if the chat message or the entity spawn happens first.
    // We only want to run foundInquisitor when both happens in under 1.5 seconds
    private fun checkInquisFound() {
        inquisitorTime.add(SimpleTimeMark.now())

        val lastTwo = inquisitorTime.takeLast(2)
        if (lastTwo.size != 2) return

        if (lastTwo.all { it.passedSince() < 1.5.seconds }) {
            inquisitorTime.clear()
            foundInquisitor(lastInquisitor)
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        val message = event.message

        if (inquisitorFoundChatPattern.matches(message)) {
            checkInquisFound()
        }
    }

    private fun foundInquisitor(inquisId: Int) {
        lastShareTime = SimpleTimeMark.farPast()
        inquisitor = inquisId

        if (config.instantShare) {
            // add repo kill switch
            sendInquisitor()
        } else {
            val keyName = KeyboardManager.getKeyName(config.keyBindShare)
            val message = "§l§bYou found an Inquisitor! Click §l§chere §l§bor press §c$keyName to share the location!"
            ChatUtils.clickableChat(
                message, onClick = {
                    sendInquisitor()
                },
                "§eClick to share!",
                oneTimeClick = true
            )
        }
    }

    @SubscribeEvent
    fun onEntityHealthUpdate(event: EntityHealthUpdateEvent) {
        if (!isEnabled()) return
        if (event.health > 0) return

        val entityId = event.entity.entityId
        if (entityId == inquisitor) {
            sendDeath()
        }
        inquisitorsNearby.find { it.entityId == entityId }?.let {
            inquisitorsNearby = inquisitorsNearby.editCopy { remove(it) }
        }
    }

    @SubscribeEvent
    fun onKeyClick(event: LorenzKeyPressEvent) {
        if (!isEnabled()) return
        if (Minecraft.getMinecraft().currentScreen != null) return
        if (event.keyCode == config.keyBindShare) sendInquisitor()
    }

    private fun sendDeath() {
        if (!isEnabled()) return
        if (lastShareTime.passedSince() < 5.seconds) return

        // already dead
        if (inquisitor == -1) return
        inquisitor = -1
        HypixelCommands.partyChat("Inquisitor dead!")
    }

    private fun sendInquisitor() {
        if (!isEnabled()) return
        if (lastShareTime.passedSince() < 5.seconds) return
        lastShareTime = SimpleTimeMark.now()

        if (inquisitor == -1) {
            ChatUtils.debug("Trying to send inquisitor via chat, but no Inquisitor is nearby.")
            return
        }

        val inquisitor = EntityUtils.getEntityByID(inquisitor)
        if (inquisitor == null) {
            ChatUtils.chat("§cInquisitor out of range!")
            return
        }

        if (inquisitor.isDead) {
            ChatUtils.chat("§cInquisitor is dead")
            return
        }
        val location = inquisitor.getLorenzVec()
        val x = location.x.toInt()
        val y = location.y.toInt()
        val z = location.z.toInt()
        HypixelCommands.partyChat("x: $x, y: $y, z: $z ")
    }

    @HandleEvent(onlyOnIsland = IslandType.HUB, priority = HandleEvent.LOW, receiveCancelled = true)
    fun onFirstChatEvent(event: PacketReceivedEvent) {
        if (!isEnabled()) return
        val packet = event.packet
        if (packet !is S02PacketChat) return
        val messageComponent = packet.chatComponent

        val message = messageComponent.formattedText.stripHypixelMessage()
        //#if MC<1.12
        if (packet.type.toInt() != 0) return
        //#else
        //$$ if (packet.type.id.toInt() != 0) return
        //#endif

        partyInquisitorCheckerPattern.matchMatcher(message) {
            if (detectFromChat()) {
                event.cancel()
            }
        }
        odinPattern.matchMatcher(message) {
            if (detectFromChat()) {
                event.cancel()
            }
        }

        partyOnlyCoordsPattern.matchMatcher(message) {
            if (detectFromChat()) {
                event.cancel()
            }
        }
        diedPattern.matchMatcher(message) {
            if (block()) return
            val rawName = group("playerName")
            val name = rawName.cleanPlayerName()
            waypoints = waypoints.editCopy { remove(name) }
            GriffinBurrowHelper.update()
        }
    }

    private fun Matcher.block(): Boolean = !hasGroup("party") && !config.globalChat

    private fun Matcher.detectFromChat(): Boolean {
        if (block()) return false
        val rawName = group("playerName")
        val x = group("x").trim().toDoubleOrNull() ?: return false
        val y = group("y").trim().toDoubleOrNull() ?: return false
        val z = group("z").trim().toDoubleOrNull() ?: return false
        val location = LorenzVec(x, y, z)

        val name = rawName.cleanPlayerName()
        val displayName = rawName.cleanPlayerName(displayName = true)
        if (!waypoints.containsKey(name)) {
            ChatUtils.chat("$displayName §l§efound an inquisitor at §l§c${x.toInt()} ${y.toInt()} ${z.toInt()}!")
            if (name != LorenzUtils.getPlayerName()) {
                LorenzUtils.sendTitle("§dINQUISITOR §efrom §b$displayName", 5.seconds)
                SoundUtils.playBeepSound()
            }
        }
        val inquis = SharedInquisitor(name, displayName, location, SimpleTimeMark.now())
        waypoints = waypoints.editCopy { this[name] = inquis }
        GriffinBurrowHelper.update()
        return true
    }

    private fun isEnabled() = DianaAPI.isDoingDiana() && config.enabled

    fun maybeRemove(inquis: SharedInquisitor) {
        if (inquisitorsNearby.isEmpty()) {
            waypoints = waypoints.editCopy { remove(inquis.fromPlayer) }
            GriffinBurrowHelper.update()
            ChatUtils.chat("Inquisitor from ${inquis.displayName} §enot found, deleting.")
        }
    }
}
