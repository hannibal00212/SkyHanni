package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.ContributorJsonEntry
import at.hannibal2.skyhanni.data.jsonobjects.repo.ContributorsJson
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.entity.EntityDisplayNameEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText

@SkyHanniModule
object ContributorManager {
    private val config get() = SkyHanniMod.feature.dev

    // Key is the lowercase contributor name
    private var contributors: Map<String, ContributorJsonEntry> = emptyMap()

    // Just the names of the contributors including their proper case
    var contributorNames = emptyList<String>()
        private set

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val map = event.getConstant<ContributorsJson>("Contributors").contributors
        contributors = map.mapKeys { it.key.lowercase() }
        contributorNames = map.map { it.key }
    }

    @HandleEvent
    fun onRenderNametag(event: EntityDisplayNameEvent<EntityPlayer>) {
        if (!config.contributorNametags) return
        if (event.entity.isRealPlayer()) getSuffix(event.entity.name)?.let {
            event.chatComponent.appendSibling(ChatComponentText(" $it"))
        }
    }

    fun getSuffix(username: String): String? = getContributor(username)?.suffix

    fun shouldSpin(username: String): Boolean = getContributor(username)?.spinny ?: false
    fun shouldBeUpsideDown(username: String): Boolean = getContributor(username)?.upsideDown ?: false

    private fun getContributor(username: String) =
        contributors[username.lowercase()]?.let { it.takeIf { it.isAllowed() } }

    private fun ContributorJsonEntry.isAllowed(): Boolean {
        if (!config.fancyContributors) return false
        return when (externalMod) {
            // normal SkyHanni contributor
            null -> true

            // TODO add other mod's devs, e.g skytils

            "SBA" -> config.fancySbaContributors

            else -> false
        }
    }
}
