package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.jsonobjects.repo.GardenJson
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlinx.coroutines.launch
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object GardenCropMilestonesCommunityFix {

    /**
     * REGEX-TEST: §2§l§m       §f§l§m             §r §e676,985§6/§e2M
     */
    private val amountPattern by RepoPattern.pattern(
        "data.garden.milestonefix.amount",
        ".*§e(?<having>.*)§6/§e(?<max>.*)",
    )

    private var showWrongData = false
    private var showWhenAllCorrect = false

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<GardenJson>("Garden")
        val map = data.cropMilestoneCommunityHelp
        for ((key, value) in map) {
            if (key == "show_wrong_data") {
                showWrongData = value
            }
            if (key == "show_when_all_correct") {
                showWhenAllCorrect = value
            }
        }
    }

    fun openInventory(inventoryItems: Map<Int, ItemStack>) {
        if (!showWrongData) return
        if (!GardenAPI.config.copyMilestoneData) return
        fixForWrongData(inventoryItems)
    }

    private fun fixForWrongData(inventoryItems: Map<Int, ItemStack>) {
        val data = mutableListOf<String>()
        for ((_, stack) in inventoryItems) {
            val crop = GardenCropMilestones.getCropTypeByLore(stack) ?: continue
            checkForWrongData(stack, crop, data)
        }

        if (data.isNotEmpty()) {
            ChatUtils.chat(
                "Found §c${data.size} §ewrong crop milestone steps in the menu! " +
                    "Correct data got put into clipboard. " +
                    "Please share it on the §bSkyHanni Discord §ein the channel §b#share-data§e.",
            )
            OSUtils.copyToClipboard("```${data.joinToString("\n")}```")
        } else {
            if (showWhenAllCorrect) {
                ChatUtils.chat("No wrong crop milestone steps found!")
            }
        }
    }

    private fun checkForWrongData(
        stack: ItemStack,
        crop: CropType,
        wrongData: MutableList<String>,
    ) {
        val rawNumber = stack.name.removeColor().replace(crop.cropName, "").trim()
        val realTier = if (rawNumber == "") 0 else rawNumber.romanToDecimalIfNecessary()

        val lore = stack.getLore()
        val next = lore.nextAfter({ GardenCropMilestones.totalPattern.matches(it) }, 3) ?: return

        val guessNextMax = nextMax(realTier, crop)
        val nextMax = amountPattern.matchMatcher(next) {
            group("max").formatLong()
        } ?: return

        if (nextMax != guessNextMax) {
            wrongData.add("$crop:$realTier:${nextMax.addSeparators()}")
        }
    }

    /**
     * This helps to fix wrong crop milestone data
     * This command reads the clipboard content,
     * in the format of users sending crop milestone step data.
     *
     * The new data will be compared to the currently saved data,
     * differences are getting replaced, and the result gets put into the clipboard.
     * The clipboard context can be used to update the repo content.
     */
    fun readDataFromClipboard() {
        SkyHanniMod.coroutineScope.launch {
            OSUtils.readFromClipboard()?.let {
                handleInput(it)
            }
        }
    }

    private var totalFixedValues = 0

    private fun handleInput(input: String) {
        println(" ")
        var fixed = 0
        var alreadyCorrect = 0
        for (line in input.lines()) {
            val split = line.replace("```", "").replace(".", ",").split(":")
            if (split.size != 3) continue
            val (rawCrop, tier, amount) = split
            val crop = LorenzUtils.enumValueOf<CropType>(rawCrop)

            if (tryFix(crop, tier.toInt(), amount.formatInt())) {
                fixed++
            } else {
                alreadyCorrect++
            }
        }
        totalFixedValues += fixed
        ChatUtils.chat("Fixed: $fixed/$alreadyCorrect, total fixes: $totalFixedValues")
        val s = ConfigManager.gson.toJsonTree(GardenCropMilestones.cropMilestoneData).toString()
        OSUtils.copyToClipboard("\"crop_milestones\":$s,")
    }

    private fun tryFix(crop: CropType, tier: Int, amount: Int): Boolean {
        val guessNextMax = nextMax(tier, crop)
        if (guessNextMax.toInt() == amount) return false
        GardenCropMilestones.cropMilestoneData = GardenCropMilestones.cropMilestoneData.editCopy {
            fix(crop, this, tier, amount)
        }
        return true
    }

    private fun nextMax(tier: Int, crop: CropType): Long =
        GardenCropMilestones.getCropsForTier(tier + 1, crop) - GardenCropMilestones.getCropsForTier(tier, crop)

    private fun fix(crop: CropType, map: MutableMap<CropType, List<Int>>, tier: Int, amount: Int) {
        map[crop] = map[crop]!!.editCopy {
            this[tier] = amount
        }
    }
}
