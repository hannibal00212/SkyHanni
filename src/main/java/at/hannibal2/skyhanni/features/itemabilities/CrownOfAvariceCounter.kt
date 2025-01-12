package at.hannibal2.skyhanni.features.itemabilities

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.OwnInventoryItemUpdateEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getCoinsOfAvarice
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.inPartialHours
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CrownOfAvariceCounter {

    private val config get() = SkyHanniMod.feature.inventory.itemAbilities.crownOfAvarice

    private val internalName = "CROWN_OF_AVARICE".toInternalName()

    private var render: Renderable? = null
    private const val MAX_AVARICE = 1_000_000_000L
    private val MAX_AFK_TIME = 2.minutes

    private val isWearingCrown by RecalculatingValue(1.seconds) {
        InventoryUtils.getHelmet()?.getInternalNameOrNull() == internalName
    }

    private var count: Long = 0L
    private var coinsEarned: Long = 0L
    private var sessionStart = SimpleTimeMark.farPast()
    private var lastCoinUpdate = SimpleTimeMark.farPast()
    private val isSessionActive get(): Boolean = sessionStart.passedSince() < 10.seconds

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!isWearingCrown) return
        render?.let { config.position.renderRenderable(it, posLabel = "Crown of Avarice Counter") }
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

        val coinsDifference = coins - count

        if (coinsDifference == 0L) return

        if (coinsDifference < 0) {
            reset()
            count = coins
            return
        }

        if (lastCoinUpdate.passedSince() > MAX_AFK_TIME) reset()
        lastCoinUpdate = SimpleTimeMark.now()
        coinsEarned += coinsDifference
        count = coins

        update()
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        reset()
        count = InventoryUtils.getHelmet()?.getCoinsOfAvarice() ?: return
    }

    private fun update() {
        val coinsPerHour = calculateCoinsPerHour().toLong()
        val timeUntilMax = calculateTimeUntilMax()
        val list = buildList {
            add(
                Renderable.horizontalContainer(
                    listOf(
                        Renderable.itemStack(internalName.getItemStack()),
                        Renderable.string("§6" + if (config.shortFormat) count.shortFormat() else count.addSeparators()),
                    ),
                ),
            )

            if (config.perHour) {
                add(
                    Renderable.string(
                        "§aCoins Per Hour: §6${if (isSessionActive) "Calculating..." else coinsPerHour.addSeparators()}",
                    ),
                )
            }
            if (config.time) {
                add(Renderable.string("§aTime until Max: §6${if (isSessionActive) "Calculating..." else timeUntilMax}"))
            }
        }
        render = Renderable.verticalContainer(list)
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enable

    private fun reset() {
        coinsEarned = 0L
        sessionStart = SimpleTimeMark.now()
        lastCoinUpdate = SimpleTimeMark.now()
    }

    private fun calculateCoinsPerHour(): Double {
        val timeInHours = sessionStart.passedSince().inPartialHours
        return if (timeInHours > 0) coinsEarned / timeInHours else 0.0
    }

    private fun calculateTimeUntilMax(): String {
        val coinsPerHour = calculateCoinsPerHour()
        if (coinsPerHour == 0.0) return "Forever..."
        val timeUntilMax = ((MAX_AVARICE - count) / coinsPerHour).hours
        return timeUntilMax.format()
    }
}
