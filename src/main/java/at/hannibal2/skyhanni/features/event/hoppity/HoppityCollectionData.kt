package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuHoppityJson
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats.RabbitCollectionRarity
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule

@SkyHanniModule
object HoppityCollectionData {
    val rabbitRarities = mutableMapOf<String, RabbitCollectionRarity>()
    private val rarityBonuses = mutableMapOf<RabbitCollectionRarity, ChocolateBonuses>()
    private val specialBonuses = mutableMapOf<String, ChocolateBonuses>()

    val knownRabbitCount
        get() = rabbitRarities.size

    fun getRarity(rabbit: String): RabbitCollectionRarity? {
        val apiName = rabbit.toApiName()
        return rabbitRarities[apiName]
    }

    fun knownRabbitsOfRarity(rarity: RabbitCollectionRarity): Int =
        rabbitRarities.filterValues { it == rarity }.count()

    fun isKnownRabbit(rabbit: String) = rabbitRarities.contains(rabbit.toApiName())

    fun getChocolateBonuses(rabbit: String): ChocolateBonuses {
        val apiName = rabbit.toApiName()
        val rarity = rabbitRarities[apiName]
        return specialBonuses[apiName]
            ?: rarityBonuses[rarity]
            ?: ChocolateBonuses(0, 0.0)
    }

    private fun String.toApiName(): String = when (this) {
        "Fish the Rabbit" -> "fish"
        else -> lowercase().replace("[- ]".toRegex(), "_")
    }

    @HandleEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        rabbitRarities.clear()
        rarityBonuses.clear()
        specialBonuses.clear()

        val data = event.readConstant<NeuHoppityJson>("hoppity").hoppity
        for ((rarityString, rarityData) in data.rarities.entries) {
            val rarity = RabbitCollectionRarity.valueOf(rarityString.uppercase())

            for (rabbit in rarityData.rabbits) {
                rabbitRarities[rabbit] = rarity
            }

            rarityBonuses[rarity] = ChocolateBonuses(rarityData.chocolate, rarityData.multiplier)
        }

        data.special.forEach { (rabbit, data) ->
            specialBonuses[rabbit] = ChocolateBonuses(data.chocolate, data.multiplier)
        }
    }

    data class ChocolateBonuses(val chocolate: Int, val multiplier: Double)
}
