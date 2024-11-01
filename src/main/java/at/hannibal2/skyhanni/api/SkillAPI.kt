package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuSkillLevelJson
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.SkillExpGainEvent
import at.hannibal2.skyhanni.events.SkillOverflowLevelUpEvent
import at.hannibal2.skyhanni.features.skillprogress.SkillProgress
import at.hannibal2.skyhanni.features.skillprogress.SkillType
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.SPACE_SPLITTER
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.XP_NEEDED_FOR_50
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.XP_NEEDED_FOR_60
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.calculateLevelXp
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.calculateSkillLevel
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.getLevelExact
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.getSkillInfo
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.xpRequiredForLevel
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.formatLongOrUserError
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.annotations.Expose
import net.minecraft.command.CommandBase
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.LinkedList
import java.util.regex.Matcher
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SkillAPI {
    private val patternGroup = RepoPattern.group("api.skilldisplay")
    // TODO add regex tests
    private val skillPercentPattern by patternGroup.pattern(
        "skill.percent",
        "\\+(?<gained>[\\d.,]+) (?<skillName>.+) \\((?<progress>[\\d.]+)%\\)",
    )
    private val skillPattern by patternGroup.pattern(
        "skill",
        "\\+(?<gained>[\\d.,]+) (?<skillName>\\w+) \\((?<current>[\\d.,]+)/(?<needed>[\\d.,]+)\\)",
    )
    private val skillMultiplierPattern by patternGroup.pattern(
        "skill.multiplier",
        "\\+(?<gained>[\\d.,]+) (?<skillName>.+) \\((?<current>[\\d.,]+)/(?<needed>[\\d,.]+[kmb])\\)",
    )
    private val skillTabPattern by patternGroup.pattern(
        "skill.tab",
        " (?<type>\\w+)(?: (?<level>\\d+))?: §r§a(?<progress>[0-9.]+)%",
    )
    private val maxSkillTabPattern by patternGroup.pattern(
        "skill.tab.max",
        " (?<type>\\w+) (?<level>\\d+): §r§c§lMAX",
    )
    private val skillTabNoPercentPattern by patternGroup.pattern(
        "skill.tab.nopercent",
        " §r§a(?<type>\\w+)(?: (?<level>\\d+))?: §r§e(?<current>[0-9,.]+)§r§6/§r§e(?<needed>[0-9kmb]+)",
    )

    var skillXPInfoMap = mutableMapOf<SkillType, SkillXPInfo>()
    var oldSkillInfoMap = mutableMapOf<SkillType?, SkillInfo?>()
    val storage get() = ProfileStorageData.profileSpecific?.skillData
    var exactLevelingMap = mapOf<Int, Int>()
    var levelingMap = mapOf<Int, Int>()
    var levelArray = listOf<Int>()
    var activeSkill: SkillType? = null
    var defaultSkillCap = mapOf<String, Int>()

    var showDisplay = false
    var lastUpdate = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        val activeSkill = activeSkill ?: return
        val info = skillXPInfoMap[activeSkill] ?: return
        if (!info.sessionTimerActive) return

        val time = when (activeSkill) {
            SkillType.FARMING -> SkillProgress.etaConfig.farmingPauseTime
            SkillType.MINING -> SkillProgress.etaConfig.miningPauseTime
            SkillType.COMBAT -> SkillProgress.etaConfig.combatPauseTime
            SkillType.FORAGING -> SkillProgress.etaConfig.foragingPauseTime
            SkillType.FISHING -> SkillProgress.etaConfig.fishingPauseTime
            else -> 0
        }
        if (info.lastUpdate.passedSince() > time.seconds) {
            info.sessionTimerActive = false
        }
        if (info.sessionTimerActive) {
            info.timeActive++
        }
    }

    @SubscribeEvent
    fun onActionBarUpdate(event: ActionBarUpdateEvent) {
        val actionBar = event.actionBar.removeColor()
        val components = SPACE_SPLITTER.splitToList(actionBar)
        for (component in components) {
            val matcher = listOf(skillPattern, skillPercentPattern, skillMultiplierPattern)
                .firstOrNull { it.matcher(component).matches() }
                ?.matcher(component)

            if (matcher?.matches() == true) {
                val skillName = matcher.group("skillName")
                val skillType = SkillType.getByNameOrNull(skillName) ?: return
                val skillInfo = storage?.get(skillType) ?: SkillInfo()
                val skillXp = skillXPInfoMap[skillType] ?: SkillXPInfo()
                activeSkill = skillType
                when (matcher.pattern()) {
                    skillPattern -> handleSkillPattern(matcher, skillType, skillInfo)
                    skillPercentPattern -> handleSkillPatternPercent(matcher, skillType)
                    skillMultiplierPattern -> handleSkillPatternMultiplier(matcher, skillType, skillInfo)
                }

                SkillExpGainEvent(skillType, matcher.group("gained").formatDouble()).postAndCatch()

                showDisplay = true
                lastUpdate = SimpleTimeMark.now()
                skillXp.lastUpdate = SimpleTimeMark.now()
                skillXp.sessionTimerActive = true
                SkillProgress.updateDisplay()
                SkillProgress.hideInActionBar = listOf(component)
                return
            }
        }
    }

    @SubscribeEvent
    fun onNEURepoReload(event: NeuRepositoryReloadEvent) {
        val data = event.readConstant<NeuSkillLevelJson>("leveling")

        levelArray = data.levelingXp
        levelingMap = levelArray.withIndex().associate { (index, xp) -> (index + 1) to xp }
        exactLevelingMap = levelArray.withIndex().associate { (index, xp) -> xp to (index + 1) }
        defaultSkillCap = data.levelingCaps
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        val inventoryName = event.inventoryName
        for (stack in event.inventoryItems.values) {
            val lore = stack.getLore()
            if (inventoryName == "Your Skills" &&
                lore.any { it.contains("Click to view!") || it.contains("Not unlocked!") }
            ) {
                val cleanName = stack.cleanName()
                val split = cleanName.split(" ")
                val skillName = split.first()
                val skill = SkillType.getByNameOrNull(skillName) ?: continue
                val skillLevel = if (split.size > 1) split.last().romanToDecimalIfNecessary() else 0
                val skillInfo = storage?.getOrPut(skill) { SkillInfo() }

                for ((lineIndex, line) in lore.withIndex()) {
                    val cleanLine = line.removeColor()
                    if (!cleanLine.startsWith("                    ")) continue
                    val previousLine = stack.getLore()[lineIndex - 1]
                    val progress = cleanLine.substring(cleanLine.lastIndexOf(' ') + 1)
                    if (previousLine == "§7§8Max Skill level reached!") {
                        val totalXp = progress.formatLong()
                        val (overflowLevel, overflowCurrent, overflowNeeded, overflowTotal) = calculateSkillLevel(
                            totalXp,
                            defaultSkillCap[skill.lowercaseName] ?: 60,
                        )

                        skillInfo?.apply {
                            this.overflowLevel = overflowLevel
                            this.overflowCurrentXp = overflowCurrent
                            this.overflowCurrentXpMax = overflowNeeded
                            this.overflowTotalXp = overflowTotal

                            this.totalXp = totalXp
                            this.level = skillLevel
                            this.currentXp = totalXp
                            this.currentXpMax = 0L
                        }
                    } else {
                        val splitProgress = progress.split("/")
                        val currentXp = splitProgress.first().formatLong()
                        val neededXp = splitProgress.last().formatLong()
                        val levelXp = calculateLevelXp(skillLevel - 1).toLong()

                        skillInfo?.apply {
                            this.currentXp = currentXp
                            this.level = skillLevel
                            this.currentXpMax = neededXp
                            this.totalXp = levelXp + currentXp

                            this.overflowCurrentXp = currentXp
                            this.overflowLevel = skillLevel
                            this.overflowCurrentXpMax = neededXp
                            this.overflowTotalXp = levelXp + currentXp
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Skills")
        val storage = storage
        if (storage == null) {
            event.addIrrelevant("SkillMap is empty")
            return
        }

        event.addIrrelevant {
            val activeSkill = activeSkill
            if (activeSkill == null) {
                add("activeSkill is null")
            } else {
                add("active skill:")
                storage[activeSkill]?.let { skillInfo ->
                    addDebug(activeSkill, skillInfo)
                }
                add("")
                add("")
            }

            for ((skillType, skillInfo) in storage) {
                addDebug(skillType, skillInfo)
            }
        }
    }

    private fun MutableList<String>.addDebug(skillType: SkillType, skillInfo: SkillInfo) {
        add("Name: $skillType")
        add("-  Level: ${skillInfo.level}")
        add("-  CurrentXp: ${skillInfo.currentXp}")
        add("-  CurrentXpMax: ${skillInfo.currentXpMax}")
        add("-  TotalXp: ${skillInfo.totalXp}")
        add("-  OverflowLevel: ${skillInfo.overflowLevel}")
        add("-  OverflowCurrentXp: ${skillInfo.overflowCurrentXp}")
        add("-  OverflowCurrentXpMax: ${skillInfo.overflowCurrentXpMax}")
        add("-  OverflowTotalXp: ${skillInfo.overflowTotalXp}")
        add("-  CustomGoalLevel: ${skillInfo.customGoalLevel}\n")
    }

    private fun handleSkillPattern(matcher: Matcher, skillType: SkillType, skillInfo: SkillInfo) {
        val currentXp = matcher.group("current").formatLong()
        val maxXp = matcher.group("needed").formatLong()
        val level = getLevelExact(maxXp)

        val cap = defaultSkillCap[skillType.lowercaseName] ?: 60
        val add = when (cap) {
            50 -> XP_NEEDED_FOR_50
            60 -> XP_NEEDED_FOR_60
            else -> 0
        }

        val (levelOverflow, currentOverflow, currentMaxOverflow, totalOverflow) =
            calculateSkillLevel(currentXp + add, cap)

        if (skillInfo.overflowLevel > 60 && levelOverflow == skillInfo.overflowLevel + 1)
            SkillOverflowLevelUpEvent(skillType, skillInfo.overflowLevel, levelOverflow).postAndCatch()

        skillInfo.apply {
            this.level = level
            this.currentXp = currentXp
            this.currentXpMax = maxXp
            this.totalXp = currentXp

            this.overflowLevel = levelOverflow
            this.overflowCurrentXp = currentOverflow
            this.overflowCurrentXpMax = currentMaxOverflow
            this.overflowTotalXp = totalOverflow

            this.lastGain = matcher.group("gained")
        }
        storage?.set(skillType, skillInfo)
    }

    private fun handleSkillPatternPercent(matcher: Matcher, skillType: SkillType) {
        var current = 0L
        var needed = 0L
        var xpPercentage = 0.0
        var isPercentPatternFound = false
        var tablistLevel: Int? = null

        for (line in TabListData.getTabList()) {
            skillTabPattern.matchMatcher(line) {
                if (group("type") == skillType.displayName) {
                    tablistLevel = group("level").toInt()
                    isPercentPatternFound = true
                    if (group("type").lowercase() != activeSkill?.lowercaseName) tablistLevel = null
                }
            }

            maxSkillTabPattern.matchMatcher(line) {
                tablistLevel = group("level").toInt()
                if (group("type").lowercase() != activeSkill?.lowercaseName) tablistLevel = null
            }

            skillTabNoPercentPattern.matchMatcher(line) {
                if (group("type") == skillType.displayName) {
                    tablistLevel = group("level").toInt()
                    current = group("current").formatLong()
                    needed = group("needed").formatLong()
                    isPercentPatternFound = false
                    return@matchMatcher
                }
            }
            xpPercentage = matcher.group("progress").formatDouble()
        }

        val existingLevel = getSkillInfo(skillType) ?: SkillInfo()
        tablistLevel?.let { level ->
            if (isPercentPatternFound) {
                val levelXp = calculateLevelXp(existingLevel.level - 1)
                val nextLevelDiff = levelArray.getOrNull(level)?.toDouble() ?: 7_600_000.0
                val nextLevelProgress = nextLevelDiff * xpPercentage / 100
                val totalXp = levelXp + nextLevelProgress
                updateSkillInfo(
                    existingLevel,
                    level,
                    nextLevelProgress.toLong(),
                    nextLevelDiff.toLong(),
                    totalXp.toLong(),
                    matcher.group("gained"),
                )
            } else {
                val exactLevel = getLevelExact(needed)
                val levelXp = calculateLevelXp(existingLevel.level - 1).toLong() + current
                updateSkillInfo(existingLevel, exactLevel, current, needed, levelXp, matcher.group("gained"))
            }
            storage?.set(skillType, existingLevel)
        }
    }

    private fun updateSkillInfo(existingLevel: SkillInfo, level: Int, currentXp: Long, maxXp: Long, totalXp: Long, gained: String) {
        val cap = defaultSkillCap[activeSkill?.lowercaseName] ?: 60
        val add = if (level >= 50) {
            when (cap) {
                50 -> XP_NEEDED_FOR_50
                60 -> XP_NEEDED_FOR_60
                else -> 0
            }
        } else {
            0
        }

        val (levelOverflow, currentOverflow, currentMaxOverflow, totalOverflow) =
            calculateSkillLevel(totalXp + add, defaultSkillCap[activeSkill?.lowercaseName] ?: 60)

        existingLevel.apply {
            this.totalXp = totalXp
            this.currentXp = currentXp
            this.currentXpMax = maxXp
            this.level = level

            this.overflowTotalXp = totalOverflow
            this.overflowCurrentXp = currentOverflow
            this.overflowCurrentXpMax = currentMaxOverflow
            this.overflowLevel = levelOverflow

            this.lastGain = gained
        }
    }

    private fun handleSkillPatternMultiplier(matcher: Matcher, skillType: SkillType, skillInfo: SkillInfo) {
        val currentXp = matcher.group("current").formatLong()
        val maxXp = matcher.group("needed").formatLong()
        val level = getLevelExact(maxXp)
        val levelXp = calculateLevelXp(level - 1).toLong() + currentXp
        val (currentLevel, currentOverflow, currentMaxOverflow, totalOverflow) =
            calculateSkillLevel(currentXp, defaultSkillCap[skillType.lowercaseName] ?: 60)

        skillInfo.apply {
            this.overflowCurrentXp = currentOverflow
            this.overflowCurrentXpMax = currentMaxOverflow
            this.overflowTotalXp = totalOverflow
            this.overflowLevel = currentLevel

            this.currentXp = currentXp
            this.currentXpMax = maxXp
            this.totalXp = levelXp
            this.level = level

            this.lastGain = matcher.group("gained")
        }
        storage?.set(skillType, skillInfo)
    }

    fun onCommand(it: Array<String>) {
        if (it.isEmpty()) {
            commandHelp()
            return
        }

        val first = it.first()
        if (it.size == 1) {
            when (first) {
                "goal" -> {
                    ChatUtils.chat("§bSkill Custom Goal Level")
                    val map = storage?.filter { it.value.customGoalLevel != 0 } ?: return
                    if (map.isEmpty()) {
                        ChatUtils.userError("You haven't set any custom goals yet!")
                    }
                    map.forEach { (skill, data) ->
                        ChatUtils.chat("§e${skill.displayName}: §b${data.customGoalLevel}")
                    }
                    return
                }
            }
        }

        if (it.size == 2) {
            val second = it[1]
            when (first) {
                "levelwithxp" -> {
                    val xp = second.formatLongOrUserError() ?: return
                    val (overflowLevel, current, needed, _) = calculateSkillLevel(xp, 60)
                    ChatUtils.chat(
                        "With §b${xp.addSeparators()} §eXP you would be level §b$overflowLevel " +
                            "§ewith progress (§b${current.addSeparators()}§e/§b${needed.addSeparators()}§e) XP",
                    )
                    return
                }

                "xpforlevel" -> {
                    val level = second.toIntOrNull()
                    if (level == null) {
                        ChatUtils.userError("Not a valid number: '$second'")
                        return
                    }
                    val neededXP = xpRequiredForLevel(level)
                    ChatUtils.chat("You need §b${neededXP.addSeparators()} §eXP to reach level §b${level.toDouble()}")
                    return
                }

                "goal" -> {
                    val rawSkill = it[1].lowercase()
                    val skillType = SkillType.getByNameOrNull(rawSkill)
                    if (skillType == null) {
                        ChatUtils.userError("Unknown Skill type: $rawSkill")
                        return
                    }
                    val skill = storage?.get(skillType) ?: return
                    skill.customGoalLevel = 0
                    ChatUtils.chat("Custom goal level for §b${skillType.displayName} §ereset")
                }
            }
        }
        if (it.size == 3) {
            when (first) {
                "goal" -> {
                    val rawSkill = it[1].lowercase()
                    val skillType = SkillType.getByNameOrNull(rawSkill)
                    if (skillType == null) {
                        ChatUtils.userError("Unknown Skill type: $rawSkill")
                        return
                    }
                    val rawLevel = it[2]
                    val targetLevel = rawLevel.toIntOrNull()
                    if (targetLevel == null) {
                        ChatUtils.userError("$rawLevel is not a valid number.")
                        return
                    }
                    val skill = storage?.get(skillType) ?: return

                    if (targetLevel <= skill.overflowLevel) {
                        ChatUtils.userError(
                            "Custom goal level ($targetLevel) must be greater than your current level (${skill.overflowLevel}).",
                        )
                        return
                    }

                    skill.customGoalLevel = targetLevel
                    ChatUtils.chat("Custom goal level for §b${skillType.displayName} §eset to §b$targetLevel")
                    return
                }
            }
        }
        commandHelp()
    }

    fun onComplete(strings: Array<String>): List<String> {
        return when (strings.size) {
            1 -> listOf("levelwithxp", "xpforlevel", "goal")
            2 -> if (strings[0].lowercase() == "goal") CommandBase.getListOfStringsMatchingLastWord(
                strings,
                SkillType.entries.map { it.displayName },
            )
            else
                listOf()

            else -> listOf()
        }
    }

    private fun commandHelp() {
        ChatUtils.chat(
            listOf(
                "§6/shskills levelwithxp <xp> - §bGet a level with the given current XP.",
                "§6/shskills xpforlevel <desiredLevel> - §bGet how much XP you need for a desired level.",
                "§6/shskills goal - §bView your current goal",
                "§6/shskills goal <skill> <level> - §bDefine your goal for <skill>",
                "",
            ).joinToString("\n"),
            prefix = false,
        )
    }

    data class SkillInfo(
        @Expose var level: Int = 0,
        @Expose var totalXp: Long = 0,
        @Expose var currentXp: Long = 0,
        @Expose var currentXpMax: Long = 0,
        @Expose var overflowLevel: Int = 0,
        @Expose var overflowCurrentXp: Long = 0,
        @Expose var overflowTotalXp: Long = 0,
        @Expose var overflowCurrentXpMax: Long = 0,
        @Expose var lastGain: String = "",
        @Expose var customGoalLevel: Int = 0,
    )

    data class SkillXPInfo(
        var lastTotalXp: Float = 0f,
        var xpGainQueue: LinkedList<Float> = LinkedList(),
        var xpGainHour: Float = 0f,
        var xpGainLast: Float = 0f,
        var timer: Int = 3,
        var sessionTimerActive: Boolean = false,
        var isActive: Boolean = false,
        var lastUpdate: SimpleTimeMark = SimpleTimeMark.farPast(),
        var timeActive: Long = 0L,
    )
}
