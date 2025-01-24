package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.features.garden.farming.CropMoneyDisplay
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getSpeed
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatLongOrUserError
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils.format
import kotlin.time.Duration.Companion.seconds

object GardenCropTimeCommand {

    private val config get() = GardenApi.config.moneyPerHours

    fun onCommand(args: Array<String>) {
        if (!config.display) {
            ChatUtils.userError("shcroptime requires 'Show money per Hour' feature to be enabled to work!")
            return
        }

        if (args.size < 2) {
            ChatUtils.userError("Usage: /shcroptime <amount> <item>")
            return
        }

        val amount = args[0].formatLongOrUserError() ?: return
        val multipliers = CropMoneyDisplay.multipliers
        if (multipliers.isEmpty()) {
            ChatUtils.userError("Data not loaded yet. Join the garden and display the money per hour display.")
            return
        }

        val rawSearchName = args.toMutableList().drop(1).joinToString(" ")
        val searchName = rawSearchName.lowercase()

        val map = mutableMapOf<String, Long>()
        for (entry in multipliers) {
            val internalName = entry.key
            val itemName = internalName.itemName
            if (itemName.removeColor().lowercase().contains(searchName)) {
                val (baseId, baseAmount) = NeuItems.getPrimitiveMultiplier(internalName)
                val baseName = baseId.itemName
                val crop = CropType.getByName(baseName.removeColor())

                val fullAmount = baseAmount.toLong() * amount
                val text = if (baseAmount == 1) {
                    "§e${amount.addSeparators()}x $itemName"
                } else {
                    "§e${amount.addSeparators()}x $itemName §7(§e${fullAmount.addSeparators()}x $baseName§7)"
                }

                val speed = crop.getSpeed()
                if (speed == null) {
                    map["$text §cNo speed data!"] = -1
                } else {
                    val missingTime = (fullAmount / speed).seconds
                    val duration = missingTime.format()
                    map["$text §b$duration"] = missingTime.inWholeSeconds
                }
            }
        }

        if (map.isEmpty()) {
            ChatUtils.userError("No crop item found for '$rawSearchName'.")
            return
        }

        ChatUtils.chat("Crop Speed for ${map.size} items:\n" + map.sorted().keys.joinToString("\n"))
    }
}
