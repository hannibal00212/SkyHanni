package at.hannibal2.skyhanni.features.itemabilities

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addItemStack
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.billion
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getCoinsOfAvarice
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.inPartialHours
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.addLine
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CrownOfAvariceCounter {

    private val config get() = SkyHanniMod.feature.inventory.itemAbilities.crownOfAvarice

    private val internalName = "CROWN_OF_AVARICE".toInternalName()

    private var display: List<Renderable> = emptyList()
    private val MAX_AVARICE_COINS = 1.billion
    private val MAX_AFK_TIME = 2.minutes
    private val isWearingCrown by RecalculatingValue(1.seconds) {
        InventoryUtils.getHelmet()?.getInternalNameOrNull() == internalName
    }

    private var count: Long? = null
    private var coinsEarned: Long = 0L
    private var sessionStart: SimpleTimeMark? = null
    private var lastCoinUpdate: SimpleTimeMark? = null
    private val isSessionActive get(): Boolean = sessionStart?.passedSince()?.let { it < config.sessionActiveTime.seconds } ?: false
    private var coinsDifference: Long? = null

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!isWearingCrown) return
        config.position.renderRenderables(display, posLabel = "Crown of Avarice Counter")
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!isWearingCrown) return
        update()
    }

    @HandleEvent
    fun onInventoryUpdated(event: OwnInventoryItemUpdateEvent) {
        if (!isEnabled() || event.slot != 5) return
        val item = event.itemStack
        if (item.getInternalNameOrNull() != internalName) return
        val coins = item.getCoinsOfAvarice() ?: return
        if (count == 0L) count = coins
        coinsDifference = coins - (count ?: 0)

        if (coinsDifference == 0L) return

        if ((coinsDifference ?: 0) < 0) {
            reset()
            count = coins
            return
        }

        if (isSessionAFK()) reset()
        lastCoinUpdate = SimpleTimeMark.now()
        coinsEarned += coinsDifference ?: 0
        count = coins

        update()
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        reset()
        count = InventoryUtils.getHelmet()?.getCoinsOfAvarice()
    }

    private fun update() {
        display = buildList()
    }

    private fun buildList(): List<Renderable> = buildList {
        addLine {
            addItemStack(internalName.getItemStack())
            addString("§6" + if (config.shortFormat) count?.shortFormat() else count?.addSeparators())
        }

        if (config.perHour) {
            val coinsPerHour = calculateCoinsPerHour().toLong()
            addString(
                "§aCoins Per Hour: §6${
                    if (isSessionActive) "Calculating..."
                    else if (config.shortFormatCPH) coinsPerHour.shortFormat() else coinsPerHour.addSeparators()
                } " + if (isSessionAFK()) "§c(RESET)" else "",
            )

        }
        if (config.time) {
            val timeUntilMax = calculateTimeUntilMax()
            addString(
                "§aTime until Max: §6${if (isSessionActive) "Calculating..." else timeUntilMax} " + if (isSessionAFK()) "§c(RESET)" else "",
            )
        }
        if (config.coinDiff) {
            addString("§aLast coins gained: §6$coinsDifference")
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enable

    private fun reset() {
        coinsEarned = 0L
        sessionStart = SimpleTimeMark.now()
        lastCoinUpdate = SimpleTimeMark.now()
        coinsDifference = 0L
    }

    private fun calculateCoinsPerHour(): Double {
        val timeInHours = sessionStart?.passedSince()?.inPartialHours ?: 0.0
        return if (timeInHours > 0) coinsEarned / timeInHours else 0.0
    }

    private fun isSessionAFK() = lastCoinUpdate?.passedSince()?.let { it > MAX_AFK_TIME } ?: false

    private fun calculateTimeUntilMax(): String {
        val coinsPerHour = calculateCoinsPerHour()
        if (coinsPerHour == 0.0) return "Forever..."
        val timeUntilMax = ((MAX_AVARICE_COINS - (count ?: 0)) / coinsPerHour).hours
        return timeUntilMax.format()
    }
}
