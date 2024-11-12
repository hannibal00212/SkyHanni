package at.hannibal2.skyhanni.features.rift.area.mirrorverse

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.jsonobjects.repo.DanceRoomInstructionsJson
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.TitleReceivedEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LocationUtils.isPlayerInside
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DanceRoomHelper {

    private var display = emptyList<String>()
    private val config get() = RiftAPI.config.area.mirrorverse.danceRoomHelper
    private var index = 0
    private var found = false
    private val danceRoom = AxisAlignedBB(-260.0, 32.0, -110.0, -267.0, 40.0, -102.0)
    private var inRoom = false
    private var instructions = emptyList<String>()
    private var countdown: String? = null

    fun update() {
        display = buildList {
            if (instructions.isEmpty()) {
                add("§cError fetching Dance Room Instructions!")
                add("§cTry §e/shreloadlocalrepo §cor §e/shupdaterepo")
                // TODO make clickable
            }
            for ((lineIndex, line) in instructions.withIndex()) {
                addLine(lineIndex, line)?.let { add(it) }
            }
        }
    }

    private fun addLine(lineIndex: Int, line: String) = with(config.danceRoomFormatting) {
        val size = instructions.size
        val format = line.format()

        when {
            index < size && index == lineIndex -> {
                val countdown = countdown?.let { "${color.countdown.formatColor()}$it" }.orEmpty()
                "${now.formatColor()} $format $countdown"
            }

            index + 1 < size && index + 1 == lineIndex -> {
                "${next.formatColor()} $format"
            }

            index + 2 < size && (index + 2..index + config.lineToShow).contains(lineIndex) -> {
                "${later.formatColor()} $format"
            }

            else -> null
        }
    }

    private fun String.formatColor() = replace("&", "§")

    private fun String.format() =
        split(" ").joinToString(" ") { it.firstLetterUppercase().addColor().replace("&", "§") }

    private fun String.addColor() = with(config.danceRoomFormatting.color) {
        when (this@addColor) {
            "Move" -> move
            "Stand" -> stand
            "Sneak" -> sneak
            "Jump" -> jump
            "Punch" -> punch
            else -> fallback
        } + this@addColor
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!inRoom) return
        config.position.renderStrings(
            display,
            config.extraSpace,
            posLabel = "Dance Room Helper"
        )
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        inRoom = false
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (event.isMod(10)) {
            inRoom = danceRoom.isPlayerInside()
        }
        if (inRoom) {
            update()
        }
    }

    @SubscribeEvent
    fun onPlaySound(event: PlaySoundEvent) {
        if (!isEnabled() || !inRoom) return
        if ((event.soundName == "random.burp" && event.volume == 0.8f) ||
            (event.soundName == "random.levelup" && event.pitch == 1.8412699f && event.volume == 1.0f)
        ) {
            index = 0
            found = false
            countdown = null
            update()
        }
        if (event.soundName == "note.bassattack" && event.pitch == 0.6984127f && event.volume == 1.0f && !found) {
            found = true
            start(2000)
            update()
        }
    }

    @SubscribeEvent
    fun onTitleReceived(event: TitleReceivedEvent) {
        if (!isEnabled()) return
        if (config.hideOriginalTitle && inRoom) event.cancel()
    }

    private fun startCountdown(seconds: Int, milliseconds: Int) {
        if (seconds <= 0 && milliseconds <= 0) {
            countdown = null
            return
        }

        val countdownString = "%01d:%03d".format(seconds, milliseconds)
        countdown = countdownString

        SkyHanniMod.coroutineScope.launch {
            delay(1)
            var updatedSeconds = seconds
            var updatedMilliseconds = milliseconds - 1
            if (updatedMilliseconds < 0) {
                updatedSeconds -= 1
                updatedMilliseconds += 1000
            }
            startCountdown(updatedSeconds, updatedMilliseconds)
        }
    }

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (RiftAPI.inRift() && config.hidePlayers) {
            val entity = event.entity
            if (entity is EntityOtherPlayerMP && inRoom) {
                event.cancel()
            }
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        instructions = event.getConstant<DanceRoomInstructionsJson>("DanceRoomInstructions").instructions
    }

    fun start(interval: Long) {
        SkyHanniMod.coroutineScope.launch {
            while (isActive && found) {
                index++
                startCountdown(0, 500)
                delay(interval)
            }
        }
    }

    fun isEnabled() = RiftAPI.inRift() && config.enabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(9, "rift.area.mirrorVerseConfig", "rift.area.mirrorverse")
    }
}
