package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.BeltsJson
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DojoRankDisplay {

    private val config get() = SkyHanniMod.feature.crimsonIsle
    private var display = emptyList<String>()
    private val patternGroup = RepoPattern.group("inventory.dojo.rankdisplay")
    private val testNamePattern by patternGroup.pattern(
        "name",
        "(?<color>§\\w)Test of (?<name>.*)",
    )
    private val testRankPattern by patternGroup.pattern(
        "rank",
        "(?:§\\w)+Your Rank: (?<rank>§\\w.) §8\\((?<score>\\d+)\\)",
    )
    private var belts = mapOf<String, Int>()

    @SubscribeEvent
    fun onBackgroundDraw(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.dojoRankDisplayPosition.renderStrings(display, posLabel = "Dojo Rank Display")
    }

    private fun drawDisplay(items: Collection<ItemStack>) = buildList {
        if (belts.isEmpty()) {
            // TODO make clickable
            add("§cUnable to get Belts data, please run /shupdaterepo")
            return@buildList
        }

        var totalScore = 0
        for (stack in items) {
            val name = stack.displayName ?: continue
            testNamePattern.matchMatcher(name) {
                val testColor = group("color")
                val testName = group("name")
                testRankPattern.firstMatcher(stack.getLore()) {
                    val rank = group("rank")
                    val score = group("score").toInt()
                    val color = if (score in 0..99) "§c" else "§a"
                    totalScore += score
                    add("$testColor$testName§f: $rank §7($color${score.addSeparators()}§7)")
                }
            }
        }

        val beltPoints = belts.toList()

        val currentBelt = beltPoints.lastOrNull { totalScore >= it.second } ?: beltPoints.first()
        val currentIndex = beltPoints.indexOf(currentBelt)
        val nextBelt = beltPoints.getOrNull(currentIndex + 1) ?: beltPoints.first()
        val pointsNeededForNextBelt = 0.coerceAtLeast(nextBelt.second.minus(totalScore))

        add("§7Total Score: §6${totalScore.addSeparators()} §7(§8${currentBelt.first}§7)")

        if (pointsNeededForNextBelt != 0)
            add("§7Points needed for ${nextBelt.first}§f: §6${pointsNeededForNextBelt.addSeparators()}")
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return
        if (event.inventoryName != "Challenges") return
        display = drawDisplay(event.inventoryItems.values)
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        display = emptyList()
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<BeltsJson>("Belts")
        belts = data.belts
    }

    private fun isEnabled() =
        IslandType.CRIMSON_ISLE.isInIsland() && LorenzUtils.skyBlockArea == "Dojo" && config.showDojoRankDisplay
}
