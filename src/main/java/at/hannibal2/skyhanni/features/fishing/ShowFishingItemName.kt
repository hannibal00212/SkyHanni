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
import at.hannibal2.skyhanni.utils.SkullTextureHolder
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.entity.item.EntityItem
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object ShowFishingItemName {

    private val config get() = SkyHanniMod.feature.fishing.fishedItemName
    private val itemsOnGround = TimeLimitedCache<EntityItem, String>(750.milliseconds)

    // Textures taken from Skytils - moved to REPO
    private val cheapCoins by lazy {
        setOf(
            SkullTextureHolder.getTexture("COINS_1"),
            SkullTextureHolder.getTexture("COINS_2"),
        )
    }

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
            val location = event.exactLocation(item).up(0.8)
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
