package at.hannibal2.skyhanni.features.garden.visitor

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.garden.visitor.VisitorOpenEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Fixing the visitor detection problem with Anita and Jacob, as those two are on the garden twice when visiting.
 */
@SkyHanniModule
object NPCVisitorFix {
    private val staticVisitors = listOf("Jacob", "Anita")

    private val barnSkinChangePattern by RepoPattern.pattern(
        "garden.barn.skin.change",
        "§aChanging Barn skin to §r.*"
    )

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!GardenAPI.inGarden()) return
        val name = staticVisitors.firstOrNull { event.inventoryName.contains(it) } ?: return
        val nearest = findNametags(name).firstOrNull { it.distanceToPlayer() < 3 } ?: return
        DelayedRun.runDelayed(200.milliseconds) {
            saveStaticVisitor(name, nearest)
        }
    }

    private fun saveStaticVisitor(name: String, entity: EntityArmorStand) {
        // clicked on the real visitor, ignoring
        if (lastVisitorOpen.passedSince() < 1.seconds) return

        val storage = GardenAPI.storage ?: return

        val location = entity.getLorenzVec()
        storage.npcVisitorLocations[name]?.let {
            // alrady stored
            if (it.distance(location) < 1) return
        }

        storage.npcVisitorLocations[name] = location
        ChatUtils.chat("Saved $name NPC location. Real $name visitors are now getting detected correctly.")
    }

    private var lastVisitorOpen = SimpleTimeMark.farPast()

    @HandleEvent
    fun onVisitorOpen(event: VisitorOpenEvent) {
        lastVisitorOpen = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        barnSkinChangePattern.matchMatcher(event.message) {
            GardenAPI.storage?.npcVisitorLocations?.clear()
        }
    }

    fun findNametag(visitorName: String): EntityArmorStand? {
        val nametags = findNametags(visitorName)
        if (nametags.isEmpty()) return null

        if (visitorName !in staticVisitors) {
            return nametags[0]
        }

        val staticLocation = GardenAPI.storage?.npcVisitorLocations?.get(visitorName) ?: return null

        for (entity in nametags.toMutableList()) {
            val distance = entity.distanceTo(staticLocation)
            if (distance < 3) {
                nametags.remove(entity)
            }
        }

        return nametags.firstOrNull()
    }

    private fun findNametags(visitorName: String): MutableList<EntityArmorStand> {
        val foundVisitorNameTags = mutableListOf<EntityArmorStand>()
        for (entity in EntityUtils.getEntities<EntityArmorStand>()) {
            if (entity.name.removeColor() == visitorName) {
                foundVisitorNameTags.add(entity)
            }
        }
        return foundVisitorNameTags
    }
}
