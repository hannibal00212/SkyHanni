package at.hannibal2.skyhanni.features.skillprogress

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.SkillAPI
import at.hannibal2.skyhanni.api.SkillAPI.activeSkill
import at.hannibal2.skyhanni.api.SkillAPI.lastUpdate
import at.hannibal2.skyhanni.api.SkillAPI.oldSkillInfoMap
import at.hannibal2.skyhanni.api.SkillAPI.showDisplay
import at.hannibal2.skyhanni.api.SkillAPI.skillData
import at.hannibal2.skyhanni.api.SkillAPI.skillXPInfoMap
import at.hannibal2.skyhanni.config.features.skillprogress.SkillProgressConfig
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.PreProfileSwitchEvent
import at.hannibal2.skyhanni.events.SkillOverflowLevelupEvent
import at.hannibal2.skyhanni.utils.ChatUtils.chat
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.interpolate
import at.hannibal2.skyhanni.utils.NumberUtil.roundToPrecision
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import at.hannibal2.skyhanni.utils.SpecialColour
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.horizontalContainer
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.ceil
import kotlin.time.Duration.Companion.seconds

object SkillProgress {

    private val config get() = SkyHanniMod.feature.skillProgress
    private val barConfig get() = config.skillProgressBarConfig
    private val allSkillConfig get() = config.allSkillDisplayConfig
    val etaConfig get() = config.skillETADisplayConfig

    private var skillExpPercentage = 0.0
    private var display = emptyList<Renderable>()
    private var allDisplay = emptyList<List<Any>>()
    private var etaDisplay = emptyList<Renderable>()
    private var lastGainUpdate = SimpleTimeMark.farPast()
    private var maxWidth = 0
    var hideInActionBar = listOf<String>()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (display.isEmpty()) return

        if (showDisplay) {
            renderDisplay()

            if (barConfig.enabled.get()) {
                renderBar()
            }
        }

        if (allSkillConfig.enabled.get()) {
            config.allSkillPosition.renderStringsAndItems(allDisplay, posLabel = "All Skills Display")
        }

        if (etaConfig.enabled.get()) {
            config.etaPosition.renderRenderables(etaDisplay, posLabel = "Skill ETA")
        }
    }

    private fun renderDisplay() {
        when (val textAlignment = config.textAlignmentProperty.get()) {
            SkillProgressConfig.TextAlignment.NONE -> {
                config.displayPosition.renderStringsAndItems(listOf(display), posLabel = "Skill Progress")
            }

            SkillProgressConfig.TextAlignment.CENTERED,
            SkillProgressConfig.TextAlignment.LEFT,
            SkillProgressConfig.TextAlignment.RIGHT -> {
                config.displayPosition.renderRenderables(
                    listOf(Renderable.fixedSizeLine(horizontalContainer(display, textAlignment.alignment), maxWidth)),
                    posLabel = "Skill Progress")
            }

            else -> {}
        }
    }

    private fun renderBar() {
        val progress = if (barConfig.useTexturedBar.get()) {
            val factor = (skillExpPercentage.toFloat().coerceAtMost(1f)) * 182
            maxWidth = 182
            Renderable.texturedProgressBar(factor,
                Color(SpecialColour.specialToChromaRGB(barConfig.barStartColor)),
                texture = barConfig.texturedBar.usedTexture.get(),
                useChroma = barConfig.useChroma.get())

        } else {
            maxWidth = barConfig.regularBar.width
            Renderable.progressBar(skillExpPercentage,
                Color(SpecialColour.specialToChromaRGB(barConfig.barStartColor)),
                Color(SpecialColour.specialToChromaRGB(barConfig.barStartColor)),
                width = maxWidth,
                height = barConfig.regularBar.height,
                useChroma = barConfig.useChroma.get())
        }

        config.barPosition.renderRenderables(listOf(progress), posLabel = "Skill Progress Bar")
    }

    @SubscribeEvent
    fun onProfileSwitch(event: PreProfileSwitchEvent) {
        display = emptyList()
        allDisplay = emptyList()
        etaDisplay = emptyList()
        skillExpPercentage = 0.0
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (lastUpdate.passedSince() > 3.seconds) showDisplay = config.alwaysShow.get()

        if (event.repeatSeconds(1)) {
            allDisplay = formatAllDisplay(drawAllDisplay())
            etaDisplay = drawETADisplay()
        }

        if (event.repeatSeconds(2)) {
            update()
            updateSkillInfo(activeSkill)
        }
    }

    @SubscribeEvent
    fun onLevelUp(event: SkillOverflowLevelupEvent) {
        if (!isEnabled()) return
        if (!config.overflowConfig.enableInChat) return
        val skillName = event.skill.displayName
        val oldLevel = event.oldLevel
        val newLevel = event.newLevel

        val rewards = buildList {
            add("  §r§7§8+§b1 Flexing Point")
            if (newLevel % 5 == 0)
                add("  §r§7§8+§d50 SkyHanni User Luck")
        }
        val messages = listOf(
            "§3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            "  §r§b§lSKILL LEVEL UP §3$skillName §8$oldLevel➜§3$newLevel",
            "",
            "  §r§a§lREWARDS",
            rewards.joinToString("\n"),
            "§3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
        )

        chat(messages.joinToString("\n"), false)
        SoundUtils.createSound("random.levelup", 1f, 1f).playSound()
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        onToggle(
            config.enabled,
            config.alwaysShow,
            config.showActionLeft,
            config.useIcon,
            config.usePercentage,
            config.useSkillName,
            config.overflowConfig.enableInDisplay,
            config.overflowConfig.enableInProgressBar,
            config.overflowConfig.enableInEtaDisplay,
            barConfig.enabled,
            barConfig.useChroma,
            barConfig.useTexturedBar,
            allSkillConfig.enabled,
            etaConfig.enabled
        ) {
            updateDisplay()
            update()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onActionBar(event: ClientChatReceivedEvent) {
        if (!config.hideInActionBar) return
        if (event.type.toInt() != 2) return
        if (event.isCanceled) return
        var msg = event.message.unformattedText
        for (line in hideInActionBar) {
            msg = msg.replace(Regex("\\s*" + Regex.escape(line)), "")
        }
        msg = msg.trim()
        event.message = ChatComponentText(msg)
    }

    fun updateDisplay() {
        display = drawDisplay()
    }

    private fun update() {
        lastGainUpdate = SimpleTimeMark.now()
        skillXPInfoMap.forEach {
            it.value.xpGainLast = it.value.xpGainHour
        }
    }

    private fun formatAllDisplay(map: List<List<Any>>): List<List<Any>> {
        val newList = mutableListOf<List<Any>>()
        if (map.isEmpty()) return newList
        for (index in allSkillConfig.skillEntryList) {
            newList.add(map[index.ordinal])
        }
        return newList
    }

    private fun drawAllDisplay() = buildList {
        val skillMap = skillData ?: return@buildList
        val sortedMap = SkillType.entries.filter { it.displayName.isNotEmpty() }.sortedBy { it.displayName.take(2) }

        for (skill in sortedMap) {
            val skillInfo = skillMap[skill] ?: SkillAPI.SkillInfo(level = -1, overflowLevel = -1)

            val lockedLevels = skillInfo.overflowCurrentXp > skillInfo.overflowCurrentXpMax
            val (level, currentXp, currentXpMax, totalXp) =
                if (config.overflowConfig.enableInAllDisplay.get() && !lockedLevels)
                    LorenzUtils.Quad(skillInfo.overflowLevel, skillInfo.overflowCurrentXp, skillInfo.overflowCurrentXpMax, skillInfo.overflowTotalXp)
                else
                    LorenzUtils.Quad(skillInfo.level, skillInfo.currentXp, skillInfo.currentXpMax, skillInfo.totalXp)

            if (level == -1) {
                addAsSingletonList(Renderable.clickAndHover(
                    "§cOpen your skills menu !",
                    listOf("§eClick here to execute §6/skills"),
                    onClick = { LorenzUtils.sendCommandToServer("skills") }
                ))
            } else {
                val tips = buildList {
                    add("§6Level: §b${level}")
                    add("§6Current XP: §b${currentXp.addSeparators()}")
                    add("§6Needed XP: §b${currentXpMax.addSeparators()}")
                    add("§6Total XP: §b${totalXp.addSeparators()}")
                }
                val nameColor = if (skill == activeSkill) "§e" else "§6"
                addAsSingletonList(Renderable.hoverTips(buildString {
                    append("$nameColor${skill.displayName} $level ")
                    append("§7(")
                    append("§b${currentXp.addSeparators()}")
                    if (currentXpMax != 0L) {
                        append("§6/")
                        append("§b${currentXpMax.addSeparators()}")
                    }
                    append("§7)")
                }, tips))
            }
        }
    }

    private fun drawETADisplay() = buildList {
        val skillInfo = skillData?.get(activeSkill) ?: return@buildList
        val xpInfo = skillXPInfoMap[activeSkill] ?: return@buildList
        val skillInfoLast = oldSkillInfoMap[activeSkill] ?: return@buildList
        oldSkillInfoMap[activeSkill] = skillInfo
        val level = if (config.overflowConfig.enableInEtaDisplay.get()) skillInfo.overflowLevel else skillInfo.level

        val useCustomGoal = etaConfig.enableCustomGoal
        var targetLevel = etaConfig.customGoalValue.toDoubleOrNull() ?: (level + 1).toDouble()
        if (targetLevel.toInt() <= level || targetLevel > 400) targetLevel = (level + 1).toDouble()
        val currentLevelNeededXp = SkillUtil.xpRequiredForLevel(level.toDouble()) + skillInfo.overflowCurrentXp
        val targetNeededXp = SkillUtil.xpRequiredForLevel(targetLevel)
        var remaining = if (useCustomGoal) targetNeededXp - currentLevelNeededXp else skillInfo.overflowCurrentXpMax - skillInfo.overflowCurrentXp

        if (!useCustomGoal) {
            if (skillInfo.overflowCurrentXpMax == skillInfoLast.overflowCurrentXpMax) {
                remaining = interpolate(remaining.toFloat(), (skillInfoLast.overflowCurrentXpMax - skillInfoLast.overflowCurrentXp).toFloat(), lastGainUpdate.toMillis()).toLong()
            }
        }

        add(Renderable.string(buildString {
            append("§6Skill: §7${activeSkill.displayName} ")
            if (useCustomGoal && targetLevel != 0.0) append("§8$level➜§3${targetLevel.toInt()}") else append(level)
        }))
        if (useCustomGoal)
            add(Renderable.string("§7Needed XP: §e${remaining.addSeparators()}"))

        var xpInterp = xpInfo.xpGainHour
        if (xpInfo.xpGainHour < 1000) {
            add(Renderable.string("§7In §cN/A"))
        } else {
            add(Renderable.string("§7In §b${Utils.prettyTime((remaining) * 1000 * 60 * 60 / xpInterp.toLong())} " +
                if (xpInfo.isActive) "" else "§c(PAUSED)"))
        }

        if (xpInfo.xpGainLast == xpInfo.xpGainHour && xpInfo.xpGainHour <= 0) {
            add(Renderable.string("§7XP/h: §cN/A"))
        } else {
            xpInterp = interpolate(xpInfo.xpGainHour, xpInfo.xpGainLast, lastGainUpdate.toMillis())
            add(Renderable.string("§7XP/h: §e${xpInterp.toLong().addSeparators()} " +
                if (xpInfo.isActive) "" else "§c(PAUSED)"))
        }


        val session = xpInfo.timeActive.seconds.format(TimeUnit.HOUR)
        add(Renderable.clickAndHover("§7Session: §e$session ${if (xpInfo.sessionTimerActive) "" else "§c(PAUSED)"}",
            listOf("§eClick to reset!")) {
            xpInfo.sessionTimerActive = false
            xpInfo.shouldStartTimer = true
            xpInfo.timeActive = 0L
            chat("Timer for §b${activeSkill.displayName} §ehas been reset!")
        })
    }

    private fun drawDisplay() = buildList {
        val skillMap = skillData ?: return@buildList
        val skill = skillMap[activeSkill] ?: return@buildList

        val (level, currentXp, currentXpMax, _) = if (config.overflowConfig.enableInDisplay.get())
            LorenzUtils.Quad(skill.overflowLevel, skill.overflowCurrentXp, skill.overflowCurrentXpMax, skill.overflowTotalXp)
        else
            LorenzUtils.Quad(skill.level, skill.currentXp, skill.currentXpMax, skill.totalXp)


        if (config.showLevel.get())
            add(Renderable.string("§9[§d$level§9] "))

        if (config.useIcon.get()) {
            add(Renderable.itemStack(activeSkill.item, 1.2))
        }

        add(Renderable.string(buildString {
            append("§b+${skill.lastGain} ")

            if (config.useSkillName.get())
                append("${activeSkill.displayName} ")

            val (barCurrent, barMax) = if (config.overflowConfig.enableInProgressBar.get())
                Pair(skill.overflowCurrentXp, skill.overflowCurrentXpMax)
            else
                Pair(skill.currentXp, skill.currentXpMax)

            val barPercent = if (barMax == 0L) 100F else 100F * barCurrent / barMax
            skillExpPercentage = (barPercent.toDouble() / 100)

            val percent = if (currentXpMax == 0L) 100F else 100F * currentXp / currentXpMax

            if (config.usePercentage.get())
                append("§7(§6${percent.roundToPrecision(2)}%§7)")
            else {
                if (currentXpMax == 0L)
                    append("§7(§6${currentXp.addSeparators()}§7)")
                else
                    append("§7(§6${currentXp.addSeparators()}§7/§6${currentXpMax.addSeparators()}§7)")
            }

            if (config.showActionLeft.get() && percent != 100f) {
                append(" - ")
                val gain = skill.lastGain.replace(",", "")
                val actionLeft = (ceil(currentXpMax.toDouble() - currentXp) / gain.toDouble()).toLong().addSeparators()
                if (skill.lastGain != "" && !actionLeft.contains("-")) {
                    append("§6$actionLeft Left")
                } else {
                    append("§6∞ Left")
                }
            }
        }))
    }

    private fun updateSkillInfo(skill: SkillType) {
        val xpInfo = skillXPInfoMap.getOrPut(skill) { SkillAPI.SkillXPInfo() }
        val skillInfo = skillData?.get(skill) ?: return
        oldSkillInfoMap[skill] = skillInfo

        val totalXp = skillInfo.currentXp

        if (xpInfo.lastTotalXp > 0) {
            val delta = totalXp - xpInfo.lastTotalXp
            if (delta > 0 && delta < 1000) {
                xpInfo.xpGainQueue.add(0, delta)

                calculateXPHour(xpInfo)
            } else if (xpInfo.timer > 0) {
                xpInfo.timer--
                xpInfo.xpGainQueue.add(0, 0f)

                calculateXPHour(xpInfo)
            } else if (delta <= 0) {
                xpInfo.isActive = false
            }
        }
        xpInfo.lastTotalXp = totalXp.toFloat()
    }

    private fun calculateXPHour(xpInfo: SkillAPI.SkillXPInfo) {
        while (xpInfo.xpGainQueue.size > 30) {
            xpInfo.xpGainQueue.removeLast()
        }

        var totalGain = 0f
        for (f in xpInfo.xpGainQueue) totalGain += f

        xpInfo.xpGainHour = totalGain * (60 * 60) / xpInfo.xpGainQueue.size

        xpInfo.isActive = true
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled.get()
}
