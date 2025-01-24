package at.hannibal2.skyhanni.features.dungeon.floor7

import at.hannibal2.skyhanni.features.dungeon.DungeonBossAPI
import at.hannibal2.skyhanni.utils.LorenzVec

private typealias BossPhase = DungeonBossAPI.DungeonBossPhase

enum class TerminalInfo(val location: LorenzVec, val phase: BossPhase, val text: String) {
    P1_TERMINAL_1(LorenzVec(111, 113, 73), BossPhase.F7_GOLDOR_1, "Terminal"),
    P1_TERMINAL_2(LorenzVec(111, 119, 79), BossPhase.F7_GOLDOR_1, "Terminal"),
    P1_TERMINAL_3(LorenzVec(89, 112, 92), BossPhase.F7_GOLDOR_1, "Terminal"),
    P1_TERMINAL_4(LorenzVec(89, 122, 101), BossPhase.F7_GOLDOR_1, "Terminal"),
    P1_LEVER_1(LorenzVec(106, 124, 113), BossPhase.F7_GOLDOR_1, "Lever"),
    P1_LEVER_2(LorenzVec(94, 124, 113), BossPhase.F7_GOLDOR_1, "Lever"),
    P1_DEVICE(LorenzVec(110, 119, 93), BossPhase.F7_GOLDOR_1, "Device"),

    P2_TERMINAL_1(LorenzVec(68, 109, 121), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_TERMINAL_2(LorenzVec(59, 120, 122), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_TERMINAL_3(LorenzVec(47, 109, 121), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_TERMINAL_4(LorenzVec(40, 124, 122), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_TERMINAL_5(LorenzVec(39, 108, 143), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_LEVER_1(LorenzVec(23, 132, 138), BossPhase.F7_GOLDOR_2, "Lever"),
    P2_LEVER_2(LorenzVec(27, 124, 127), BossPhase.F7_GOLDOR_2, "Lever"),
    P2_DEVICE(LorenzVec(60, 131, 142), BossPhase.F7_GOLDOR_2, "Device"),

    P3_TERMINAL_1(LorenzVec(-3, 109, 112), BossPhase.F7_GOLDOR_3, "Terminal"),
    P3_TERMINAL_2(LorenzVec(-3, 119, 93), BossPhase.F7_GOLDOR_3, "Terminal"),
    P3_TERMINAL_3(LorenzVec(19, 123, 93), BossPhase.F7_GOLDOR_3, "Terminal"),
    P3_TERMINAL_4(LorenzVec(-3, 109, 77), BossPhase.F7_GOLDOR_3, "Terminal"),
    P3_LEVER_1(LorenzVec(14, 122, 55), BossPhase.F7_GOLDOR_3, "Lever"),
    P3_LEVER_2(LorenzVec(2, 122, 55), BossPhase.F7_GOLDOR_3, "Lever"),
    P3_DEVICE(LorenzVec(-2, 119, 77), BossPhase.F7_GOLDOR_3, "Device"),

    P4_TERMINAL_1(LorenzVec(41, 109, 29), BossPhase.F7_GOLDOR_4, "Terminal"),
    P4_TERMINAL_2(LorenzVec(44, 121, 29), BossPhase.F7_GOLDOR_4, "Terminal"),
    P4_TERMINAL_3(LorenzVec(67, 109, 29), BossPhase.F7_GOLDOR_4, "Terminal"),
    P4_TERMINAL_4(LorenzVec(72, 115, 48), BossPhase.F7_GOLDOR_4, "Terminal"),
    P4_LEVER_1(LorenzVec(84, 121, 34), BossPhase.F7_GOLDOR_4, "Lever"),
    P4_LEVER_2(LorenzVec(86, 128, 46), BossPhase.F7_GOLDOR_4, "Lever"),
    P4_DEVICE(LorenzVec(63, 126, 35), BossPhase.F7_GOLDOR_4, "Device"),
    ;

    var highlight: Boolean = true

    companion object {
        fun resetTerminals() = entries.forEach { it.highlight = true }

        fun getClosestTerminal(input: LorenzVec): TerminalInfo? {
            return entries.filter { it.highlight }.minByOrNull { it.location.distance(input) }
        }
    }
}
