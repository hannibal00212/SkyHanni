package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.menu

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.InventoryConfig
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class MenuItemDisplayOverlayMining {
    private val genericPercentPattern = ".* (§.)?(?<percent>[0-9]+)(\\.[0-9]*)?(§.)?%".toPattern()
    private val xOutOfYNoColorRequiredPattern = ".* (§.)?(?<useful>[0-9]+)(§.)?\\/(§.)?(?<total>[0-9]+).*".toPattern()

    @SubscribeEvent
    fun onRenderItemTip(event: RenderItemTipEvent) {
        event.stackTip = getStackTip(event.stack)
    }

    private fun getStackTip(item: ItemStack): String {
        if (SkyHanniMod.feature.inventory.stackSize.menu.mining.isEmpty()) return ""
        val stackSizeConfig = SkyHanniMod.feature.inventory.stackSize.menu.mining
        val chestName = InventoryUtils.openInventoryName()
        
        if (stackSizeConfig.contains(InventoryConfig.StackSizeConfig.MenuConfig.Mining.CURRENT_SKYMALL_PERK) && (item.cleanName().contains("Sky Mall")) && (chestName == "Heart of the Mountain")) {
            val lore = item.getLore()
            if (lore.last().contains("Right-click to ") && lore.last().contains("disable")) {
                // §8 ? §7Gain §a+100 §6? Mining Speed§7.§r
                /*
                "§8 ■ §7Gain §a+100 §6⸕ Mining Speed§7." --> " ■ Gain +100 ⸕ Mining Speed."
                "§8 ■ §7Gain §a+50 §6☘ Mining Fortune§7." --> " ■ Gain +50 ☘ Mining Fortune."
                "§8 ■ §7Gain §a+15% §7more Powder while" --> " ■ Gain +15% more Powder while"
                "§8 ■ §7Reduce Pickaxe Ability cooldown" --> " ■ Reduce Pickaxe Ability cooldown"
                "§8 ■ §7§a10x §7chance to find Goblins" --> " ■ 10x chance to find Goblins"
                "§8 ■ §7Gain §a5x §9Titanium §7drops." --> " ■ Gain 5x Titanium drops."
                "§aYour Current Effect" --> "Your Current Effect"
                */
                var currentEffectLineLocated = false
                for (line in lore) {
                    if (line.contains("Your Current Effect")) {
                        currentEffectLineLocated = true
                    }
                    if (currentEffectLineLocated && line.contains(" ■ ")) {
                        return when (line.removeColor().replace(" ■ ", "").replace(".", "")) {
                            "Gain +100 ⸕ Mining Speed" -> return "§a+§6⸕"
                            "Gain +50 ☘ Mining Fortune" -> return "§a+§6☘"
                            "Gain +15% more Powder while" -> return "§a15%"
                            "Reduce Pickaxe Ability cooldown" -> return "§a20%"
                            "10x chance to find Goblins" -> return "§a10x"
                            "Gain 5x Titanium drops" -> return "§a5x§9T"
                            else -> "§c!?"
                        }
                    }
                }
            }
        }

        if (stackSizeConfig.contains(InventoryConfig.StackSizeConfig.MenuConfig.Mining.HOTM_PERK_LEVELS) && (chestName == "Heart of the Mountain")) {
            val nameWithColor = item.name ?: return ""
            if ((nameWithColor.startsWith("§a")) || (nameWithColor.startsWith("§e")) || (nameWithColor.startsWith("§c"))) {
                //§7Level 64/§8100
                val lore = item.getLore()
                if ((lore.firstOrNull() == null) || (lore.lastOrNull() == null)) return ""
                if (!lore.first().contains("Level ") && !lore.last().contains("Right click to ")) return ""
                if (lore.last().contains("the Mountain!") || lore.last().contains("Requires ")) return ""
                xOutOfYNoColorRequiredPattern.matchMatcher(lore.first()) {
                    //§7Level 64/§8100
                    var colorCode = ""
                    var level = group("useful")
                    if (nameWithColor.startsWith("§a")) level = "✔"
                    if (lore.takeLast(3).any { it.removeColor().replace("Right click to ", "").contains("enable") }) colorCode = "§c"
                    return "$colorCode$level"
                }
            }
        }

        //the basis of all of this code was from technoblade's skycrypt profile so this might be WAY off, please have mercy
        //https://sky.shiiyu.moe/stats/Technoblade/Blueberry#Skills
        //ping @erymanthus on the skyhanni discord if you find any bugs with this
        if (stackSizeConfig.contains(InventoryConfig.StackSizeConfig.MenuConfig.Mining.HOTM_OVERALL_TIERS) && chestName == ("Heart of the Mountain") && item.cleanName().contains("Tier ")) {
            val nameWithColor = item.name ?: return ""
            if (nameWithColor.contains("§a")) return ""
            val lore = item.getLore()
            if (lore != null && lore.isNotEmpty()) {
                for (line in lore) {
                    if (line.startsWith("§7Progress: ") && line.endsWith("%")) {
                        return genericPercentPattern.matchMatcher(line) { group("percent").replace("100", "§a✔") } ?: ""
                    }
                }
            }
        }

        if (stackSizeConfig.contains(InventoryConfig.StackSizeConfig.MenuConfig.Mining.CRYSTAL_HOLLOWS_NUCLEUS) && (chestName == "Heart of the Mountain")) {
            val nameWithColor = item.name ?: return ""
            if (nameWithColor != "§5Crystal Hollows Crystals") return ""
            val lore = item.getLore()
            var crystalsNotPlaced = 0
            var crystalsNotFound = 0
            val totalCrystals = 5 //change "5" to whatever new value Hypixel does if this value ever changes
            for (line in lore) {
                if (line.contains("Your Other Crystals") || line.contains("Jasper") || line.contains("Ruby")) break //apparently jasper and ruby get counted without this hotfix line, soooo
                else if (line.contains(" §e✖ Not Placed")) crystalsNotPlaced++
                else if (line.contains(" §c✖ Not Found")) crystalsNotFound++
            }
            val crystalsPlaced = totalCrystals - crystalsNotPlaced - crystalsNotFound
            return "§a${crystalsPlaced}§r§e${crystalsNotPlaced}§r§c${crystalsNotFound}"
        }

        return ""
    }
}
