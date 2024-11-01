package at.hannibal2.skyhanni.features.dungeon

import at.hannibal2.skyhanni.events.DungeonBossPhaseChangeEvent
import at.hannibal2.skyhanni.events.DungeonCompleteEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI.dungeonFloor
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object DungeonBossAPI {
    var bossPhase: DungeonBossPhase? = null

    enum class DungeonBossPhase {
        F6_TERRACOTTA,
        F6_GIANTS,
        F6_SADAN,
        F7_MAXOR,
        F7_STORM,
        F7_GOLDOR_1,
        F7_GOLDOR_2,
        F7_GOLDOR_3,
        F7_GOLDOR_4,
        F7_GOLDOR_5,
        F7_NECRON,
        M7_WITHER_KING,
        ;

        fun isCurrent(): Boolean = bossPhase == this
    }

    private val patternGroup = RepoPattern.group("dungeon.boss.message")

    /**
     * REGEX-TEST: §c[BOSS] Sadan§r§f: So you made it all the way here... Now you wish to defy me\? Sadan\?!
     */
    private val terracottaStartPattern by patternGroup.pattern(
        "f6.terracotta",
        "§c\\[BOSS] Sadan§r§f: So you made it all the way here\\.\\.\\. Now you wish to defy me\\? Sadan\\?!",
    )

    /**
     * REGEX-TEST: §c[BOSS] Sadan§r§f: ENOUGH!
     */
    private val giantsStartPattern by patternGroup.pattern(
        "f6.giants",
        "§c\\[BOSS] Sadan§r§f: ENOUGH!",
    )

    /**
     * REGEX-TEST: §c[BOSS] Sadan§r§f: You did it. I understand now, you have earned my respect.
     */
    private val sadanStartPattern by patternGroup.pattern(
        "f6.sadan",
        "§c\\[BOSS] Sadan§r§f: You did it\\. I understand now, you have earned my respect\\.",
    )

    /**
     * REGEX-TEST: §4[BOSS] Maxor§r§c: §r§cWELL! WELL! WELL! LOOK WHO'S HERE!
     */
    private val maxorStartPattern by patternGroup.pattern(
        "f7.maxor",
        "§4\\[BOSS] Maxor§r§c: §r§cWELL! WELL! WELL! LOOK WHO'S HERE!",
    )

    /**
     * REGEX-TEST: §4[BOSS] Storm§r§c: §r§cPathetic Maxor, just like expected.
     */
    private val stormStartPattern by patternGroup.pattern(
        "f7.storm",
        "§4\\[BOSS] Storm§r§c: §r§cPathetic Maxor, just like expected\\.",
    )

    /**
     * REGEX-TEST: §4[BOSS] Goldor§r§c: §r§cWho dares trespass into my domain?
     */
    private val goldorStartPattern by patternGroup.pattern(
        "f7.goldor.start",
        "§4\\[BOSS] Goldor§r§c: §r§cWho dares trespass into my domain\\?",
    )

    /**
     * REGEX-TEST: §bmartimavocado§r§a activated a lever! (§r§c7§r§a/7)
     * REGEX-TEST: §bmartimavocado§r§a completed a device! (§r§c3§r§a/8)
     * REGEX-TEST: §bmartimavocado§r§a activated a terminal! (§r§c4§r§a/7)
     */
    @Suppress("MaxLineLength")
    val goldorTerminalPattern by patternGroup.pattern(
        "f7.goldor.terminalcomplete",
        "§.(?<playerName>\\w+)§r§a (?:activated|completed) a (?<type>lever|terminal|device)! \\(§r§c(?<currentTerminal>\\d)§r§a/(?<total>\\d)\\)",
    )

    /**
     * REGEX-TEST: §aThe Core entrance is opening!
     */
    private val goldor5StartPattern by patternGroup.pattern(
        "f7.goldor.5",
        "§aThe Core entrance is opening!",
    )

    /**
     * REGEX-TEST: §4[BOSS] Necron§r§c: §r§cYou went further than any human before, congratulations.
     */
    private val necronStartPattern by patternGroup.pattern(
        "f7.necron.start",
        "§4\\[BOSS] Necron§r§c: §r§cYou went further than any human before, congratulations\\.",
    )

    /**
     * REGEX-TEST: §4[BOSS] Necron§r§c: §r§cAll this, for nothing...
     */
    private val witherKingStartPattern by patternGroup.pattern(
        "m7.witherking",
        "§4\\[BOSS] Necron§r§c: §r§cAll this, for nothing\\.\\.\\.",
    )

    private fun handlePhaseMessage(message: String) {
        if (dungeonFloor == "F6" || dungeonFloor == "M6") when { // TODO: move to enum
            terracottaStartPattern.matches(message) -> changePhase(DungeonBossPhase.F6_TERRACOTTA)
            giantsStartPattern.matches(message) -> changePhase(DungeonBossPhase.F6_GIANTS)
            sadanStartPattern.matches(message) -> changePhase(DungeonBossPhase.F6_SADAN)
        }

        if (dungeonFloor == "F7" || dungeonFloor == "M7") { // TODO: move to enum
            goldorTerminalPattern.matchMatcher(message) {
                val currentTerminal = group("currentTerminal").toIntOrNull() ?: return
                val totalTerminals = group("total").toIntOrNull() ?: return
                if (currentTerminal != totalTerminals) return
                changePhase(
                    when (bossPhase) {
                        DungeonBossPhase.F7_GOLDOR_1 -> DungeonBossPhase.F7_GOLDOR_2
                        DungeonBossPhase.F7_GOLDOR_2 -> DungeonBossPhase.F7_GOLDOR_3
                        DungeonBossPhase.F7_GOLDOR_3 -> DungeonBossPhase.F7_GOLDOR_4
                        else -> return
                    },
                )
            }
            when {
                maxorStartPattern.matches(message) -> changePhase(DungeonBossPhase.F7_MAXOR)
                stormStartPattern.matches(message) -> changePhase(DungeonBossPhase.F7_STORM)
                goldorStartPattern.matches(message) -> changePhase(DungeonBossPhase.F7_GOLDOR_1)
                goldor5StartPattern.matches(message) -> changePhase(DungeonBossPhase.F7_GOLDOR_5)
                necronStartPattern.matches(message) -> changePhase(DungeonBossPhase.F7_NECRON)
                witherKingStartPattern.matches(message) -> if (bossPhase != null) changePhase(DungeonBossPhase.M7_WITHER_KING)
            }
        }
    }

    private fun changePhase(newPhase: DungeonBossPhase) {
        DungeonBossPhaseChangeEvent(newPhase).post()
        bossPhase = newPhase
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        bossPhase = null
    }

    @SubscribeEvent
    fun onDungeonEnd(event: DungeonCompleteEvent) {
        bossPhase = null
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        handlePhaseMessage(event.message)
    }
}
