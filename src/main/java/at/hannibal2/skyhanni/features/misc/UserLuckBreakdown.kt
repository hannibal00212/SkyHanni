package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.render.gui.ReplaceItemEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrPut
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.player.inventory.ContainerLocalMenu
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object UserLuckBreakdown {
    private var inMiscStats = false
    private var replaceSlot: Int? = null
    private var itemCreateCoolDown = SimpleTimeMark.farPast()
    private var skillCalcCoolDown = SimpleTimeMark.farPast()

    private val storage get() = ProfileStorageData.playerSpecific
    private val config get() = SkyHanniMod.feature.misc

    private lateinit var mainLuckItem: ItemStack
    private val mainLuckID = "ENDER_PEARL".toInternalName()
    private const val MAIN_LUCK_NAME = "§a✴ SkyHanni User Luck"

    private lateinit var fillerItem: ItemStack
    private val fillerID = "STAINED_GLASS_PANE".toInternalName()
    private const val FILLER_NAME = " "

    private lateinit var limboItem: ItemStack
    private val limboID = "ENDER_PEARL".toInternalName()
    private const val LIMBO_NAME = "§a✴ Limbo Personal Best"

    private lateinit var skillsItem: ItemStack
    private val skillsID = "DIAMOND_SWORD".toInternalName()
    private const val SKILLS_NAME = "§a✴ Category: Skills"

    private var showAllStats = true

    /**
     * REGEX-TEST: §7Show all stats: §aYes
     * REGEX-TEST: §7Show all stats: §cNope
     */
    private val showAllStatsPattern by RepoPattern.pattern(
        "misc.statsbreakdown.showallstats",
        "§7Show all stats: §.(?<toggle>.*)",
    )

    private const val LUCK_TOOLTIP = "§5§o §a✴ SkyHanni User Luck §f"
    private var inCustomBreakdown = false

    private val validItemSlots = (10..53).filter { it !in listOf(17, 18, 26, 27, 35, 36) && it !in 44..53 }
    private val invalidItemSlots = (0..53).filter { it !in validItemSlots }

    private var skillOverflowLuck = mutableMapOf<SkillType, Int>()

    @SubscribeEvent
    fun replaceItem(event: ReplaceItemEvent) {
        if (!config.userluckEnabled) return
        if (event.inventory !is ContainerLocalMenu) return
        if (!inMiscStats) return

        if (event.slot == replaceSlot && !inCustomBreakdown) {
            val limboUserLuck = storage?.limbo?.userLuck ?: 0.0f
            if (limboUserLuck == 0.0f && !showAllStats) return
            if (itemCreateCoolDown.passedSince() > 3.seconds) {
                itemCreateCoolDown = SimpleTimeMark.now()
                createItems()
            }
            event.replace(mainLuckItem)
            return
        }
        if (inCustomBreakdown) {
            if (itemCreateCoolDown.passedSince() > 3.seconds) {
                itemCreateCoolDown = SimpleTimeMark.now()
                createItems()
            }
            checkItemSlot(event)
        }
    }

    private fun checkItemSlot(event: ReplaceItemEvent) {
        when (event.slot) {
            48, 49 -> return

            10 -> event.replace(skillsItem)
            11 -> event.replace(limboItem)

            in validItemSlots -> event.replace(null)

            in invalidItemSlots -> {
                if (event.originalItem.item == limboID.getItemStack().item) return
                event.replace(fillerItem)
                return
            }
        }
    }

    @SubscribeEvent
    fun openInventory(event: InventoryOpenEvent) {
        if (event.inventoryName != "Your Stats Breakdown") {
            inMiscStats = false
            return
        }
        val inventoryName = event.inventoryItems[4]?.name.orEmpty()
        if (inventoryName != "§dMisc Stats") return
        inMiscStats = true
        replaceSlot = findValidSlot(event.inventoryItems)
        val showAllStatsLore = event.inventoryItems[50]?.getLore() ?: listOf("")
        for (line in showAllStatsLore) {
            showAllStatsPattern.matchMatcher(line) {
                showAllStats = when (group("toggle")) {
                    "Yes" -> true
                    else -> false
                }
            }
        }
        return
    }

    @SubscribeEvent
    fun closeInventory(event: InventoryCloseEvent) {
        inMiscStats = false
        inCustomBreakdown = false
    }

    private fun findValidSlot(input: Map<Int, ItemStack>): Int? {
        for (slot in input.keys) {
            if (slot !in validItemSlots && slot < 44) continue
            val itemStack = input[slot]
            if (itemStack?.name == " ") {
                return slot
            }
        }
        return null
    }

    @SubscribeEvent
    fun onHoverItem(event: LorenzToolTipEvent) {
        if (!config.userluckEnabled) return
        if (!LorenzUtils.inSkyBlock) return
        if (skillCalcCoolDown.passedSince() > 3.seconds) {
            skillCalcCoolDown = SimpleTimeMark.now()
            calcSkillLuck()
        }
        val limboLuck = storage?.limbo?.userLuck?.roundTo(1) ?: 0.0f
        when (event.slot.inventory.name) {
            "Your Equipment and Stats" -> equipmentMenuTooltip(event, limboLuck)
            "Your Stats Breakdown" -> statsBreakdownLoreTooltip(event, limboLuck)
            "SkyBlock Menu" -> skyblockMenuTooltip(event, limboLuck)
        }
    }

    private fun equipmentMenuTooltip(event: LorenzToolTipEvent, limboLuck: Float) {
        if (event.slot.slotIndex != 25) return
        if (limboLuck == 0.0f && !showAllStats) return

        val skillLuck = skillOverflowLuck.values.sum()
        val totalLuck = skillLuck + limboLuck
        val lastIndex = event.toolTip.indexOfLast { it == "§5§o" }
        if (lastIndex == -1) return

        val luckString = tryTruncateFloat(totalLuck)
        event.toolTip.add(lastIndex, "$LUCK_TOOLTIP$luckString")
    }

    private fun statsBreakdownLoreTooltip(event: LorenzToolTipEvent, limboLuck: Float) {
        if (!inMiscStats) return
        if (inCustomBreakdown && event.slot.slotIndex == 48) {
            event.toolTip[1] = "§7To Your Stats Breakdown"
        }
        if (event.slot.slotIndex != 4) return
        if (limboLuck == 0.0f && !showAllStats) return

        val skillLuck = skillOverflowLuck.values.sum()
        val totalLuck = skillLuck + limboLuck
        val luckString = tryTruncateFloat(totalLuck)
        event.toolTip.add("§5§o §a✴ SkyHanni User Luck §f$luckString")
    }

    private fun skyblockMenuTooltip(event: LorenzToolTipEvent, limboLuck: Float) {
        if (event.slot.slotIndex != 13) return
        val lastIndex = event.toolTip.indexOfLast { it == "§5§o" }
        if (lastIndex == -1) return

        val skillLuck = skillOverflowLuck.values.sum()
        val totalLuck = skillLuck + limboLuck
        if (totalLuck == 0f) return

        val luckString = tryTruncateFloat(totalLuck)
        event.toolTip.add(lastIndex, "$LUCK_TOOLTIP$luckString")
    }

    private fun tryTruncateFloat(input: Float): String {
        val string = input.addSeparators()
        return if (string.endsWith(".0")) return string.dropLast(2)
        else string
    }

    @SubscribeEvent
    fun onStackClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!config.userluckEnabled) return
        if (!inMiscStats) return
        val limboUserLuck = storage?.limbo?.userLuck ?: 0.0f
        if (limboUserLuck == 0.0f && !showAllStats) return

        if (inCustomBreakdown && event.slotId != 49) event.cancel()
        when (event.slotId) {
            replaceSlot -> {
                if (inCustomBreakdown) return
                event.cancel()
                inCustomBreakdown = true
            }
            48 -> {
                if (!inCustomBreakdown) return
                inCustomBreakdown = false
            }
            else -> return
        }
    }

    private fun createItems() {
        fillerItem = ItemUtils.createItemStack(
            fillerID.getItemStack().item,
            FILLER_NAME,
            listOf(),
            1,
            15,
        )

        val limboLuck = storage?.limbo?.userLuck ?: 0.0f
        val skillLuck = skillOverflowLuck.values.sum()
        val totalLuck = skillLuck + limboLuck

        mainLuckItem = ItemUtils.createItemStack(
            mainLuckID.getItemStack().item,
            "$MAIN_LUCK_NAME §f${tryTruncateFloat(totalLuck)}",
            createItemLore("mainMenu", totalLuck),
        )
        limboItem = ItemUtils.createItemStack(
            limboID.getItemStack().item,
            LIMBO_NAME,
            createItemLore("limbo", limboLuck),
        )
        skillsItem = ItemUtils.createItemStack(
            skillsID.getItemStack().item,
            SKILLS_NAME,
            createItemLore("skills"),
        )
    }

    private fun createItemLore(type: String, luckInput: Float = 0.0f): Array<String> {
        calcSkillLuck()
        return when (type) {
            "mainMenu" -> {
                val luckString = tryTruncateFloat(luckInput.roundTo(2))
                if (luckInput == 0.0f) {
                    arrayOf(
                        "§7SkyHanni User Luck is the best stat.",
                        "",
                        "§7Flat: §a+$luckString✴",
                        "",
                        "§8You have none of this stat!",
                        "§eClick to view!",
                    )
                } else {
                    arrayOf(
                        "§7SkyHanni User Luck increases your",
                        "§7overall fortune around Hypixel SkyBlock.",
                        "",
                        "§7(Disclaimer: May not affect real drop chances)",
                        "",
                        "§eClick to view!",
                    )
                }
            }

            "limbo" -> {
                val luckString = tryTruncateFloat(luckInput.roundTo(2))
                arrayOf(
                    "§8Action",
                    "",
                    "§7Value: §a+$luckString✴",
                    "",
                    "§8Gain more by going to Limbo,",
                    "§8and obtaining a higher Personal Best§8.",
                )
            }

            "skills" -> {
                val luckString = skillOverflowLuck.values.sum()
                val firstHalf = arrayOf(
                    "§8Grouped",
                    "",
                    "§7Value: §a+$luckString✴",
                    "",
                )
                val secondHalf = arrayOf(
                    "§8Stats from your overflow skills.",
                    "§8Obtain more each 5 overflow levels!",
                )
                val sourcesList = mutableListOf<String>()
                for ((skillType, luck) in skillOverflowLuck) {
                    if (luck == 0) continue
                    sourcesList.add(" §a+$luck✴ §f${skillType.displayName} Skill")
                }
                val finalList = mutableListOf<String>()
                finalList.addAll(firstHalf)
                if (sourcesList.isNotEmpty()) {
                    finalList.addAll(sourcesList)
                    finalList.add("")
                }
                finalList.addAll(secondHalf)
                finalList.toTypedArray()
            }

            else -> arrayOf("")
        }
    }

    private fun calcSkillLuck() {
        val storage = ProfileStorageData.profileSpecific?.skillData ?: return
        skillOverflowLuck.clear()
        for ((skillType, skillInfo) in storage) {
            val level = skillInfo.level
            val overflow = skillInfo.overflowLevel
            val luck = ((overflow - level) / 5) * 50
            skillOverflowLuck.addOrPut(skillType, luck)
        }
    }
}
