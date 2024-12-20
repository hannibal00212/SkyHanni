package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.data.GardenCropMilestones
import at.hannibal2.skyhanni.data.GardenCropMilestones.getCounter
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.features.garden.farming.GardenCropSpeed.getSpeed
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatIntOrUserError
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.command.CommandBase
import kotlin.time.Duration.Companion.seconds

object FarmingMilestoneCommand {

    fun onCommand(crop: String?, current: String?, target: String?, needsTime: Boolean) {
        if (crop == null) {
            ChatUtils.userError("No crop type entered")
            return
        }

        val enteredCrop = CropType.getByNameOrNull(crop) ?: run {
            ChatUtils.userError("Invalid crop type entered")
            return
        }

        val currentMilestone = current?.toIntOrNull()
        val targetMilestone = target?.toIntOrNull()

        if (currentMilestone == null) {
            val currentProgress = enteredCrop.getCounter()
            val currentCropMilestone =
                GardenCropMilestones.getTierForCropCount(currentProgress, enteredCrop, allowOverflow = true) + 1
            val cropsForTier =
                GardenCropMilestones.getCropsForTier(currentCropMilestone, enteredCrop, allowOverflow = true)
            val output = (cropsForTier - currentProgress).formatOutput(needsTime, enteredCrop)

            ChatUtils.chat("§7$output needed to reach the next milestone")
            return
        }

        if (targetMilestone == null) {
            val cropsForTier = GardenCropMilestones.getCropsForTier(currentMilestone, enteredCrop, allowOverflow = true)
            val output = cropsForTier.formatOutput(needsTime, enteredCrop)

            ChatUtils.chat("§7$output needed for milestone §7$currentMilestone")
            return
        }

        if (currentMilestone >= targetMilestone) {
            ChatUtils.userError("Entered milestone is greater than or the same as target milestone")
            return
        }

        val currentAmount = GardenCropMilestones.getCropsForTier(currentMilestone, enteredCrop, allowOverflow = true)
        val targetAmount = GardenCropMilestones.getCropsForTier(targetMilestone, enteredCrop, allowOverflow = true)
        val output = (targetAmount - currentAmount).formatOutput(needsTime, enteredCrop)
        ChatUtils.chat("§7$output needed for milestone §7$currentMilestone §a-> §7$targetMilestone")
    }

    fun setGoal(args: Array<String>) {
        val storage = ProfileStorageData.profileSpecific?.garden?.customGoalMilestone ?: return

        if (args.size != 2) {
            ChatUtils.userError("Usage: /shcropgoal <crop name> <target milestone>")
            return
        }

        val enteredCrop = CropType.getByNameOrNull(args[0]) ?: run {
            ChatUtils.userError("Not a crop type: '${args[0]}'")
            return
        }
        val targetLevel = args[1].formatIntOrUserError() ?: return

        val counter = enteredCrop.getCounter()
        val level = GardenCropMilestones.getTierForCropCount(counter, enteredCrop)
        if (targetLevel <= level && targetLevel != 0) {
            ChatUtils.userError("Custom goal milestone ($targetLevel) must be greater than your current milestone ($level).")
            return
        }
        storage[enteredCrop] = targetLevel
        ChatUtils.chat("Custom goal milestone for §b${enteredCrop.cropName} §eset to §b$targetLevel.")
    }

    fun onComplete(strings: Array<String>): List<String> {
        return if (strings.size <= 1) {
            CommandBase.getListOfStringsMatchingLastWord(
                strings,
                CropType.entries.map { it.simpleName }
            )
        } else listOf()
    }

    private fun Long.formatOutput(needsTime: Boolean, crop: CropType): String {
        if (!needsTime) return "${this.addSeparators()} §a${crop.cropName}"
        val speed = crop.getSpeed() ?: -1
        val missingTime = (this / speed).seconds
        return "${missingTime.format()}§a"
    }
}
