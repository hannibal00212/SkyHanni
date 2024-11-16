package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.garden.CropAccessory
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CropAccessoryData {

    /**
     * REGEX-TEST: Accessory Bag (1/2)
     */
    private val accessoryBagNamePattern by RepoPattern.pattern(
        "data.accessory.bagname.new",
        "Accessory Bag.*",
    )

    private var accessoryInBag = CropAccessory.NONE
    private var accessoryInInventory = CropAccessory.NONE

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        accessoryInBag = CropAccessory.NONE
        accessoryInInventory = CropAccessory.NONE
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryUpdatedEvent) {
        if (!accessoryBagNamePattern.matches(event.inventoryName)) return

        val items = event.inventoryItems.mapNotNull { it.value }
        val bestInPage = bestCropAccessory(items)
        if (bestInPage > accessoryInBag) {
            accessoryInBag = bestInPage
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!event.repeatSeconds(5)) return

        accessoryInInventory = bestCropAccessory(InventoryUtils.getItemsInOwnInventory())

        val bestAccessory = maxOf(accessoryInInventory, accessoryInBag)
        if (bestAccessory > cropAccessory) {
            cropAccessory = bestAccessory
        }
    }

    private fun bestCropAccessory(items: List<ItemStack>) =
        items.mapNotNull { item -> CropAccessory.getByName(item.getInternalName()) }
            .maxOrNull() ?: CropAccessory.NONE

    var cropAccessory: CropAccessory
        get() = GardenAPI.storage?.savedCropAccessory ?: CropAccessory.NONE
        private set(accessory) {
            GardenAPI.storage?.savedCropAccessory = accessory
        }
}
