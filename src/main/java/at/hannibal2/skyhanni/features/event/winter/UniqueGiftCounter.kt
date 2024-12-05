package at.hannibal2.skyhanni.features.event.winter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.WinterAPI
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object UniqueGiftCounter {

    private val config get() = SkyHanniMod.feature.event.winter.uniqueGiftCounter
    private val storage get() = ProfileStorageData.playerSpecific?.winter

    private val giftedAmountPattern by RepoPattern.pattern(
        "event.winter.uniqugifts.counter.amount",
        "§7Unique Players Gifted: §a(?<amount>.*)",
    )

    private var display = ""

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Generow") return
        val item = event.inventoryItems[40] ?: return

        val storage = storage ?: return

        giftedAmountPattern.firstMatcher(item.getLore()) {
            val amount = group("amount").formatInt()
            storage.amountGifted = amount
            update()
        }
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        update()
    }

    fun addUniqueGift() {
        val storage = storage ?: return
        storage.amountGifted++
        update()
    }

    private fun update() {
        val storage = storage ?: return

        val amountGifted = storage.amountGifted
        val max = 600
        val hasMax = amountGifted >= max
        val color = if (hasMax) "§a" else "§e"
        display = "§7Unique Players Gifted: $color$amountGifted/$max"
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        config.position.renderString(
            display,
            posLabel = "Unique Gift Counter",
        )
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled && WinterAPI.isDecember() &&
        InventoryUtils.itemInHandId.endsWith("_GIFT")
}
