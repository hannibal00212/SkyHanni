package at.hannibal2.skyhanni.features.inventory.itemdisplayoverlay.menu

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.inventory.stacksize.StackSizeMenuConfig
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class MenuItemDisplayOverlayAbiphone {
    private val xOutOfYNoColorRequiredPattern = ".*: (§.)?(?<useful>[0-9]+)(§.)?\\/(§.)?(?<total>[0-9]+).*".toPattern()

    @SubscribeEvent
    fun onRenderItemTip(event: RenderItemTipEvent) {
        event.stackTip = getStackTip(event.stack)
    }

    private fun getStackTip(item: ItemStack): String {
        if (SkyHanniMod.feature.inventory.stackSize.menu.abiphone.isEmpty()) return ""
        val itemName = item.cleanName()
        val stackSizeConfig = SkyHanniMod.feature.inventory.stackSize.menu.abiphone
        val chestName = InventoryUtils.openInventoryName()
        val isAbiphone = ((chestName.contains("Abiphone") || chestName.contains("AⒷiphone")))
        
        if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.CONTACTS_DIRECTORY)) && isAbiphone && (itemName == ("Contacts Directory"))) {
            for (line in item.getLore()) {
                if (line.contains("Your contacts: ")) {
                    return xOutOfYNoColorRequiredPattern.matchMatcher(line) { group("useful") } ?: ""
                }
            }
        }

        if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.DO_NOT_DISTURB)) && isAbiphone) {
            val nameWithColor = item.name ?: return ""
            if ((nameWithColor.startsWith("§f§"))) {
                val lore = item.getLore()
                for (line in lore) {
                    if (line.equals("§cDo Not Disturb enabled!")) {
                        return "§c§l✖"
                    }
                }
            }
        }

        if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.RELAYS_COMPLETED)) && isAbiphone && (itemName.contains(" Operator Chip"))) {
            val maxRelays = "9" //edit this line whenever they add more relays
            //§7Upgraded Relays: §e1§7/§59
            //Upgraded Relays: 1/9
            for (line in item.getLore()) {
                if (line.contains("Upgraded Relays: ")) {
                    if (line.contains("ALL!")) return maxRelays
                    else return xOutOfYNoColorRequiredPattern.matchMatcher(line) { group("useful") } ?: ""
                }
            }
        }

        if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.SELECTED_RINGTONE)) && isAbiphone && (itemName.contains("Ringtones"))) {
            for (line in item.getLore()) {
                if (line.contains("Selected Ringtone: ")) {
                    val ringtone = item.getLore().first().removeColor().split(" ").last()
                    return when (ringtone) {
                        "Default" -> "Def"
                        "Entertainer" -> "Ent"
                        "Notkia" -> "Nka"
                        "Techy" -> "Tec"
                        "Scrapper" -> "Scr"
                        "Elise" -> "WTF" //intentional. do not edit.
                        "Bells" -> "Jbl"
                        "Vibrate" -> "Vib"
                        else -> "?"
                    }
                }
            }
        }

        if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.TIC_TAC_TOE)) && isAbiphone && (itemName == ("Tic Tac Toe"))) {
            var finalString = ""
            for (line in item.getLore()) {
                if (line.contains("Wins: ") || line.contains("Draws: ") || line.contains("Losses: ")) {
                    finalString += line.split(" ").last()
                }
            }
            return finalString
        }

        if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.SNAKE)) && isAbiphone && (itemName == ("Snake"))) {
            for (line in item.getLore()) {
                if (line.contains(" Score: ")) {
                    return line.removeColor().split(" ").last()
                }
            }
        }

        if ((stackSizeConfig.contains(StackSizeMenuConfig.Abiphone.NAVIGATION)) && (isAbiphone || chestName.contains("Contacts Directory")) && ((itemName == ("Filter")) || itemName == ("Sort"))) {
            for (line in item.getLore()) {
                if (line.contains("▶ ")) {
                    val placeholder = line.removeColor().replace("▶ ","").replace(" ","").lowercase() //lowercase() because i dont trust hypixel admins
                    return when (placeholder) {
                        "alphabetical" -> "ABC"
                        "donotdisturbfirst" -> "§cDND"
                        "difficulty" -> "§aE§eM§cH"
                        "usuallocation" -> "Loc"
                        "notadded" -> "§cQA"
                        "completedquestbutnotadded" -> "§aQ§cA"
                        else -> placeholder.take(3).firstLetterUppercase()
                    }
                }
            }
        }

        return ""
    }
}
