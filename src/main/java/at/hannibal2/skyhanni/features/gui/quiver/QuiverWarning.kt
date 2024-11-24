package at.hannibal2.skyhanni.features.gui.quiver

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.ArrowType
import at.hannibal2.skyhanni.data.QuiverAPI
import at.hannibal2.skyhanni.data.QuiverAPI.amount
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.DungeonCompleteEvent
import at.hannibal2.skyhanni.events.KuudraCompleteEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.QuiverUpdateEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.createCommaSeparatedList
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object QuiverWarning {

    private val config get() = SkyHanniMod.feature.combat.quiverConfig

    private var lastLowQuiverReminder = SimpleTimeMark.farPast()
    private val arrowsInInstance = mutableSetOf<ArrowType>()

    @SubscribeEvent
    fun onDungeonComplete(event: DungeonCompleteEvent) {
        onInstanceComplete()
    }

    @SubscribeEvent
    fun onKuudraComplete(event: KuudraCompleteEvent) {
        onInstanceComplete()
    }

    private fun onInstanceComplete() {
        val arrows = arrowsInInstance
        arrowsInInstance.clear()
        arrows.filter { it.amount <= config.lowQuiverAmount }

        if (arrows.isNotEmpty() && config.reminderAfterRun) {
            DelayedRun.runNextTick {
                instanceAlert(arrows)
            }
        }
    }

    private fun instanceAlert(arrows: Set<ArrowType>) {
        val arrowsText = arrows.map { arrowType ->
            val rarity = arrowType.internalName.getItemStackOrNull()?.getItemRarityOrNull()?.chatColorCode ?: "§f"
            "$rarity${arrowType.arrow}"
        }.createCommaSeparatedList()
        TitleManager.sendTitle("§cLow on arrows!", 5.seconds, 3.6, 7f)
        ChatUtils.chat("Low on $arrowsText!")
        SoundUtils.repeatSound(100, 30, SoundUtils.plingSound)
    }

    private fun lowQuiverAlert(amount: Int) {
        if (lastLowQuiverReminder.passedSince() < 30.seconds) return
        lastLowQuiverReminder = SimpleTimeMark.now()
        TitleManager.sendTitle("§cLow on arrows!", 5.seconds, 3.6, 7f)
        ChatUtils.chat("Low on arrows §e(${amount.addSeparators()} left)")
    }

    @SubscribeEvent
    fun onQuiverUpdate(event: QuiverUpdateEvent) {
        val amount = event.currentAmount
        val arrow = event.currentArrow ?: return
        if (arrow == QuiverAPI.NONE_ARROW_TYPE) return

        if (inInstance()) arrowsInInstance.add(arrow)

        if (amount > config.lowQuiverAmount) return
        if (config.lowQuiverNotification) {
            lowQuiverAlert(amount)
        }
    }

    @SubscribeEvent
    fun onWorldSwitch(event: LorenzWorldChangeEvent) {
        arrowsInInstance.clear()
    }

    private fun inInstance() = DungeonAPI.inDungeon() || KuudraAPI.inKuudra()

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(35, "inventory.quiverAlert", "combat.quiverConfig.lowQuiverNotification")
    }
}
