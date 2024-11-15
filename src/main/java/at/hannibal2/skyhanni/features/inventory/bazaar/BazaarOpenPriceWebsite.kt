package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.entity.player.InventoryPlayer
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BazaarOpenPriceWebsite {

    private val config get() = SkyHanniMod.feature.inventory.bazaar
    private var lastClick = SimpleTimeMark.farPast()

    private val item by lazy {
        val neuItem = "PAPER".toInternalName().getItemStack()
        ItemUtils.createItemStack(
            neuItem.item,
            "§bPrice History",
            "§8(From SkyHanni)",
            "",
            "§7Click here to open",
            "§7the price history",
            "§7on §cskyblock.bz",
        )
    }

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!isEnabled()) return
        BazaarApi.currentlyOpenedProduct ?: return
        if (event.inventory is InventoryPlayer) return

        if (event.slot == 22) {
            event.replace(item)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        val lastItem = BazaarApi.currentlyOpenedProduct ?: return

        if (event.slotId == 22) {
            event.cancel()
            if (lastClick.passedSince() > 0.3.seconds) {
                val name = getSkyBlockBzName(lastItem)
                OSUtils.openBrowser("https://www.skyblock.bz/product/$name")
                lastClick = SimpleTimeMark.now()
            }
        }
    }

    private fun getSkyBlockBzName(internalName: NEUInternalName): String {
        val name = internalName.asString()
        return if (name.contains(";")) {
            "ENCHANTMENT_" + name.replace(";", "_")
        } else name
    }

    fun isEnabled() = config.openPriceWebsite
}
