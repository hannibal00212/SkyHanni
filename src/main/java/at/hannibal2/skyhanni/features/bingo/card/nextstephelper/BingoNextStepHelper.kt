package at.hannibal2.skyhanni.features.bingo.card.nextstephelper

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.CollectionAPI
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.SkillExperience
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.bingo.BingoAPI
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.ChatMessageStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.CollectionStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.CraftStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.IslandVisitStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.ItemsStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.NextStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.ObtainCrystalStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.PartialProgressItemsStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.ProgressionStep
import at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps.SkillLevelStep
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object BingoNextStepHelper {

    private val config get() = SkyHanniMod.feature.event.bingo.bingoCard
    private var dirty = true

    private val patternGroup = RepoPattern.group("bingo.steps")
    private val crystalObtainedPattern by patternGroup.pattern(
        "crystal.obtained",
        " *§r§e(?<crystalName>Topaz|Sapphire|Jade|Amethyst|Amber) Crystal"
    )
    private val collectionPattern by patternGroup.pattern(
        "collection",
        "Reach (?<amount>[0-9]+(?:,\\d+)*) (?<name>.*) Collection\\."
    )
    private val crystalPattern by patternGroup.pattern(
        "crystal.obtain",
        "Obtain a (?<name>\\w+) Crystal in the Crystal Hollows\\."
    )
    private val skillPattern by patternGroup.pattern(
        "skill",
        "Obtain level (?<level>.*) in the (?<skill>.*) Skill."
    )
    private val crystalFoundPattern by patternGroup.pattern(
        "crystal.found",
        " *§r§5§l✦ CRYSTAL FOUND §r§7\\(.§r§7/5§r§7\\)"
    )

    private val itemIslandRequired = mutableMapOf<String, IslandVisitStep>()
    private val itemPreconditions = mutableMapOf<String, NextStep>()
    private val islands = mutableMapOf<IslandType, IslandVisitStep>()
    private const val RHYS_TASK_NAME = "30x Enchanted Minerals (Redstone, Lapis Lazuli, Coal) (for Rhys)"

    private val finalSteps = mutableListOf<NextStep>()
    private var currentSteps = emptyList<NextStep>()
    var currentHelp = emptyList<String>()

    fun command() {
        updateResult(true)
    }

    private fun updateResult(print: Boolean = false) {
        if (print) println()
        currentSteps = listOf()
        for (step in finalSteps) {
            printRequirements(step, print)
            if (print) println()
        }

        currentHelp = drawDisplay(print)
    }

    private fun drawDisplay(print: Boolean): MutableList<String> {
        val newCurrentHelp = mutableListOf<String>()
        newCurrentHelp.add("§6Bingo Step Helper:")

        if (currentSteps.isEmpty()) {
            newCurrentHelp.add("§cOpen the §e/bingo §ccard.")
        }
        for (currentStep in currentSteps) {
            val text = getName(currentStep)
            newCurrentHelp.add("  §7$text")
            if (print) println(text)
        }
        if (print) println()
        return newCurrentHelp
    }

    private fun printRequirements(step: NextStep, print: Boolean, parentDone: Boolean = false, depth: Int = 0) {
        if (print) println(getName(step, parentDone, depth))
        var requirementsToDo = 0
        for (requirement in step.requirements) {
            printRequirements(requirement, print, step.done || parentDone, depth + 1)
            if (!requirement.done) {
                requirementsToDo++
            }
        }

        if (!step.done && !parentDone && requirementsToDo == 0 && !currentSteps.contains(step)) {
            currentSteps = currentSteps.editCopy { add(step) }
        }
    }

    private fun getName(step: NextStep, parentDone: Boolean = false, depth: Int = 0): String {
        val prefix = "  ".repeat(depth) + if (step.done) "[DONE] " else if (parentDone) "[done] " else ""
        val suffix = if (step is ProgressionStep) progressDisplay(step) else ""
        return prefix + step.displayName + suffix
    }

    private fun progressDisplay(step: ProgressionStep): String {
        val having = step.amountHaving
        return if (having > 0) {
            val needed = step.amountNeeded
            val percentage = LorenzUtils.formatPercentage(having.toDouble() / needed)
            " $percentage (${having.addSeparators()}/${needed.addSeparators()})"
        } else ""
    }

    init {
        reset()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.isBingoProfile) return
        if (!config.enabled) return

        if (event.repeatSeconds(1)) {
            update()
            updateIslandsVisited()
        }
        if (event.isMod(5)) {
            updateCurrentSteps()
        }
    }

    private var nextMessageIsCrystal = false

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.isBingoProfile) return
        if (!config.enabled) return

        for (currentStep in currentSteps) {
            if (currentStep is ObtainCrystalStep) {
                crystalFoundPattern.matchMatcher(event.message) {
                    nextMessageIsCrystal = true
                    return
                }
                if (nextMessageIsCrystal) {
                    nextMessageIsCrystal = false
                    crystalObtainedPattern.matchMatcher(event.message) {
                        if (group("crystalName") == currentStep.crystalName)
                            currentStep.done()
                    }
                }
            }
            if (currentStep is PartialProgressItemsStep && currentStep.displayName == RHYS_TASK_NAME &&
                event.message == "§e[NPC] §dRhys§f: §rThank you for the items!§r"
            ) {
                currentStep.amountHavingHidden -= 10
            }
        }
        // TODO add thys message
//        if (event.message == "thys message") {
//            thys.done()
//        }
    }

    private fun updateCurrentSteps() {
        for (step in currentSteps.toMutableList()) {
            if (step is ItemsStep) {
                var totalCount = 0L
                for ((itemName, multiplier) in step.variants) {
                    val count = InventoryUtils.countItemsInLowerInventory { it.name.removeColor() == itemName }
                    totalCount += count * multiplier
                }
                if (step.amountHaving != totalCount) {
                    step.amountHaving = totalCount
                    if (totalCount >= step.amountNeeded) {
                        step.done()
                    }
                    updateResult()
                }
            }
            if (step is SkillLevelStep) {
                val expForSkill = SkillExperience.getExpForSkill(step.skillName.lowercase())
                if (step.amountHaving != expForSkill) {
                    step.amountHaving = expForSkill
                    if (expForSkill >= step.amountNeeded) {
                        step.done()
                    }
                    updateResult()
                }
            }
            if (step is CollectionStep) {
                val counter = CollectionAPI.getCollectionCounter(step.internalName) ?: 0
                if (step.amountHaving != counter) {
                    step.amountHaving = counter
                    if (counter >= step.amountNeeded) {
                        step.done()
                    }
                    updateResult()
                }
            }
        }
    }

    private fun NextStep.done(silent: Boolean = false) {
        if (done) return
        done = true
        updateResult()
        if (!silent && config.stepHelper) {
            ChatUtils.chat("A bingo goal step is done! ($displayName)")
        }
    }

    private fun updateIslandsVisited() {
        for (step in islands.values) {
            val island = step.island
            if (island == LorenzUtils.skyBlockIsland) {
                step.done()
            }
        }
    }

    private fun update() {
        val personalGoals = BingoAPI.personalGoals.filter { !it.done }
        if (personalGoals.isEmpty()) {
            if (!dirty) {
                reset()
                dirty = true
            }
            return
        }

        if (!dirty) return
        dirty = false

        for (goal in personalGoals) {
            val description = goal.description
            val bingoCardStep = readDescription(description.removeColor())
            if (bingoCardStep == null) {
//                 println("Warning: Could not find bingo steps for $description")
            } else {
                finalSteps.add(bingoCardStep)
            }
        }

        updateResult()
    }

    private fun readDescription(description: String): NextStep? {
        collectionPattern.matchMatcher(description) {
            val amount = group("amount").formatInt()
            val name = group("name")

            return CollectionStep(name, amount) withItemIslandRequirement name
        }

        if (description == "Craft an Emerald Ring.") {
            return CraftStep("Emerald Ring") requires (
                ItemsStep(
                    "32x Enchanted Emerald",
                    "Emerald",
                    160 * 32,
                    mapOf("Emerald" to 1, "Enchanted Emerald" to 160)
                ) requires IslandType.DWARVEN_MINES.getStep()
                )
        }

        if (description == "Obtain a Mathematical Hoe Blueprint.") {
            return CraftStep("Mathematical Hoe Blueprint") requires (
                ItemsStep(
                    "32x Jacob's Ticket",
                    "Jacob's Ticket",
                    32,
                    mapOf("Jacob's Ticket" to 1)
                ).addItemRequirements() requires IslandType.GARDEN.getStep()
                )
        }

        crystalPattern.matchMatcher(description) {
            val crystal = group("name")
            return ObtainCrystalStep(crystal) requires IslandType.CRYSTAL_HOLLOWS.getStep()
        }

        skillPattern.matchMatcher(description) {
            val level = group("level").toInt()
            val skill = group("skill")
            return SkillLevelStep(skill, level)
        }

        return null
    }

    private fun <T : NextStep> T.makeFinalStep(): T {
        finalSteps.add(this)
        return this
    }

    private infix fun <T : NextStep> T.withItemIslandRequirement(itemName: String): T {
        itemIslandRequired[itemName]?.let { this requires it }
        return this
    }

    private infix fun <T : NextStep> T.requires(other: NextStep): T {
        requirements.add(other)
        return this
    }

    private fun IslandType.getStep() = islands.getOrPut(this) { IslandVisitStep(this) }

    private fun reset() {
        islands.clear()
        finalSteps.clear()

        itemIslandRequired["Acacia Wood"] = IslandType.THE_PARK.getStep()
        itemIslandRequired["Redstone"] = IslandType.DEEP_CAVERNS.getStep()
        itemIslandRequired["Lapis Lazuli"] = IslandType.DEEP_CAVERNS.getStep()
        itemIslandRequired["Coal"] = IslandType.DEEP_CAVERNS.getStep()
        itemIslandRequired["Slimeball"] = IslandType.DEEP_CAVERNS.getStep()
        itemIslandRequired["Emerald"] = IslandType.DEEP_CAVERNS.getStep()
        itemIslandRequired["Mithril"] = IslandType.DEEP_CAVERNS.getStep()

        IslandType.GOLD_MINES.getStep() requires IslandType.HUB.getStep()
        IslandType.GOLD_MINES.getStep() requires SkillLevelStep("Mining", 1)

        IslandType.DEEP_CAVERNS.getStep() requires IslandType.GOLD_MINES.getStep()
        IslandType.DEEP_CAVERNS.getStep() requires SkillLevelStep("Mining", 5)

        rhys()
        IslandType.DWARVEN_MINES.getStep() requires SkillLevelStep(
            "Mining",
            12
        ).also { it requires IslandType.THE_FARMING_ISLANDS.getStep() }

        IslandType.CRYSTAL_HOLLOWS.getStep() requires IslandType.DWARVEN_MINES.getStep()

        // TODO add SkyBlock level requirement
//        IslandType.GARDEN.getStep() requires SkyBlockLevelStep(6)
        IslandType.GARDEN.getStep() requires IslandType.HUB.getStep()

        val farmingContest = ChatMessageStep("Farming Contest")
        farmingContest requires SkillLevelStep("Farming", 10)
        itemPreconditions["Jacob's Ticket"] = farmingContest

        IslandType.DWARVEN_MINES.getStep().makeFinalStep()
        ChatMessageStep("Get Ender Armor").makeFinalStep() requires IslandType.THE_END.getStep()
        IslandType.THE_END.getStep() requires SkillLevelStep(
            "Combat",
            12
        ).also { it requires IslandType.DEEP_CAVERNS.getStep() }
//        enchantedCharcoal(7)
//        compactor(7)
    }

    private fun rhys() {
        val redstoneForRhys = PartialProgressItemsStep(
            RHYS_TASK_NAME,
            "Redstone",
            160 * 10,
            mapOf("Redstone" to 1, "Enchanted Redstone" to 160)
        )
        redstoneForRhys requires IslandType.DEEP_CAVERNS.getStep()

        val lapisForRhys = PartialProgressItemsStep(
            RHYS_TASK_NAME,
            "Lapis Lazuli",
            160 * 10,
            mapOf("Lapis Lazuli" to 1, "Enchanted Lapis Lazuli" to 160)
        )
        lapisForRhys requires IslandType.DEEP_CAVERNS.getStep()

        val coalForRhys = PartialProgressItemsStep(
            RHYS_TASK_NAME,
            "Coal",
            160 * 10,
            mapOf("Coal" to 1, "Enchanted Coal" to 160)
        )
        coalForRhys requires IslandType.DEEP_CAVERNS.getStep()

        val mines = IslandType.DWARVEN_MINES.getStep()
        mines requires redstoneForRhys
        mines requires lapisForRhys
        mines requires coalForRhys
    }

    private fun compactor(amount: Long) {
        val compactorForMinions = ItemsStep(
            "Compactor (for Minions)",
            "Compactor",
            amount,
            mapOf("Compactor" to 1)
        ).apply { finalSteps.add(this) }

        compactorForMinions requires CollectionStep(
            "Cobblestone",
            2_500
        ).apply { this requires IslandType.HUB.getStep() }

        compactorForMinions requires ItemsStep(
            "" + (7 * amount) + " Enchanted Cobblestone (For Minions)",
            "Enchanted Cobblestone",
            amount * 7 * 160,
            mapOf("Cobblestone" to 1, "Enchanted Cobblestone" to 160)
        )
        compactorForMinions requires ItemsStep(
            "$amount Enchanted Redstone (For Minions)",
            "Enchanted Redstone",
            amount * 160,
            mapOf("Redstone" to 1, "Enchanted Redstone" to 160)
        )
    }

    private fun enchantedCharcoal(amount: Long) {
        val enchantedCharcoalForMinions = ItemsStep(
            "Enchanted Charcoal (for Minions)",
            "Enchanted Charcoal",
            amount,
            mapOf("Enchanted Charcoal" to 1)
        ).apply { finalSteps.add(this) }

        enchantedCharcoalForMinions requires CollectionStep(
            "Coal",
            2_500
        ).apply { this requires IslandType.GOLD_MINES.getStep() }

        enchantedCharcoalForMinions requires ItemsStep(
            "Oak Wood (For Minions)",
            "Oak Wood",
            amount * 32,
            mapOf("Oak Wood" to 1)
        )
        enchantedCharcoalForMinions requires ItemsStep(
            "Coal (For Minions)",
            "Coal",
            amount * 64 * 2,
            mapOf("Coal" to 1)
        )
    }

    private fun ItemsStep.addItemRequirements(): ItemsStep {
        val step = itemPreconditions[itemName]
        if (step != null) {
            requirements.add(step)
        }
        return this
    }
}
