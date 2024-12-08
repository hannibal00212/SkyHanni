package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.inventory.InventoryConfig.ItemNumberEntry.CRIMSON_ARMOR
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.features.inventory.ItemDisplayOverlayFeatures.isSelected
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI.getKuudraTier
import at.hannibal2.skyhanni.features.nether.kuudra.KuudraAPI.isKuudraArmor
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getDungeonStarCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getStarCount
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ItemStars {

    private val config get() = SkyHanniMod.feature.inventory

    private val repoGroup = RepoPattern.group("inventory.itemstars")

    /**
     * REGEX-TEST: §6Ancient Terror Leggings §d✪✪§6✪✪✪
     * REGEX-TEST: §dRenowned Burning Crimson Helmet §6✪✪✪✪✪
     */
    private val starPattern by repoGroup.pattern(
        "stars",
        "^(?<name>.+) (?<stars>(?:(?:§.)?✪)+)",
    )

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        val stack = event.itemStack
        if (stack.stackSize != 1) return
        val stars = stack.grabStarCount() ?: return
        starPattern.findMatcher(stack.name) {
            val name = group("name")
            event.toolTip[0] = "$name §c$stars✪"
        }
    }

    @SubscribeEvent
    fun onRenderItemTip(event: RenderItemTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!CRIMSON_ARMOR.isSelected()) return
        val stack = event.stack
        if (stack.getInternalNameOrNull()?.isKuudraArmor() != true) return
        val stars = stack.grabStarCount() ?: return
        event.stackTip = stars.toString()
    }

    private fun ItemStack.grabStarCount(): Int? {
        val internalName = getInternalNameOrNull() ?: return null
        val baseStars = getDungeonStarCount() ?: getStarCount()
        if (!internalName.isKuudraArmor()) return baseStars
        val tier = internalName.getKuudraTier() ?: return baseStars
        return (baseStars ?: 0) + (tier - 1) * 10
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.itemStars
}
