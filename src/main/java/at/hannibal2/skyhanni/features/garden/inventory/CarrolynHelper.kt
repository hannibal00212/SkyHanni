package at.hannibal2.skyhanni.features.garden.inventory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.EntityMovementData
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ItemClickEvent
import at.hannibal2.skyhanni.events.item.ItemHoverEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack

@SkyHanniModule
object CarrolynHelper {
    private val config get() = GardenAPI.config

    private val carrolynLocation = LorenzVec(0.5, 103.1, -803.7)

    private val patternGroup = RepoPattern.group("garden.carrolyn")

    /**
     * REGEX-TEST: §7Bring §63,000 §7of these to §5Carrolyn §7in
     */
    private val lorePattern by patternGroup.pattern(
        "lore",
        "§7Bring §63,000 §7of these to §5Carrolyn §7in",
    )

    @HandleEvent(priority = HandleEvent.LOWEST)
    fun onTooltip(event: ItemHoverEvent) {
        if (!isEnabled()) return

        if (!event.itemStack.isCarrolynItem()) return

        event.toolTip.add("")
        event.toolTip.add("§eClick to navigate to Carrolyn!")
    }

    private fun ItemStack?.isCarrolynItem() = this?.getLore()?.any { lorePattern.matches(it) } ?: false

    @HandleEvent(onlyOnSkyblock = true)
    fun onItemClick(event: ItemClickEvent) {
        if (!isEnabled()) return

        if (!event.itemInHand.isCarrolynItem()) return

        if (LorenzUtils.skyBlockIsland == IslandType.CRIMSON_ISLE) {
            startPathfind()
        } else {
            ChatUtils.clickableChat(
                "Carrolyn is on the Crimson Isle. Click here to warp there!",
                onClick = {
                    HypixelCommands.warp("crimson")
                    EntityMovementData.onNextTeleport(IslandType.CRIMSON_ISLE) {
                        startPathfind()
                    }
                },
                replaceSameMessage = true,
            )
        }
    }

    private fun startPathfind() {
        IslandGraphs.pathFind(carrolynLocation, "§5Carrolyn", condition = { isEnabled() })
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.helpCarrolyn

}
