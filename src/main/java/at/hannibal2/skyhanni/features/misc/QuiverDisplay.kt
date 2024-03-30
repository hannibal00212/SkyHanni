package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.gui.QuiverDisplayConfig.ShowWhen
import at.hannibal2.skyhanni.data.ArrowType
import at.hannibal2.skyhanni.data.QuiverAPI
import at.hannibal2.skyhanni.data.QuiverAPI.NONE_ARROW_TYPE
import at.hannibal2.skyhanni.data.QuiverAPI.arrowAmount
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DungeonCompleteEvent
import at.hannibal2.skyhanni.events.DungeonEnterEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.KuudraCompleteEvent
import at.hannibal2.skyhanni.events.KuudraEnterEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.QuiverUpdateEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.createCommaSeparatedList
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

class QuiverDisplay {

    private val config get() = SkyHanniMod.feature.gui.quiverDisplay

    private var display = emptyList<Renderable>()
    private var arrow: ArrowType? = null
    private var amount = QuiverAPI.currentAmount
    private var rarity = "§f"
    private var lastLowQuiverReminder = SimpleTimeMark.farPast()
    private var hideAmount = false
    private var arrowsUsedInRun = mutableListOf<ArrowType>()
    private var arrowsToAlert = mutableListOf<String>()
    private var inInstance = false

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = emptyList()
        arrow = QuiverAPI.currentArrow
        amount = QuiverAPI.currentAmount
        updateDisplay()
    }

    @SubscribeEvent
    fun onWorldSwitch(event: LorenzWorldChangeEvent) {
        arrowsUsedInRun = mutableListOf()
        arrowsToAlert = mutableListOf()
        inInstance = false
    }

    @SubscribeEvent
    fun onDungeonEnter(event: DungeonEnterEvent) {
        onInstanceEnter()
    }

    @SubscribeEvent
    fun onKuudraEnter(event: KuudraEnterEvent) {
        onInstanceEnter()
    }

    private fun onInstanceEnter() {
        arrowsUsedInRun = mutableListOf()
        arrowsToAlert = mutableListOf()
        inInstance = true
    }

    @SubscribeEvent
    fun onDungeonComplete(event: DungeonCompleteEvent) {
        onInstanceComplete()
    }

    @SubscribeEvent
    fun onKuudraComplete(event: KuudraCompleteEvent) {
        onInstanceComplete()
    }

    private fun onInstanceComplete() {
        if (!config.reminderAfterRun) return
        if (arrowsUsedInRun.isEmpty()) return
        for (arrow in arrowsUsedInRun) {
            if ((arrowAmount[arrow.internalName] ?: return) <= config.lowQuiverAmount) {
                arrowsToAlert.add(arrow.arrow)
            }
        }
        if (arrowsToAlert.isNotEmpty()) instanceAlert()
    }

    private fun instanceAlert() {
        DelayedRun.runNextTick {
            TitleManager.sendTitle("§cLow on arrows!", 5.seconds, 3.6, 7f)
            ChatUtils.chat("Low on ${arrowsToAlert.createCommaSeparatedList()}!")
            SoundUtils.repeatSound(100, 30, SoundUtils.plingSound)
        }
    }

    private fun lowQuiverAlert() {
        if (lastLowQuiverReminder.passedSince() < 30.seconds) return
        lastLowQuiverReminder = SimpleTimeMark.now()
        TitleManager.sendTitle("§cLow on $rarity${arrow?.arrow}!", 5.seconds, 3.6, 7f)
        ChatUtils.chat("Low on $rarity${arrow?.arrow} §e($amount left)")
    }

    private fun updateDisplay() {
        display = drawDisplay()
    }

    private fun drawDisplay() = buildList {
        val arrow = arrow ?: return@buildList
        val itemStack = NEUItems.getItemStackOrNull(arrow.internalName.asString()) ?: ItemStack(Items.arrow)

        rarity = itemStack.getItemRarityOrNull()?.chatColorCode ?: "§f"
        val arrowDisplayName = if (hideAmount || arrow == NONE_ARROW_TYPE) arrow.arrow else StringUtils.pluralize(amount, arrow.arrow)

        if (config.showIcon.get()) {
            add(Renderable.itemStack(itemStack,1.68))
        }
        if (!hideAmount) {
            add(Renderable.string(" §b${amount}x"))
        }
        add(Renderable.string(" $rarity$arrowDisplayName"))
    }

    @SubscribeEvent
    fun onQuiverUpdate(event: QuiverUpdateEvent) {
        val lastArrow = arrow
        val lastAmount = amount

        arrow = event.currentArrow
        amount = event.currentAmount
        hideAmount = event.hideAmount

        updateDisplay()

        if (config.lowQuiverNotification && amount <= config.lowQuiverAmount) {
            if (arrow != lastArrow || (arrow == lastArrow && amount <= lastAmount)) lowQuiverAlert()
        }

        if (inInstance) {
            if (!arrowsUsedInRun.contains(arrow)) arrowsUsedInRun.add(arrow ?: return)
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (display.isEmpty()) updateDisplay()
        val whenToShow = config.whenToShow.get()
        if (whenToShow == ShowWhen.ALWAYS ||
            whenToShow == ShowWhen.ONLY_BOW_INVENTORY && QuiverAPI.hasBowInInventory() ||
            whenToShow == ShowWhen.ONLY_BOW_HAND && QuiverAPI.isHoldingBow()) {
            config.quiverDisplayPos.renderStringsAndItems(listOf(display), posLabel = "Quiver Display")
        }
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(
            config.whenToShow,
            config.showIcon
        ) {
            updateDisplay()
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(31,"config.inventory.quiverAlert","config.gui.quiverDisplay.lowQuiverNotification")
    }
}
