package at.hannibal2.skyhanni.features.skillprogress

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.SkillAPI
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundToPrecision
import at.hannibal2.skyhanni.utils.NumberUtil.toRoman
import at.hannibal2.skyhanni.utils.StringUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class SkillOverflowTooltip {

    private val config get() = SkyHanniMod.feature.skillProgress.overflowConfig

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!isEnabled()) return
        val inventoryName = InventoryUtils.openInventoryName()
        val stack = event.itemStack
        if (inventoryName == "Your Skills" && stack.getLore().any { it.contains("Click to view!") }) {
            val iterator = event.toolTip.listIterator()
            val split = stack.cleanName().split(" ")
            val skillName = split.first()
            val skill = SkillType.getByNameFirstUppercase(skillName) ?: return
            val useRoman = split.last().toIntOrNull() == null
            val skillInfo = SkillAPI.skillData?.get(skill) ?: return
            var next = false
            for (line in iterator) {
                val maxReached = "§7§8Max Skill level reached!"
                if (line.contains(maxReached)) {
                    val progress = (skillInfo.overflowCurrentXp.toDouble() / skillInfo.overflowCurrentXpMax) * 100
                    val percent = "§e${progress.roundToPrecision(1)}%"
                    val currentLevel = skillInfo.overflowLevel

                    val level = if (useRoman) currentLevel.toRoman() else currentLevel
                    val nextLevel = if (useRoman) (currentLevel + 1).toRoman() else currentLevel + 1
                    iterator.set("§7Progress to Level $nextLevel: $percent")

                    event.itemStack.name = "§a${skill.displayName} $level"
                    next = true
                    continue
                }
                if (next) {
                    val bar = "                    "
                    if (line.contains(bar)) {
                        val progress = (skillInfo.overflowCurrentXp.toDouble() / skillInfo.overflowCurrentXpMax)
                        val progressBar = StringUtils.progressBar(progress)
                        iterator.set("$progressBar §e${skillInfo.overflowCurrentXp.addSeparators()}§6/§e${skillInfo.overflowCurrentXpMax.addSeparators()}")
                        iterator.add("")
                        iterator.add("§b§lOVERFLOW XP:")
                        iterator.add("§7▸ ${skillInfo.overflowTotalXp.addSeparators()}")
                    }
                }
            }
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enableInSkillMenuTooltip
}
