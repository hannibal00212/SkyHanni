package at.hannibal2.skyhanni.utils.mc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.test.command.ErrorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.audio.ISound
import net.minecraft.client.audio.PositionedSound
import net.minecraft.client.audio.SoundCategory
import net.minecraft.client.audio.SoundHandler
import net.minecraft.util.ResourceLocation

typealias Sound = ISound

object McSound {

    val BEEP by lazy { create("random.orb", 1f) }
    val CLICK by lazy { create("gui.button.press", 1f) }
    val ERROR by lazy { create("mob.endermen.portal", 0f) }
    val PLING by lazy { create("note.pling", 1f) }
    val CENTURY_ACTIVE_TIMER_ALERT by lazy { create("skyhanni:centurytimer.active", 1f) }

    val soundHandler: SoundHandler? get() = McClient.minecraft.soundHandler

    fun create(name: String, pitch: Float, volume: Float = 50f): Sound {
        return object : PositionedSound(ResourceLocation(name)) {
            init {
                this.volume = volume
                this.repeat = false
                this.repeatDelay = 0
                this.attenuationType = ISound.AttenuationType.NONE
                this.pitch = pitch
            }
        }
    }

    fun playOnRepeat(name: String, pitch: Float, volume: Float = 50f, delay: Long, repeat: Int) =
        create(name, pitch, volume).playOnRepeat(delay, repeat)

    fun Sound.playOnRepeat(delay: Long, repeat: Int) {
        SkyHanniMod.coroutineScope.launch {
            repeat(repeat) {
                play()
                delay(delay)
            }
        }
    }

    fun play(name: String, pitch: Float, volume: Float = 50f) =
        create(name, pitch, volume).play()

    fun Sound.play() {
        McClient.schedule {
            val oldLevel = McClient.options.getSoundLevel(SoundCategory.PLAYERS)
            McClient.options.setSoundLevel(SoundCategory.PLAYERS, 1f)
            try {
                soundHandler?.playSound(this)
            } catch (e: Exception) {
                if (e is IllegalArgumentException && e.message?.startsWith("value already present:") == true) {
                    println("SkyHanni Sound error: ${e.message}")
                } else {
                    ErrorManager.logErrorWithData(
                        e, "Failed to play a sound",
                        "soundLocation" to this.soundLocation,
                    )
                }
            } finally {
                McClient.options.setSoundLevel(SoundCategory.PLAYERS, oldLevel)
            }
        }
    }
}
