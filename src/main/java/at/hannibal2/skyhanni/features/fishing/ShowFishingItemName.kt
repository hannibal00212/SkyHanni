package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI.isBait
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object ShowFishingItemName {

    private val config get() = SkyHanniMod.feature.fishing.fishedItemName
    private var itemsOnGround = TimeLimitedCache<EntityItem, String>(750.milliseconds)

    // Taken from Skytils
    private val cheapCoins = setOf(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZhMDg3ZWI3NmU3Njg3YTgxZTRlZjgxYTdlNjc3MjY0OTk5MGY2MTY3Y2ViMGY3NTBhNGM1ZGViNmM0ZmJhZCJ9fX0=",
    )

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        for (entityItem in EntityUtils.getEntitiesNextToPlayer<EntityItem>(15.0)) {
            val itemStack = entityItem.entityItem
            // Hypixel sometimes replaces the bait item midair with a stone
            if (itemStack.name.removeColor() == "Stone") continue
            var text = ""

            val isBait = itemStack.isBait()
            if (isBait && !config.showBaits) continue

            if (itemStack.getSkullTexture() in cheapCoins) {
                text = "§6Coins"
            } else {
                val name = itemStack.name.transformIf({ isBait }) { "§7" + this.removeColor() }
                text += if (isBait) "§c§l- §r" else "§a§l+ §r"

                val size = itemStack.stackSize
                if (size != 1) text += "§7x$size §r"
                text += name
            }

            itemsOnGround[entityItem] = text
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return

        for ((item, text) in itemsOnGround) {
            val location = event.exactLocation(item).add(y = 0.8)
            event.drawString(location, text)
        }
    }

    fun inCorrectArea(): Boolean {
        if (IslandType.HUB.isInIsland()) {
            LorenzUtils.skyBlockArea?.let {
                if (it.endsWith(" Atrium")) return false
                if (it.endsWith(" Museum")) return false
                if (it == "Fashion Shop") return false
                if (it == "Shen's Auction") return false
            }
        }
        if (IslandType.THE_END.isInIsland()) return false
        return true
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled && FishingAPI.holdingRod && inCorrectArea()
}
