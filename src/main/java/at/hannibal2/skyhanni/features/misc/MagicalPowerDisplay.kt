package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.RenderItemTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object MagicalPowerDisplay {
    private val config get() = SkyHanniMod.feature.inventory.magicalPower
    private var contactAmount: Int?
        get() = ProfileStorageData.profileSpecific?.abiphoneContactAmount
        private set(value) {
            ProfileStorageData.profileSpecific?.abiphoneContactAmount = value
        }

    private val hegemonyArtifact = "HEGEMONY_ARTIFACT".toInternalName()
    private val riftPrism = "RIFT_PRISM".toInternalName()

    /**
     * REGEX-TEST: Accessory Bag
     * REGEX-TEST: Accessory Bag (1/75)
     * REGEX-TEST: Accessory Bag (909/394294)
     * REGEX-TEST: Auctions Browser
     * REGEX-TEST: Auctions: "ligma"
     * REGEX-TEST: Auctions: ""sugoma""
     * */
    private val acceptedInvPattern by RepoPattern.pattern(
        "inv.acceptable",
        "^(Accessory Bag(?: \\(\\d+\\/\\d+\\))?|Auctions Browser|Manage Auctions|Auctions: \".*\"?)$",
    )

    private val abiphoneGroup = RepoPattern.group("data.abiphone")

    /**
     * REGEX-TEST: Abiphone X Plus
     * REGEX-TEST: Abiphone X Plus Special Edition
     * REGEX-TEST: Abiphone XI Ultra Style
     * REGEX-TEST: Abiphone XII Mega Color
     * REGEX-TEST: Abiphone XIII Pro
     * REGEX-TEST: Abiphone XIV Enormous Purple
     * REGEX-TEST: Abiphone Flip
     * */
    private val abiphoneNamePattern by abiphoneGroup.pattern(
        "name",
        "Abiphone .*",
    )

    /**
     * REGEX-TEST: Your contacts: 0/0
     * REGEX-TEST: Your contacts: 1/75
     * REGEX-TEST: Your contacts: 52/60
     * */
    private val yourContactPattern by abiphoneGroup.pattern(
        "contacts",
        "Your contacts: (?<contacts>\\d+)\\/\\d+",
    )

    @SubscribeEvent
    fun onRenderItemTip(event: RenderItemTipEvent) {
        if (!isEnabled()) return
        if (!acceptedInvPattern.matches(InventoryUtils.openInventoryName().removeColor())) return

        val item = event.stack
        val rarity = item.getAccessoryRarityOrNull() ?: return
        val internalName = item.getInternalNameOrNull() ?: return

        var endMP = rarity.toMP() ?: ErrorManager.skyHanniError(
            "Unknown rarity '$rarity' for item '${item.displayName}§7'",
        )

        when (internalName) {
            hegemonyArtifact -> endMP *= 2
            riftPrism -> endMP = 11
            else -> if (internalName.isAbicase()) endMP += (contactAmount ?: 0) / 2
        }

        event.stackTip = "${if (config.colored) rarity.chatColorCode else "§7"}$endMP"
    }

    @SubscribeEvent
    fun onInventoryOpened(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (!abiphoneNamePattern.matches(event.inventoryName)) return

        val theBookLore = event.inventoryItems[51]?.getLore() ?: return
        for (line in theBookLore) {
            yourContactPattern.matchMatcher(line.removeColor()) {
                contactAmount = group("contacts").toInt()
                return
            }
        }
    }

    private fun NEUInternalName.isAbicase(): Boolean = this.startsWith("ABICASE_")

    private fun LorenzRarity.toMP(): Int? = when (this) {
        LorenzRarity.COMMON, LorenzRarity.SPECIAL -> 3
        LorenzRarity.UNCOMMON, LorenzRarity.VERY_SPECIAL -> 5
        LorenzRarity.RARE -> 8
        LorenzRarity.EPIC -> 12
        LorenzRarity.LEGENDARY -> 16
        LorenzRarity.MYTHIC -> 22
        else -> null
    }

    private fun ItemStack.getAccessoryRarityOrNull(): LorenzRarity? {
        val category = this.getItemCategoryOrNull() ?: return null
        if (category != ItemCategory.ACCESSORY && category != ItemCategory.HATCESSORY) return null
        return this.getItemRarityOrNull()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
