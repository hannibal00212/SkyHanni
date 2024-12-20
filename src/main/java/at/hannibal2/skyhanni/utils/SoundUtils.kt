package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.test.command.ErrorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.ISound
import net.minecraft.client.audio.PositionedSound
import net.minecraft.client.audio.SoundCategory
import net.minecraft.util.ResourceLocation

object SoundUtils {

    private val beepSound by lazy { createSound("random.orb", 1f) }
    private val clickSound by lazy { createSound("gui.button.press", 1f) }
    private val errorSound by lazy { createSound("mob.endermen.portal", 0f) }
    val plingSound by lazy { createSound("note.pling", 1f) }
    val centuryActiveTimerAlert by lazy { createSound("skyhanni:centurytimer.active", 1f) }

    fun ISound.playSound() {
        DelayedRun.onThread.execute {
            val gameSettings = Minecraft.getMinecraft().gameSettings
            val oldLevel = gameSettings.getSoundLevel(SoundCategory.PLAYERS)
            if (!SkyHanniMod.feature.misc.maintainGameVolume) {
                gameSettings.setSoundLevel(SoundCategory.PLAYERS, 1f)
            }
            try {
                Minecraft.getMinecraft().soundHandler.playSound(this)
            } catch (e: IllegalArgumentException) {
                if (e.message?.startsWith("value already present:") == true) return@execute
                ErrorManager.logErrorWithData(e, "Failed to play a sound", "soundLocation" to this.soundLocation)
            } catch (e: Exception) {
                ErrorManager.logErrorWithData(e, "Failed to play a sound", "soundLocation" to this.soundLocation)
            } finally {
                if (!SkyHanniMod.feature.misc.maintainGameVolume) {
                    gameSettings.setSoundLevel(SoundCategory.PLAYERS, oldLevel)
                }
            }
        }
    }

    fun createSound(name: String, pitch: Float, volume: Float = 50f): ISound {
        val sound: ISound = object : PositionedSound(ResourceLocation(name)) {
            init {
                this.volume = volume
                repeat = false
                repeatDelay = 0
                attenuationType = ISound.AttenuationType.NONE
                this.pitch = pitch
            }
        }
        return sound
    }

    fun playBeepSound() {
        beepSound.playSound()
    }

    fun playClickSound() {
        clickSound.playSound()
    }

    fun playPlingSound() {
        plingSound.playSound()
    }

    fun command(args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.userError("Specify a sound effect to test")
            return
        }

        val soundName = args[0]
        val pitch = args.getOrNull(1)?.toFloat() ?: 1.0f
        val volume = args.getOrNull(2)?.toFloat() ?: 50.0f

        createSound(soundName, pitch, volume).playSound()
    }

    fun playErrorSound() {
        errorSound.playSound()
    }

    // TODO use duration for delay
    fun repeatSound(delay: Long, repeat: Int, sound: ISound) {
        SkyHanniMod.coroutineScope.launch {
            repeat(repeat) {
                sound.playSound()
                delay(delay)
            }
        }
    }
}
