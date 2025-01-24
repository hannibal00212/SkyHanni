package at.hannibal2.skyhanni.features.inventory.bazaar

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.jsonobjects.other.SkyblockItemsDataJson
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ApiUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.json.fromJson
import kotlinx.coroutines.launch

class HypixelItemApi {

    companion object {

        private var npcPrices = mapOf<NeuInternalName, Double>()

        fun getNpcPrice(internalName: NeuInternalName) = npcPrices[internalName]
    }

    private fun loadNpcPrices(): MutableMap<NeuInternalName, Double> {
        val list = mutableMapOf<NeuInternalName, Double>()
        val apiResponse = ApiUtils.getJSONResponse("https://api.hypixel.net/v2/resources/skyblock/items")
        try {
            val itemsData = ConfigManager.gson.fromJson<SkyblockItemsDataJson>(apiResponse)

            val motesPrice = mutableMapOf<NeuInternalName, Double>()
            val allStats = mutableMapOf<NeuInternalName, Map<String, Int>>()
            for (item in itemsData.items) {
                val neuItemId = NeuItems.transHypixelNameToInternalName(item.id ?: continue)
                item.npcPrice?.let { list[neuItemId] = it }
                item.motesPrice?.let { motesPrice[neuItemId] = it }
                item.stats?.let { stats -> allStats[neuItemId] = stats }
            }
            ItemUtils.updateBaseStats(allStats)
            RiftApi.motesPrice = motesPrice
        } catch (e: Throwable) {
            ErrorManager.logErrorWithData(
                e, "Error getting npc sell prices",
                "hypixelApiResponse" to apiResponse,
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
