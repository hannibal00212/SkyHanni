package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.data.jsonobjects.repo.HoppityRabbitTextureEntry
import at.hannibal2.skyhanni.data.jsonobjects.repo.HoppityRabbitTexturesJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzRarity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HoppityTextureHandler {

    private var hoppityRabbitTextures = mutableMapOf<LorenzRarity, List<HoppityRabbitTextureEntry>>()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<HoppityRabbitTexturesJson>("HoppityRabbitTextures")
        hoppityRabbitTextures = data.textures.mapNotNull { (key, entries) ->
            val rarity = LorenzRarity.getByName(key) ?: return@mapNotNull null
            rarity to entries
        }.toMap().toMutableMap()
    }

    /**
     * Get the rarity for a given Skull ID.
     * @param skullId The Skull ID to search for.
     * @return The rarity or null if no rabbit was found.
     */
    fun getRarityBySkullId(skullId: String): LorenzRarity? {
        return hoppityRabbitTextures.entries.firstOrNull { (_, entries) ->
            entries.any { it.skullId == skullId }
        }?.key
    }

    /**
     * Get the rabbit for a given Skull ID. If more than one rabbit is found, null is returned.
     * @param skullId The Skull ID to search for.
     * @return The rabbit name or null if no rabbit was found or more than one rabbit was found.
     */
    fun getRabbitBySkullId(skullId: String): String? =
        hoppityRabbitTextures.values.flatten().firstOrNull {
            it.skullId == skullId
        }?.rabbits?.takeIf {
            it.size == 1
        }?.firstOrNull()

    /**
     * Get the Texture ID for a given Rabbit name.
     * @param rabbit The Rabbit name to search for.
     * @return The Texture ID or null if no rabbit was found.
     */
    fun getTextureIdByRabbit(rabbit: String): String? =
        hoppityRabbitTextures.values.flatten().firstOrNull {
            rabbit in it.rabbits
        }?.textureId

}
