package at.hannibal2.skyhanni.features.garden.composter

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.RenderInventoryItemTipEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ComposterInventoryNumbers {

    private val patternGroup = RepoPattern.group("garden.composter.inventory.numbers")
    private val valuePattern by patternGroup.pattern(
        "value",
        ".* §e(?<having>.*)§6/(?<total>.*)",
    )
    private val amountPattern by patternGroup.pattern(
        "amount",
        "§7§7Compost Available: §a(?<amount>.*)",
    )

    @SubscribeEvent
    fun onRenderItemTip(event: RenderInventoryItemTipEvent) {
        if (!GardenAPI.inGarden()) return
        if (!GardenAPI.config.composters.inventoryNumbers) return

        if (event.inventoryName != "Composter") return

        val stack = event.stack

        val slotNumber = event.slot.slotNumber

        // Composts Available
        if (slotNumber == 13) {
            amountPattern.firstMatcher(stack.getLore()) {
                val total = group("amount").formatInt()
                event.offsetY = -2
                event.offsetX = -20
                event.stackTip = "§6${total.addSeparators()}"
                return
            }
        }

        // Organic Matter or Fuel
        if (slotNumber == 46 || slotNumber == 52) {
            valuePattern.firstMatcher(stack.getLore()) {
                val having = group("having").removeColor().formatInt()
                val havingFormat = having.shortFormat()
                val total = group("total").removeColor()

                val color = if (slotNumber == 46) {
                    // Organic Matter
                    event.offsetY = -95
                    event.offsetX = 5
                    event.alignLeft = false
                    "§e"
                } else {
                    // Fuel
                    event.offsetY = -41
                    event.offsetX = -20
                    "§a"
                }

                event.stackTip = "$color$havingFormat/$total"
            }
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.composterInventoryNumbers", "garden.composters.inventoryNumbers")
    }
}
