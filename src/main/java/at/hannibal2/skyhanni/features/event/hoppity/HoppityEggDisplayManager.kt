package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.event.hoppity.HoppityUnclaimedEggsConfig.UnclaimedEggsOrder.SOONEST_FIRST
import at.hannibal2.skyhanni.data.mob.MobFilter.isRealPlayer
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.render.EntityRenderLayersEvent
import at.hannibal2.skyhanni.features.fame.ReminderUtils
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI.partyModeReplace
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderDisplayHelper
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11

@SkyHanniModule
object HoppityEggDisplayManager {

    private val config get() = HoppityEggsManager.config
    private val unclaimedEggsConfig get() = config.unclaimedEggs
    private var shouldHidePlayer: Boolean = false

    var display = listOf<Renderable>()

    private fun canChangeOpacity(entity: EntityLivingBase): Boolean {
        if (!HoppityEggLocator.isEnabled()) return false
        if (entity !is EntityPlayer) return false
        if (entity == LorenzUtils.getPlayer()) return false
        if (!entity.isRealPlayer()) return false
        return config.playerOpacity < 100
    }

    @HandleEvent
    fun onPreRenderPlayer(event: SkyHanniRenderEntityEvent.Pre<EntityLivingBase>) {
        if (!canChangeOpacity(event.entity)) return

        shouldHidePlayer = HoppityEggLocator.sharedEggLocation?.let { event.entity.distanceTo(it) < 4.0 }
            ?: HoppityEggLocator.possibleEggLocations.any { event.entity.distanceTo(it) < 4.0 }

        if (!shouldHidePlayer) return
        if (config.playerOpacity <= 0) return event.cancel()

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.color(1.0f, 1.0f, 1.0f, config.playerOpacity / 100f)
    }

    @HandleEvent
    fun onPostRenderPlayer(event: SkyHanniRenderEntityEvent.Post<EntityLivingBase>) {
        if (!canChangeOpacity(event.entity)) return

        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
    }

    @HandleEvent
    fun onRenderPlayerLayers(event: EntityRenderLayersEvent.Pre<EntityLivingBase>) {
        if (!canChangeOpacity(event.entity)) return
        if (!shouldHidePlayer) return
        event.cancel()
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        display = updateDisplay()
    }

    private fun updateDisplay(): List<Renderable> {
        if (!HoppityEggsManager.isActive()) return emptyList()
        if (!unclaimedEggsConfig.enabled) return emptyList()
        if (ReminderUtils.isBusy() && !unclaimedEggsConfig.showWhileBusy) return emptyList()

        val displayList: List<String> = buildList {
            add("§bUnclaimed Eggs:")
            HoppityEggType.resettingEntries.filter {
                it.hasRemainingSpawns() // Only show eggs that have future spawns
            }.let { entries ->
                if (unclaimedEggsConfig.displayOrder == SOONEST_FIRST) entries.sortedBy { it.timeUntil() }
                else entries
            }.forEach { add("§7 - ${it.formattedName} ${it.timeUntil().format()}") }

            if (!unclaimedEggsConfig.showCollectedLocationCount || !LorenzUtils.inSkyBlock) return@buildList

            val totalEggs = HoppityEggLocations.islandLocations.size
            if (totalEggs > 0) {
                val collectedEggs = HoppityEggLocations.islandCollectedLocations.size
                val collectedFormat = formatEggsCollected(collectedEggs)
                add("§7Locations: $collectedFormat$collectedEggs§7/§a$totalEggs")
            }
        }.map { it.partyModeReplace() }

        if (displayList.size == 1) return emptyList()

        val container = Renderable.verticalContainer(displayList.map(Renderable::string))
        return listOf(
            if (unclaimedEggsConfig.warpClickEnabled) Renderable.clickAndHover(
                container,
                tips = listOf("§eClick to ${"/warp ${unclaimedEggsConfig.warpClickDestination}".trim()}!"),
                onClick = { HypixelCommands.warp(unclaimedEggsConfig.warpClickDestination) },
            ) else container,
        )
    }

    init {
        RenderDisplayHelper(
            outsideInventory = true,
            inOwnInventory = true,
            condition = { HoppityEggsManager.isActive() },
            onRender = {
                unclaimedEggsConfig.position.renderRenderables(display, posLabel = "Hoppity Eggs")
            },
        )
    }

    private fun formatEggsCollected(collectedEggs: Int): String =
        when (collectedEggs) {
            in 0 until 5 -> "§c"
            in 5 until 10 -> "§6"
            in 10 until 15 -> "§e"
            else -> "§a"
        }
}
