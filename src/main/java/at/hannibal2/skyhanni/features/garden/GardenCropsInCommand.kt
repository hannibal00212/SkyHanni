package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.features.garden.farming.CropMoneyDisplay
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getSpeed
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeUtils

object GardenCropsInCommand {

    private val config get() = GardenApi.config.moneyPerHours

    fun onCommand(args: Array<String>) {
        if (!config.display) {
            ChatUtils.userError("shcropsin requires 'Show money per Hour' feature to be enabled to work!")
            return
        }

        if (args.size < 2) {
            ChatUtils.userError("Usage: /shcropsin <time> <item>")
            return
        }

        val rawTime = args[0]
        val seconds = try {
            TimeUtils.getDuration(rawTime).inWholeSeconds
        } catch (e: NumberFormatException) {
            ChatUtils.userError("Not a valid time: '$rawTime'")
            return
        }
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

                val speed = crop.getSpeed()

                if (speed == null) {
                    map["$itemName §cNo speed data!"] = -1
                } else {
                    val fullAmount = seconds * speed / baseAmount
                    map["$itemName §b${fullAmount.addSeparators()}x"] = fullAmount
                }
            }
        }

        if (map.isEmpty()) {
            ChatUtils.userError("No crops found for '$rawSearchName'")
            return
        }

        ChatUtils.chat("Crops farmed in $rawTime:\n" + map.sorted().keys.joinToString("\n"))
    }
}
