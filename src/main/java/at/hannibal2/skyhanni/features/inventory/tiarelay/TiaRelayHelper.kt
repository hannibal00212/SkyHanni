package at.hannibal2.skyhanni.features.inventory.tiarelay

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.events.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object TiaRelayHelper {

    private val config get() = SkyHanniMod.feature.inventory.helper.tiaRelay
    private var inInventory = false

    private var lastClickSlot = 0
    private var lastClickTime = SimpleTimeMark.farPast()
    private val sounds = mutableMapOf<Int, Sound>()

    private val resultDisplay = mutableMapOf<Int, Int>()

    @SubscribeEvent
    fun onPlaySound(event: PlaySoundEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val soundName = event.soundName

        if (config.tiaRelayMute && soundName == "mob.wolf.whine") {
            event.cancel()
        }

        if (!config.soundHelper) return
        if (!inInventory) return

        val distance = event.distanceToPlayer
        if (distance >= 2) return

        if (lastClickSlot == 0) return
        if (lastClickTime.passedSince() > 1.minutes) return
        if (sounds.contains(lastClickSlot)) return

        sounds[lastClickSlot] = Sound(soundName, event.pitch)

        lastClickSlot = 0

        tryResult()
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.soundHelper) return

        if (InventoryUtils.openInventoryName().contains("Network Relay")) {
            inInventory = true
        } else {
            inInventory = false
            sounds.clear()
            resultDisplay.clear()
        }
    }

    private fun tryResult() {
        if (sounds.size < 4) return

        val name = sounds.values.first().name
        for (sound in sounds.toMutableMap()) {
            if (sound.value.name != name) {
                ChatUtils.userError("Tia Relay Helper error: Too much background noise! Try turning off the music and then try again.")
                ChatUtils.clickableChat("Click here to run /togglemusic", onClick = {
                    HypixelCommands.toggleMusic()
                }, "§eClick to run /togglemusic!")
                sounds.clear()
                return
            }
        }

        val pitchMap = mutableMapOf<Int, Float>()
        for (sound in sounds) {
            pitchMap[sound.key] = sound.value.pitch
        }
        sounds.clear()
        resultDisplay.clear()

        var i = 1
        for (entry in pitchMap.sorted()) {
            resultDisplay[entry.key] = i
            i++
        }
    }

    @SubscribeEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.soundHelper) return
        if (!inInventory) return

        val slot = event.slot
        val stack = slot.stack

        val slotNumber = slot.slotNumber

        val position = resultDisplay.getOrDefault(slotNumber, null)
        if (position != null) {
            if (stack.getLore().any { it.contains("Done!") }) {
                resultDisplay.clear()
                return
            }
            event.stackTip = "#$position"
            return
        }

        if (!sounds.contains(slotNumber) && stack.getLore().any { it.contains("Hear!") }) {
            event.stackTip = "Hear!"
            event.offsetX = 5
            event.offsetY = -5
            return
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.soundHelper) return
        if (!inInventory) return

        // only listen to right clicks
        if (event.clickedButton != 1) return

        lastClickSlot = event.slotId
        lastClickTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "misc.tiaRelayMute", "inventory.helper.tiaRelay.tiaRelayMute")
        event.move(2, "misc.tiaRelayHelper", "inventory.helper.tiaRelay.soundHelper")

        event.move(2, "misc.tiaRelayNextWaypoint", "inventory.helper.tiaRelay.nextWaypoint")
        event.move(2, "misc.tiaRelayAllWaypoints", "inventory.helper.tiaRelay.allWaypoints")
    }

    class Sound(val name: String, val pitch: Float)
}
