package at.hannibal2.skyhanni.features.slayer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.SlayerApi
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SlayerItemsOnGround {

    private val config get() = SkyHanniMod.feature.slayer.itemsOnGround

    private val itemsOnGround = TimeLimitedCache<EntityItem, String>(2.seconds)

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        if (!isEnabled()) return
        for (entityItem in EntityUtils.getEntitiesNextToPlayer<EntityItem>(15.0)) {
            val itemStack = entityItem.entityItem
            if (itemStack.item == Items.spawn_egg) continue
            if (itemStack.getInternalName() == NeuInternalName.NONE) continue
            val (name, price) = SlayerApi.getItemNameAndPrice(itemStack.getInternalName(), itemStack.stackSize)
            if (config.minimumPrice > price) continue
            itemsOnGround[entityItem] = name
        }
    }

    @HandleEvent
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!isEnabled()) return

        for ((item, text) in itemsOnGround) {
            val location = event.exactLocation(item).up(0.8)
            event.drawString(location, text)
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled &&
        SlayerApi.isInCorrectArea && SlayerApi.hasActiveSlayerQuest()
}
