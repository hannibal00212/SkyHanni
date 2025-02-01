package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.SoundUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SpringBootsHelper {

    private val config get() = SkyHanniMod.feature.dungeon

    /**
     * Two [PlaySoundEvent] get created roughly every 100ms. This tracks the amount of times we heard the `note.pling` sound while wearing Spring Boots and sneaking.
     */
    private var soundStreak = 0

    @SubscribeEvent
    fun onSound(event: PlaySoundEvent) {
        if (!isEnabled()) return
        if (!(event.soundName == "note.pling" || event.soundName == "fireworks.launch" || event.soundName == "random.eat")) return
        if (InventoryUtils.getBoots()?.getInternalName()?.asString() != "SPRING_BOOTS") return
        if (event.soundName == "fireworks.launch" || event.soundName == "random.eat") {
            soundStreak = 0
            return
        }
        if (!Minecraft.getMinecraft().thePlayer.isSneaking) return
        soundStreak += 1

        if (soundStreak == 5) { // 7-8 seems to be perfect but subtract a bit of reaction speed
            TitleManager.sendTitle("§cSpring Boots ready!", 2.seconds, 3.6, 7.0f)
            SoundUtils.playBeepSound()
        }
    }

    private fun inDungeon(): Boolean {
        if (!DungeonAPI.inDungeon()) return false
        if (!DungeonAPI.inBossRoom) return false
        if (!DungeonAPI.isOneOf("F7", "M7")) return false

        return true
    }

    fun isEnabled() = inDungeon() && config.springBootsNotification
}
