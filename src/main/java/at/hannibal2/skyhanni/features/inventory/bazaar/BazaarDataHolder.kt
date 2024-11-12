package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.jsonobjects.other.SkyblockItemsDataJson
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.json.fromJson
import kotlinx.coroutines.launch

class BazaarDataHolder {

    companion object {

        private var npcPrices = mapOf<NEUInternalName, Double>()

        fun getNpcPrice(internalName: NEUInternalName) = npcPrices[internalName]
    }

    private fun loadNpcPrices(): MutableMap<NEUInternalName, Double> {
        val list = mutableMapOf<NEUInternalName, Double>()
        val apiResponse = APIUtils.getJSONResponse("https://api.hypixel.net/v2/resources/skyblock/items")
        try {
            val itemsData = ConfigManager.gson.fromJson<SkyblockItemsDataJson>(apiResponse)

            val motesPrice = mutableMapOf<NEUInternalName, Double>()
            for (item in itemsData.items) {
                val neuItemId = NEUItems.transHypixelNameToInternalName(item.id ?: continue)
                item.npcPrice?.let { list[neuItemId] = it }
                item.motesPrice?.let { motesPrice[neuItemId] = it }
            }
            RiftAPI.motesPrice = motesPrice
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(
                e, "Error getting npc sell prices",
                "hypixelApiResponse" to apiResponse
            )
        }
        return list
    }

    fun start() {
        SkyHanniMod.coroutineScope.launch {
            npcPrices = loadNpcPrices()
        }

        // TODO use SecondPassedEvent
    }

}
