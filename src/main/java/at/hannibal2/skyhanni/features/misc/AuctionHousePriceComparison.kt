package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.features.inventory.AuctionsHighlighter
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValueCalculator
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.toChromaColor
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import net.minecraft.client.player.inventory.ContainerLocalMenu
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyHanniModule
object AuctionHousePriceComparison {

    private val config get() = SkyHanniMod.feature.inventory.auctions.auctionsPriceComparison

    private var slotPriceMap = mapOf<Int, Long>()
    private var bestPrice = 0L
    private var worstPrice = 0L
    private var inInventory = false

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        inInventory = false
        if (!event.inventoryName.startsWith("Auctions")) return
        inInventory = true

        bestPrice = 0L
        worstPrice = 0L

        val map = mutableMapOf<Int, Long>()

        for ((slot, stack) in event.inventoryItems) {
            for (line in stack.getLore()) {
                AuctionsHighlighter.buyItNowPattern.matchMatcher(line) {
                    map.add(stack, group("coins").formatLong(), slot)
                }
                AuctionsHighlighter.auctionPattern.matchMatcher(line) {
                    map.add(stack, group("coins").formatLong(), slot)
                }
            }
        }
        this.slotPriceMap = map
    }

    private fun MutableMap<Int, Long>.add(stack: ItemStack, binPrice: Long, slot: Int) {
        val (totalPrice, basePrice) = EstimatedItemValueCalculator.calculate(stack, mutableListOf())
        if (totalPrice == basePrice) return
        val estimatedPrice = totalPrice.toLong()

        val diff = estimatedPrice - binPrice
        this[slot] = diff
        if (diff >= 0) {
            if (diff > bestPrice) {
                bestPrice = diff
            }
        } else {
            if (diff < worstPrice) {
                worstPrice = diff
            }
        }
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!isEnabled()) return

        val good = config.good.toChromaColor()
        val veryGood = config.veryGood.toChromaColor()

        val bad = config.bad.toChromaColor()
        val veryBad = config.veryBad.toChromaColor()


        for (slot in InventoryUtils.getItemsInOpenChest()) {
            val diff = slotPriceMap[slot.slotIndex] ?: continue
            if (diff == 0L) {
                slot highlight good
                continue
            }
            val isGood = diff >= 0
            val percentage = if (isGood) {
                diff.toDouble() / bestPrice
            } else {
                -diff.toDouble() / -worstPrice
            }
            val color = if (isGood) {
                getColorInBetween(good, veryGood, percentage)
            } else {
                getColorInBetween(bad, veryBad, percentage)
            }
            slot highlight color
        }
    }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return

        val diff = slotPriceMap[event.slot.slotIndex] ?: return
        if (event.slot.inventory !is ContainerLocalMenu) return

        event.toolTip.add("")
        if (diff >= 0) {
            event.toolTip.add("§aThis item is §6${diff.addSeparators()} coins §acheaper")
            event.toolTip.add("§athan the estimated item value!")
        } else {
            event.toolTip.add("§cThis item is §6${(-diff).addSeparators()} coins §cmore")
            event.toolTip.add("§cexpensive than the estimated item value!")
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(31, "inventory.auctionsPriceComparison", "inventory.auctions.auctionsPriceComparison")
    }

    private fun getColorInBetween(color1: Color, color2: Color, percentage: Double): Color {
        val r1 = color1.red
        val g1 = color1.green
        val b1 = color1.blue

        val r2 = color2.red
        val g2 = color2.green
        val b2 = color2.blue

        val newRed = (lerp(percentage, r1, r2)).toInt().coerceIn(0, 255)
        val newGreen = (lerp(percentage, g1, g2)).toInt().coerceIn(0, 255)
        val newBlue = (lerp(percentage, b1, b2)).toInt().coerceIn(0, 255)

        return Color(newRed, newGreen, newBlue)
    }

    private fun lerp(delta: Double, start: Int, end: Int) = start + delta * (end - start)

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled && inInventory
}
