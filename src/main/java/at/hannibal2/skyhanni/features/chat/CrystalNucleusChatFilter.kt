package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.chat.CrystalNucleusConfig.CrystalNucleusMessageTypes
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CrystalNucleusChatFilter {

    class NucleusChatFilterRes(private var blockMessage: String? = null, private var newMessage: String? = null) {
        fun getPair(): Pair<String?, String?> {
            return Pair(blockMessage, newMessage)
        }
    }

    private val config get() = SkyHanniMod.feature.chat.filterType.crystalNucleus
    private val patternGroup = RepoPattern.group("filter.crystalnucleus")

    private var unclosedRunCompleted = false
    private var unclosedCrystalCollected = false
    private var crystalCount = 0
    private var crystalCollected = ""
    private var lastKeeper = ""
    private var inCompListPreamble = false

    /**
     * REGEX-TEST: §3§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
     */
    private val runCompletedWrapperPattern by patternGroup.pattern(
        "run.completed",
        "§3§l▬{64}",
    )

    /**
     * REGEX-TEST: §5§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
     */
    private val crystalCollectedWrapperPattern by patternGroup.pattern(
        "crystal.collected.wrapper",
        "§5§l▬{64}",
    )

    /**
     * REGEX-TEST: §f                       §r§5§l✦ CRYSTAL FOUND §r§7(1§r§7/5§r§7)
     */
    private val crystalCollectedCountPattern by patternGroup.pattern(
        "crystal.collected.count",
        "§f *§r§5§l✦ CRYSTAL FOUND §r§7\\((?<count>\\d)§r§7/5§r§7\\)",
    )

    /**
     * REGEX-TEST: §f                              §r§5Amethyst Crystal
     * REGEX-TEST: §f                              §r§bSapphire Crystal
     * REGEX-TEST: §f                                §r§6Amber Crystal
     * REGEX-TEST: §f                                §r§eTopaz Crystal
     * REGEX-TEST: §f                                §r§aJade Crystal
     */
    private val crystalCollectedIdentifierPattern by patternGroup.pattern(
        "crystal.collected.id",
        "§f *§r(?<crystal>.* Crystal) *",
    )

    /**
     * REGEX-TEST: §5§l✦ §r§dYou placed the §r§bSapphire Crystal§r§d!
     */
    private val crystalPlacedPattern by patternGroup.pattern(
        "crystal.placed",
        "§5§l✦ §r§dYou placed the §r(?<crystal>.* Crystal)§r§d!",
    )

    /**
     * REGEX-TEST: §aYou found §r§cScavenged Diamond Axe §r§awith your §r§cMetal Detector§r§a!
     * REGEX-TEST: §aYou found §r§cScavenged Emerald Hammer §r§awith your §r§cMetal Detector§r§a!
     * REGEX-TEST: §aYou found §r§a☘ Flawed Jade Gemstone §r§8x2 §r§awith your §r§cMetal Detector§r§a!
     */
    private val scavengeLootPattern by patternGroup.pattern(
        "divan.scavenge",
        "§aYou found §r(?<loot>.*) §r§awith your §r§cMetal Detector§r§a!",
    )

    /**
     * REGEX-TEST: §6§lPICK IT UP!
     */
    private val scavengeSecondaryPattern by patternGroup.pattern(
        "divan.scavenge.secondary",
        "§6§lPICK IT UP!",
    )

    /**
     * REGEX-TEST: §e[NPC] §6Keeper of Gold§f: §rExcellent! You have returned the §cScavenged Golden Hammer §rto its rightful place!
     * REGEX-TEST: §e[NPC] §6Keeper of Diamond§f: §rExcellent! You have returned the §cScavenged Diamond Axe §rto its rightful place!
     * REGEX-TEST: §e[NPC] §6Keeper of Emerald§f: §rExcellent! You have returned the §cScavenged Emerald Hammer §rto its rightful place!
     * REGEX-TEST: §e[NPC] §6Keeper of Lapis§f: §rYou found all of the items! Behold... the §aJade Crystal§r!
     */
    private val genericKeeperMessage by patternGroup.pattern(
        "npc.keeper",
        "§e\\[NPC\\] §6Keeper of (?<keepertype>.*)§f: §r(?<message>.*)",
    )

    /**
     * REGEX-TEST: Thanks for bringing me the §9Synthetic Heart§r! Bring me 5 more components to fix the giant!
     * REGEX-TEST: Thanks for bringing me the §9Robotron Reflector§r! Bring me 5 more components to fix the giant!
     * REGEX-TEST: Thanks for bringing me the §9Superlite Motor§r! Bring me 4 more components to fix the giant!
     * REGEX-TEST: Thanks for bringing me the §9Synthetic Heart§r! Bring me 3 more components to fix the giant!
     * REGEX-TEST: Thanks for bringing me the §9FTX 3070§r! Bring me 2 more components to fix the giant!
     * REGEX-TEST: Thanks for bringing me the §9Electron Transmitter§r! Bring me one more component to fix the giant!
     * REGEX-TEST: §rYou've brought me all of the components!
     * REGEX-TEST: §rYou've brought me all of the components... I think? To be honest, I kind of lost count...
     * REGEX-TEST: Wait a minute. This will work just fine.
     */
    @Suppress("MaxLineLength")
    private val componentSubmittedPattern by patternGroup.pattern(
        "precursor.submitted",
        "(?:Wait a minute. This will work just fine.|You've brought me all|me the (?<component>.*)§r! Bring me (?<remaining>\\d|one) more).*",
    )

    /**
     * REGEX-TEST: §rThat's not one of the components I need! Bring me one of the missing components:
     */
    private val componentListPreamblePattern by patternGroup.pattern(
        "component.list.preamble",
        "§rThat's not one of the components I need! Bring me one of the missing components:",
    )

    /**
     * REGEX-TEST:   §r§9FTX 3070
     * REGEX-TEST:   §r§9Electron Transmitter
     * REGEX-TEST:   §r§9Superlite Motor
     * REGEX-TEST:   §r§9Synthetic Heart
     * REGEX-TEST:   §r§9Control Switch
     * REGEX-TEST:   §r§9Robotron Reflector
     */
    private val componentListPattern by patternGroup.pattern(
        "component.list",
        " {2}§r§9(?<component>.*)",
    )

    /**
     * REGEX-TEST: §8§oWhew! That was a close one, better get out of here...
     * REGEX-TEST: §cThe Goblin King's §r§afoul stench §r§chas dissipated!
     */
    private val goblinGuardExitMessagePattern by patternGroup.pattern(
        "goblin.guard.exit",
        "§8§oWhew! That was a close one, better get out of here\\.{3}|§cThe Goblin King's §r§afoul stench §r§chas dissipated!",
    )


    fun block(message: String): NucleusChatFilterRes? {
        if (!isEnabled()) return null

        return blockCrystalCollected(message)
            ?: blockCrystalPlaced(message)
            ?: blockRunCompleted(message)
            ?: blockNonToolScavenge(message)
            ?: blockGoblinGuards(message)
            ?: blockNPC(message)
    }

    private fun blockCrystalCollected(message: String): NucleusChatFilterRes? {
        if (!shouldBlock(CrystalNucleusMessageTypes.CRYSTAL_COLLECTED)) return null
        if (crystalCollectedWrapperPattern.matches(message)) {
            unclosedCrystalCollected = !unclosedCrystalCollected
            return NucleusChatFilterRes("crystal_collected")
        }

        if (!unclosedCrystalCollected) return null

        crystalCollectedCountPattern.matchMatcher(message) {
            crystalCount = group("count").toInt()
        }

        crystalCollectedIdentifierPattern.matchMatcher(message) {
            crystalCollected = group("crystal")
            return NucleusChatFilterRes("", "§5§l✦ $crystalCollected §5found§d! §7(§a$crystalCount§7/§a5§7)")
        }

        return NucleusChatFilterRes("crystal_collected")
    }

    private fun blockCrystalPlaced(message: String): NucleusChatFilterRes? {
        if (!inNucleus()) return null
        if (!shouldBlock(CrystalNucleusMessageTypes.CRYSTAL_PLACED)) return null

        if (message == "  §r§dKeep exploring the §r§5Crystal Hollows §r§dto find the rest!") return NucleusChatFilterRes("crystal_placed")
        crystalPlacedPattern.matchMatcher(message) {
            return NucleusChatFilterRes("", "§5§l✦ ${group("crystal")} §5placed§d!")
        }
        return null
    }

    private fun blockRunCompleted(message: String): NucleusChatFilterRes? {
        if (!inNucleus()) return null
        if (!shouldBlock(CrystalNucleusMessageTypes.RUN_COMPLETED)) return null

        if (runCompletedWrapperPattern.matches(message)) {
            unclosedRunCompleted = !unclosedRunCompleted
            return NucleusChatFilterRes("run_completed")
        }

        if (message == "§7Pick it up near the §r§5Nucleus Vault§r§7!") return NucleusChatFilterRes("", "§5Crystal Nucleus Run complete§d!")
        if (!unclosedRunCompleted) return null

        return NucleusChatFilterRes("run_completed")
    }

    private fun blockNonToolScavenge(message: String): NucleusChatFilterRes? {
        if (!shouldBlock(CrystalNucleusMessageTypes.NON_TOOL_SCAVENGE)) return null

        scavengeLootPattern.matchMatcher(message) {
            if (!group("loot").startsWith("§cScavenged")) return NucleusChatFilterRes("non_tool_scavenge")
        }
        if (scavengeSecondaryPattern.matches(message)) return NucleusChatFilterRes("non_tool_scavenge")

        return null
    }

    private fun blockGoblinGuards(message: String): NucleusChatFilterRes? {
        if (!shouldBlock(CrystalNucleusMessageTypes.NPC_GOBLIN_GUARDS)) return null
        if (goblinGuardExitMessagePattern.matches(message)) {
            return NucleusChatFilterRes("npc_goblin_guard")
        }

        // §c[GUARD] Ooblak§r§f: §r§eTHEY'RE STEALING THE CRYSTAL! GET THEM!
        if (!message.startsWith("§c[GUARD]")) return null

        return NucleusChatFilterRes("npc_goblin_guard")
    }

    private fun blockNPC(message: String): NucleusChatFilterRes? {
        if (!message.startsWith("§e[NPC]")) return null

        return blockProfessorRobot(message)
            ?: blockKingYolkar(message)
            ?: blockKeepers(message)
    }

    private fun blockProfessorRobot(message: String): NucleusChatFilterRes? {
        if (!shouldBlock(CrystalNucleusMessageTypes.NPC_PROF_ROBOT)) return null
        if (inCompListPreamble && componentListPattern.matches(message)) {
            return NucleusChatFilterRes("npc_prof_robot")
        }
        if (!message.startsWith("§e[NPC] Professor Robot")) return null

        if (componentListPreamblePattern.matches(message)) {
            inCompListPreamble = true
            DelayedRun.runDelayed(2.seconds) {
                inCompListPreamble = false
            }
            return NucleusChatFilterRes("npc_prof_robot")
        }

        componentSubmittedPattern.findMatcher(message) {
            if (message.contains("brought me all") || message.contains("This will work just fine.")) {
                return NucleusChatFilterRes("", "§e[NPC] Professor Robot§f: §rAll components submitted.")
            }

            return NucleusChatFilterRes(
                "",
                "§e[NPC] Professor Robot§f: ${group("component")} submitted. ${group("remaining")} components left.",
            )
        }

        return NucleusChatFilterRes("npc_prof_robot")
    }

    private fun blockKingYolkar(message: String): NucleusChatFilterRes? {
        if (!shouldBlock(CrystalNucleusMessageTypes.NPC_KING_YOLKAR)) return null
        if (!message.startsWith("§e[NPC] §6King Yolkar")) return null

        return when {
            // §e[NPC] §6King Yolkar§f: §r*rumble* *rumble*
            message.contains("*rumble* *rumble*") ->
                NucleusChatFilterRes("", "§e[NPC] §6King Yolkar§f: ...")

            // §e[NPC] §6King Yolkar§f: §rBring me back a §9Goblin Egg §rof any type and we can teach her a lesson!
            message.contains("Bring me back a §9Goblin Egg") ->
                NucleusChatFilterRes("", "§e[NPC] §6King Yolkar§f: §rBring me a §9Goblin Egg §rof any type.")

            // §e[NPC] §6King Yolkar§f: §rThis egg will help me stomach my pain.
            message.contains("This egg will help me stomach my pain.") ->
                NucleusChatFilterRes("", "§e[NPC] §6King Yolkar§f: §2King's Scent§r applied.")

            else -> NucleusChatFilterRes("npc_king_yolkar")
        }
    }

    private fun blockKeepers(message: String): NucleusChatFilterRes? {
        if (!shouldBlock(CrystalNucleusMessageTypes.NPC_DIVAN_KEEPERS)) return null
        if (!message.startsWith("§e[NPC] §6Keeper of ")) return null

        genericKeeperMessage.matchMatcher(message) {
            lastKeeper = group("keepertype")
        }

        if (message.contains("You found all of the items!")) {
            return NucleusChatFilterRes("", "§e[NPC] §6Keeper of §6$lastKeeper§f: §rAll tools submitted.")
        }

        return NucleusChatFilterRes("npc_divan_keeper")
    }

    private fun shouldBlock(type: CrystalNucleusMessageTypes) = config.modifiedMessages.contains(type)
    private fun inNucleus() = LorenzUtils.skyBlockArea == "Crystal Nucleus"
    private fun isEnabled() = config.enabled && IslandType.CRYSTAL_HOLLOWS.isInIsland()
}
