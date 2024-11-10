package at.hannibal2.skyhanni.api.enoughupdates

import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.extraAttributes
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.StringUtils.cleanString
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import java.util.regex.Matcher

class ItemResolutionQuery {

    private var compound: NBTTagCompound? = null
    private var itemType: Item? = null
    private var knownInternalName: String? = null
    private var guiContext: Gui? = null

    companion object {
        val enchantedBookNamePattern = "^((?:§.)*)([^§]+) ([IVXL]+)$".toPattern()
        private val petPattern = ".*(\\[Lvl .*] )§(.).*".toPattern()
        val petRarities = listOf("COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC")

        private val BAZAAR_ENCHANTMENT_PATTERN = "ENCHANTMENT_(\\D*)_(\\d+)".toPattern()

        fun transformHypixelBazaarToNEUItemId(hypixelId: String): String {
            val matcher = BAZAAR_ENCHANTMENT_PATTERN.matcher(hypixelId)
            if (matcher.matches()) {
                return matcher.group(1) + ";" + matcher.group(2)
            }
            return hypixelId.replace(":", "-")
        }

        fun findInternalNameByDisplayName(displayName: String, mayBeMangled: Boolean): String? {
            return filterInternalNameCandidates(
                findInternalNameCandidatesForDisplayName(displayName),
                displayName,
                mayBeMangled,
            )
        }

        private fun filterInternalNameCandidates(
            candidateInternalNames: Collection<String>,
            displayName: String,
            mayBeMangled: Boolean,
        ): String? {
            var itemName = displayName
            val isPet = itemName.contains("[Lvl ")
            var petRarity: String? = null
            if (isPet) {
                val matcher: Matcher = petPattern.matcher(itemName)
                if (matcher.matches()) {
                    itemName = itemName.replace(matcher.group(1), "")
                    petRarity = matcher.group(2)
                }
            }
            val cleanDisplayName = itemName.removeColor()
            var bestMatch: String? = null
            var bestMatchLength = -1
            for (internalName in candidateInternalNames) {
                val unCleanItemDisplayName: String = EnoughUpdatesManager.getDisplayName(internalName)
                var cleanItemDisplayName = unCleanItemDisplayName.removeColor()
                if (cleanItemDisplayName.isEmpty()) continue
                if (isPet) {
                    if (!cleanItemDisplayName.contains("[Lvl {LVL}] ")) continue
                    cleanItemDisplayName = cleanItemDisplayName.replace("[Lvl {LVL}] ", "")
                    val matcher: Matcher = petPattern.matcher(unCleanItemDisplayName)
                    if (matcher.matches()) {
                        if (matcher.group(2) != petRarity) {
                            continue
                        }
                    }
                }

                val isMangledMatch = mayBeMangled && !cleanDisplayName.contains(cleanItemDisplayName)
                val isExactMatch = !mayBeMangled && cleanItemDisplayName == cleanDisplayName

                if (isMangledMatch || isExactMatch) {
                    continue
                }
                if (cleanItemDisplayName.length > bestMatchLength) {
                    bestMatchLength = cleanItemDisplayName.length
                    bestMatch = internalName
                }
            }
            return bestMatch
        }

        private fun findInternalNameCandidatesForDisplayName(displayName: String?): Set<String> {
            if (displayName == null) return emptySet()
            val isPet = displayName.contains("[Lvl ")
            val cleanDisplayName = displayName.cleanString()
            val titleWordMap = EnoughUpdatesManager.titleWordMap
            val candidates = HashSet<String>()
            for (partialDisplayName in cleanDisplayName.split(" ")) {
                if (partialDisplayName.isEmpty()) continue
                if (!titleWordMap.containsKey(partialDisplayName)) continue
                val c: Set<String> = titleWordMap[partialDisplayName]?.keys ?: continue
                for (s in c) {
                    if (isPet && !s.contains(";")) continue
                    candidates.add(s)
                }
            }
            return candidates
        }
    }

    fun withItemNbt(compound: NBTTagCompound): ItemResolutionQuery {
        this.compound = compound
        return this
    }

    fun withItemStack(stack: ItemStack): ItemResolutionQuery {
        this.itemType = stack.item
        this.compound = stack.tagCompound
        return this
    }

    fun withKnownInternalName(internalName: String): ItemResolutionQuery {
        this.knownInternalName = internalName
        return this
    }

    fun withGuiContext(gui: Gui): ItemResolutionQuery {
        this.guiContext = gui
        return this
    }

    fun withCurrentGuiContext(): ItemResolutionQuery {
        this.guiContext = Minecraft.getMinecraft().currentScreen
        return this
    }

    fun resolveInternalName(): String? {
        knownInternalName?.let {
            return it
        }
        var resolvedName = resolveFromSkyblock()
        resolvedName = if (resolvedName == null) {
            resolveContextualName()
        } else {
            when (resolvedName.intern()) {
                "PET" -> resolvePetName()
                "RUNE", "UNIQUE_RUNE" -> resolveRuneName()
                "ENCHANTED_BOOK" -> resolveEnchantedBookNameFromNBT()
                "PARTY_HAT_CRAB", "PARTY_HAT_CRAB_ANIMATED" -> resolveCrabHatName()
                "ABICASE" -> resolvePhoneCase()
                "PARTY_HAT_SLOTH" -> resolveSlothHatName()
                "POTION" -> resolvePotionName()
                "BALLOON_HAT_2024" -> resolveBalloonHatName()
                else -> resolvedName
            }
        }

        return resolvedName
    }

    private fun resolvePetName(): String? {
        val petInfo = getExtraAttributes().getString("petInfo")
        if (petInfo.isNullOrEmpty()) return null
        try {
            val petInfoObject = ConfigManager.gson.fromJson(petInfo, JsonObject::class.java)
            val petId = petInfoObject["type"].asString
            val petTier = petInfoObject["tier"].asString
            val rarityIndex = petRarities.indexOf(petTier)
            return petId.uppercase() + ";" + rarityIndex
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Error while resolving pet information",
                "petInfo" to petInfo,
            )
            return null
        }
    }

    private fun resolveRuneName(): String? {
        val runes = getExtraAttributes().getCompoundTag("runes")
        val runeName = CollectionUtils.getOnlyElement(runes.keySet, null)
        if (runeName.isNullOrEmpty()) return null
        return runeName.uppercase() + "_RUNE;" + runes.getInteger(runeName)
    }

    private fun resolveEnchantedBookNameFromNBT(): String? {
        val enchantments = getExtraAttributes().getCompoundTag("enchantments")
        val enchantName = CollectionUtils.getOnlyElement(enchantments.keySet, null)
        if (enchantName.isNullOrEmpty()) return null
        return enchantName.uppercase() + ";" + enchantments.getInteger(enchantName)
    }

    private fun resolveCrabHatName(): String {
        val crabHatYear = getExtraAttributes().getInteger("party_hat_year")
        val color = getExtraAttributes().getString("party_hat_color")
        return "PARTY_HAT_CRAB_" + color.uppercase() + (if (crabHatYear == 2022) "_ANIMATED" else "")
    }

    private fun resolvePhoneCase(): String {
        val model = getExtraAttributes().getString("model")
        return "ABICASE_" + model.uppercase()
    }

    private fun resolveSlothHatName(): String {
        val emoji = getExtraAttributes().getString("party_hat_emoji")
        return "PARTY_HAT_SLOTH_" + emoji.uppercase()
    }

    private fun resolvePotionName(): String {
        val potion = getExtraAttributes().getString("potion")
        val potionLvl = getExtraAttributes().getInteger("potion_level")
        val potionName = getExtraAttributes().getString("potion_name").replace(" ", "_")
        val potionType = getExtraAttributes().getString("potion_type")
        return if (potionName.isNotEmpty()) {
            "POTION_" + potionName.uppercase() + ";" + potionLvl
        } else if (!potion.isNullOrEmpty()) {
            "POTION_" + potion.uppercase() + ";" + potionLvl
        } else if (!potionType.isNullOrEmpty()) {
            "POTION_" + potionType.uppercase()
        } else {
            "WATER_BOTTLE"
        }
    }

    private fun resolveBalloonHatName(): String {
        val color = getExtraAttributes().getString("party_hat_color")
        return "BALLOON_HAT_2024_" + color.uppercase()
    }

    private fun resolveEnchantmentByName(displayName: String): String? {
        val matcher = enchantedBookNamePattern.matcher(displayName)
        if (!matcher.matches()) return null
        val format = matcher.group(1).lowercase()
        val enchantmentName = matcher.group(2).trim { it <= ' ' }
        val romanLevel = matcher.group(3)
        val ultimate = (format.contains("§l"))

        val prefix = if (ultimate && enchantmentName != "Ultimate Wise" && enchantmentName != "Ultimate Jerry") "ULTIMATE_" else ""
        val cleanedEnchantName = turboCheck(enchantmentName).replace(" ", "_").replace("-", "_").uppercase()
        return "$prefix$cleanedEnchantName;${romanLevel.romanToDecimal()}".uppercase()
    }

    private fun turboCheck(text: String): String {
        if (text == "Turbo-Cocoa") return "Turbo-Coco"
        if (text == "Turbo-Cacti") return "Turbo-Cactus"

        return text
    }

    private fun resolveItemInCatacombsRngMeter(): String? {
        val lore = compound.getLore()
        if (lore.size > 16) {
            val s = lore[15]
            if (s == "§7Selected Drop") {
                val displayName = lore[16]
                return findInternalNameByDisplayName(displayName, false)
            }
        }

        return null
    }

    private fun resolveContextualName(): String? {
        val chest = guiContext as? GuiChest ?: return null
        val inventorySlots = chest.inventorySlots as ContainerChest
        val guiName = inventorySlots.lowerChestInventory.displayName.unformattedText
        val isOnBazaar: Boolean = isBazaar(inventorySlots.lowerChestInventory)
        val displayName: String = ItemUtils.getDisplayName(compound) ?: return null
        if (itemType === Items.enchanted_book && isOnBazaar && compound != null) {
            return resolveEnchantmentByName(displayName)
        }
        if (displayName.endsWith("Enchanted Book") && guiName.startsWith("Superpairs")) {
            for (loreLine in compound.getLore()) {
                val enchantmentIdCandidate = resolveEnchantmentByName(loreLine)
                if (enchantmentIdCandidate != null) return enchantmentIdCandidate
            }
            return null
        }
        if (guiName == "Catacombs RNG Meter") {
            return resolveItemInCatacombsRngMeter()
        }
        if (guiName.startsWith("Choose Pet")) {
            return findInternalNameByDisplayName(displayName, false)
        }
        return null
    }

    private fun isBazaar(chest: IInventory): Boolean {
        if (chest.displayName.formattedText.startsWith("Bazaar ➜ ")) {
            return true
        }
        val bazaarSlot = chest.sizeInventory - 5
        if (bazaarSlot < 0) return false
        val stackInSlot = chest.getStackInSlot(bazaarSlot) ?: return false
        if (stackInSlot.stackSize == 0) return false

        val lore: List<String> = stackInSlot.getLore()
        return lore.contains("§7To Bazaar")
    }

    private fun getExtraAttributes(): NBTTagCompound {
        compound?.let {
            return it.extraAttributes
        } ?: return NBTTagCompound()
    }

    private fun resolveFromSkyblock(): String? {
        val internalName = getExtraAttributes().getString("id")
        if (internalName.isNullOrEmpty()) return null
        return internalName.uppercase().replace(":", "-")
    }

    fun resolveToItemStack(): ItemStack? {
        val internalName = knownInternalName ?: return null
        return EnoughUpdatesManager.jsonToStack(EnoughUpdatesManager.getItemById(internalName))
    }
}
