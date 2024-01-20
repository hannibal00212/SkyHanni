package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class AbiphoneContactSlots {
    private val ALL_UPGRADES_TOTAL: Int = 40
    private val maximumContactSlotsLoreLinePattern by RepoPattern.pattern(
        "abiphone.contact.slots/maximumcontactslots.loreline",
        " ?§7Maximum Contacts: (?:§.)*(?<first>\\d+)(?: |§.)*(?:[(\\[]\\+?(?<second>\\d+)[)\\]])?(?: |§.)*(?:[(\\[]\\+?(?<third>\\d+)[)\\]])?(?: |§.)*(?:[(\\[]\\+?(?<fourth>\\d+)[)\\]])?(?: |§.)*(?:[(\\[]\\+?(?<fifth>\\d+)[)\\]])?"
    )

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (!isEnabled()) return
        if (Minecraft.getMinecraft().currentScreen !is GuiInventory) return
        val itemStack = event.itemStack
        if (itemStack.getItemCategoryOrNull() != ItemCategory.ABIPHONE) return
        val itemLore = itemStack.getLore()
        val trueIndex = itemLore.indexOfFirst { maximumContactSlotsLoreLinePattern.matches(it) }
        if (trueIndex == -1 || trueIndex + 2 !in itemLore.indices) return
        maximumContactSlotsLoreLinePattern.matchMatcher(itemLore[trueIndex]) {
            val upgrades = listOf<String>(group("first") ?: "0", group("second") ?: "0", group("third") ?: "0", group("fourth") ?: "0", group("fifth") ?: "0")
            var total = 0
            upgrades.forEach { total += it.toIntOrNull() ?: 0 }
            if (total > 0) {
                event.toolTip.add(
                    trueIndex + 2,
                    " §7Contacts Progress: §b$total§7/§b${(ALL_UPGRADES_TOTAL + upgrades.first().toInt())}"
                )
            } else {
                event.toolTip.add(trueIndex + 2, " §8Could not calculate contact slots. [SkyHanni]")
            }
        }
    }
    private fun isEnabled(): Boolean = LorenzUtils.inSkyBlock && SkyHanniMod.feature.misc.abiphoneContactsProgress
}
