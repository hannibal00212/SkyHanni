package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object GardenCropUpgrades {

    private val patternGroup = RepoPattern.group("garden.cropupgrades")

    /**
     * REGEX-TEST: §7Current Tier: §e7§7/§a9
     */
    private val tierPattern by patternGroup.pattern(
        "tier",
        "§7Current Tier: §.(?<level>\\d)§7/§a9",
    )

    /**
     * REGEX-TEST:   §r§6§lCROP UPGRADE §eNether Wart§7 #7
     */
    private val chatUpgradePattern by patternGroup.pattern(
        "chatupgrade",
        "\\s+§r§6§lCROP UPGRADE §e(?<crop>[\\w ]+)§7 #(?<tier>\\d)",
    )

    private val cropUpgrades: MutableMap<CropType, Int>? get() = GardenApi.storage?.cropUpgrades

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!GardenApi.inGarden()) return

        chatUpgradePattern.matchMatcher(event.message) {
            val crop = CropType.getByNameOrNull(group("crop"))
            val level = group("tier").formatInt()
            crop?.setUpgradeLevel(level)
        }
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!GardenApi.inGarden()) return
        if (event.inventoryName != "Crop Upgrades") return

        for (item in event.inventoryItems.values) {
            val crop = CropType.getByNameOrNull(item.name.removeColor()) ?: continue
            tierPattern.firstMatcher(item.getLore()) {
                val level = group("level").formatInt()
                crop.setUpgradeLevel(level)
            }
        }
    }

    fun CropType.getUpgradeLevel() = cropUpgrades?.get(this)

    private fun CropType.setUpgradeLevel(level: Int) {
        cropUpgrades?.put(this, level)
    }
}
