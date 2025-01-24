package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
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
import at.hannibal2.skyhanni.features.skillprogress.SkillUtil.calculateLevelXP
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
import java.util.LinkedList
import java.util.regex.Matcher
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SkillApi {
    private val patternGroup = RepoPattern.group("api.skilldisplay")

    /**
     * REGEX-TEST: +1.1 Mining (48.39%)
     */
    private val skillPercentPattern by patternGroup.pattern(
        "skill.percent",
        "\\+(?<gained>[\\d.,]+) (?<skillName>.+) \\((?<progress>[\\d.]+)%\\)",
    )

    /**
     * REGEX-TEST: +6.3 Foraging (24/750)
     */
    private val skillMultiplierPattern by patternGroup.pattern(
        "skill.multiplier",
        "\\+(?<gained>[\\d.,]+) (?<skillName>.+) \\((?<current>[\\d.,]+)\\/(?<needed>[\\d,.]+[kmb]?)\\)",
    )

    // TODO find out whats going on here
    /**
     * REGEX-TEST: Farming 35: §r§a12.4%
     */
    private val skillTabPattern by patternGroup.pattern(
        "skill.tab",
        " (?<type>\\w+)(?: (?<level>\\d+))?: §r§a(?<progress>[0-9.]+)%",
    )

    // TODO add regex tests
    private val maxSkillTabPattern by patternGroup.pattern(
        "skill.tab.max",
        " (?<type>\\w+) (?<level>\\d+): §r§c§lMAX",
    )

    // TODO add regex tests
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

    var showDisplay = false
    var lastUpdate = SimpleTimeMark.farPast()

    @HandleEvent
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

    @HandleEvent
    fun onActionBarUpdate(event: ActionBarUpdateEvent) {
        val actionBar = event.actionBar.removeColor()
        val components = SPACE_SPLITTER.splitToList(actionBar)
        for (component in components) {
            val matcher = listOf(skillPercentPattern, skillMultiplierPattern).firstOrNull {
                it.matcher(component).matches()
            }?.matcher(component)

            if (matcher?.matches() != true) continue
            val skillName = matcher.group("skillName")
            val skillType = SkillType.getByNameOrNull(skillName) ?: return
            val skillInfo = storage?.get(skillType) ?: SkillInfo()
            val skillXP = skillXPInfoMap[skillType] ?: SkillXPInfo()
            activeSkill = skillType
            when (matcher.pattern()) {
                skillPercentPattern -> handleSkillPatternPercent(matcher, skillType)
                skillMultiplierPattern -> handleSkillPatternMultiplier(matcher, skillType, skillInfo)
            }

            SkillExpGainEvent(skillType, matcher.group("gained").formatDouble()).post()

            showDisplay = true
            lastUpdate = SimpleTimeMark.now()
            skillXP.lastUpdate = SimpleTimeMark.now()
            skillXP.sessionTimerActive = true
            SkillProgress.updateDisplay()
            SkillProgress.hideInActionBar = listOf(component)
            return
        }
    }

    @HandleEvent
    fun onNEURepoReload(event: NeuRepositoryReloadEvent) {
        val data = event.readConstant<NeuSkillLevelJson>("leveling")

        levelArray = data.levelingXP
        levelingMap = levelArray.withIndex().associate { (index, xp) -> (index + 1) to xp }
        exactLevelingMap = levelArray.withIndex().associate { (index, xp) -> xp to (index + 1) }
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (event.inventoryName != "Your Skills") return
        for (stack in event.inventoryItems.values) {
            val lore = stack.getLore()
            if (lore.none { it.contains("Click to view!") || it.contains("Not unlocked!") }) continue
            val cleanName = stack.cleanName()
            val split = cleanName.split(" ")
            val skillName = split.first()
            val skill = SkillType.getByNameOrNull(skillName) ?: continue
            val skillLevel = if (split.size > 1) split.last().romanToDecimalIfNecessary() else 0
            val skillInfo = storage?.getOrPut(skill, ::SkillInfo) ?: continue

            lore@ for ((index, line) in lore.withIndex()) {
                val cleanLine = line.removeColor()
                if (!cleanLine.startsWith("                    ")) continue@lore
                val previousLine = lore.getOrNull(index - 1) ?: continue@lore
                val progress = cleanLine.substring(cleanLine.lastIndexOf(' ') + 1)
                if (previousLine == "§7§8Max Skill level reached!") {
                    onUpdateMax(progress, skill, skillInfo, skillLevel)
                } else {
                    onUpdateNotMax(progress, skillLevel, skillInfo)
                }
            }
        }
    }

    private fun onUpdateMax(progress: String, skill: SkillType, skillInfo: SkillInfo, skillLevel: Int) {
        val totalXP = progress.formatLong()
        val cap = skill.maxLevel
        val maxXP = if (cap == 50) XP_NEEDED_FOR_50 else XP_NEEDED_FOR_60
        val currentXP = totalXP - maxXP
        val (overflowLevel, overflowCurrent, overflowNeeded, overflowTotal) = calculateSkillLevel(totalXP, cap)

        skillInfo.apply {
            this.overflowLevel = overflowLevel
            this.overflowCurrentXP = overflowCurrent
            this.overflowCurrentXPMax = overflowNeeded
            this.overflowTotalXP = overflowTotal

            this.totalXP = totalXP
            this.level = skillLevel
            this.currentXP = currentXP
            this.currentXPMax = 0L
        }
    }

    private fun onUpdateNotMax(progress: String, skillLevel: Int, skillInfo: SkillInfo) {
        val splitProgress = progress.split("/")
        val currentXP = splitProgress.first().formatLong()
        val neededXP = splitProgress.last().formatLong()
        val levelXP = calculateLevelXP(skillLevel - 1).toLong()

        skillInfo.apply {
            this.currentXP = currentXP
            this.level = skillLevel
            this.currentXPMax = neededXP
            this.totalXP = levelXP + currentXP

            this.overflowCurrentXP = currentXP
            this.overflowLevel = skillLevel
            this.overflowCurrentXPMax = neededXP
            this.overflowTotalXP = levelXP + currentXP
        }
    }

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
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
        add("-  CurrentXp: ${skillInfo.currentXP}")
        add("-  CurrentXpMax: ${skillInfo.currentXPMax}")
        add("-  TotalXp: ${skillInfo.totalXP}")
        add("-  OverflowLevel: ${skillInfo.overflowLevel}")
        add("-  OverflowCurrentXp: ${skillInfo.overflowCurrentXP}")
        add("-  OverflowCurrentXpMax: ${skillInfo.overflowCurrentXPMax}")
        add("-  OverflowTotalXp: ${skillInfo.overflowTotalXP}")
        add("-  CustomGoalLevel: ${skillInfo.customGoalLevel}\n")
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
        val level = tablistLevel ?: return
        if (isPercentPatternFound) {
            val levelXP = calculateLevelXP(existingLevel.level - 1)
            val nextLevelDiff = levelArray.getOrNull(level)?.toDouble() ?: 7_600_000.0
            val nextLevelProgress = nextLevelDiff * xpPercentage / 100
            val totalXP = levelXP + nextLevelProgress
            updateSkillInfo(
                existingLevel,
                level,
                nextLevelProgress.toLong(),
                nextLevelDiff.toLong(),
                totalXP.toLong(),
                matcher.group("gained"),
            )
        } else {
            val exactLevel = getLevelExact(needed)
            val levelXP = calculateLevelXP(existingLevel.level - 1).toLong() + current
            updateSkillInfo(existingLevel, exactLevel, current, needed, levelXP, matcher.group("gained"))
        }
        storage?.set(skillType, existingLevel)
    }

    private fun updateSkillInfo(existingLevel: SkillInfo, level: Int, currentXP: Long, maxXP: Long, totalXP: Long, gained: String) {
        val cap = activeSkill?.maxLevel
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
            calculateSkillLevel(totalXP + add, cap ?: 60)

        existingLevel.apply {
            this.totalXP = totalXP
            this.currentXP = currentXP
            this.currentXPMax = maxXP
            this.level = level

            this.overflowTotalXP = totalOverflow
            this.overflowCurrentXP = currentOverflow
            this.overflowCurrentXPMax = currentMaxOverflow
            this.overflowLevel = levelOverflow

            this.lastGain = gained
        }
    }

    private fun handleSkillPatternMultiplier(matcher: Matcher, skillType: SkillType, skillInfo: SkillInfo) {
        val currentXP = matcher.group("current").formatLong()
        val maxXP = matcher.group("needed").formatLong()

        // when at overflow, we dont need to subtract one level in the logic below
        val minus = if (maxXP == 0L) 0 else 1
        val level = getLevelExact(maxXP) - minus

        val levelXP = calculateLevelXP(level - 1).toLong() + currentXP
        val (currentLevel, currentOverflow, currentMaxOverflow, totalOverflow) =
            calculateSkillLevel(levelXP, skillType.maxLevel)

        if (skillInfo.overflowLevel > skillType.maxLevel && currentLevel == skillInfo.overflowLevel + 1) {
            SkillOverflowLevelUpEvent(skillType, skillInfo.overflowLevel, currentLevel).post()
        }

        skillInfo.apply {
            this.overflowCurrentXP = currentOverflow
            this.overflowCurrentXPMax = currentMaxOverflow
            this.overflowTotalXP = totalOverflow
            this.overflowLevel = currentLevel

            this.currentXP = currentXP
            this.currentXPMax = maxXP
            this.totalXP = levelXP
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
                    return
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
            ) else listOf()

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
        @Expose var totalXP: Long = 0,
        @Expose var currentXP: Long = 0,
        @Expose var currentXPMax: Long = 0,
        @Expose var overflowLevel: Int = 0,
        @Expose var overflowCurrentXP: Long = 0,
        @Expose var overflowTotalXP: Long = 0,
        @Expose var overflowCurrentXPMax: Long = 0,
        @Expose var lastGain: String = "",
        @Expose var customGoalLevel: Int = 0,
    )

    data class SkillXPInfo(
        var lastTotalXP: Float = 0f,
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
