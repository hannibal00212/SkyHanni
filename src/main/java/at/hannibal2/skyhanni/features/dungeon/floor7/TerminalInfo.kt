package at.hannibal2.skyhanni.features.dungeon.floor7

import at.hannibal2.skyhanni.features.dungeon.DungeonBossApi
import at.hannibal2.skyhanni.utils.SkyHanniVec3d

private typealias BossPhase = DungeonBossApi.DungeonBossPhase

enum class TerminalInfo(val location: SkyHanniVec3d, val phase: BossPhase, val text: String) {
    P1_TERMINAL_1(SkyHanniVec3d(111, 113, 73), BossPhase.F7_GOLDOR_1, "Terminal"),
    P1_TERMINAL_2(SkyHanniVec3d(111, 119, 79), BossPhase.F7_GOLDOR_1, "Terminal"),
    P1_TERMINAL_3(SkyHanniVec3d(89, 112, 92), BossPhase.F7_GOLDOR_1, "Terminal"),
    P1_TERMINAL_4(SkyHanniVec3d(89, 122, 101), BossPhase.F7_GOLDOR_1, "Terminal"),
    P1_LEVER_1(SkyHanniVec3d(106, 124, 113), BossPhase.F7_GOLDOR_1, "Lever"),
    P1_LEVER_2(SkyHanniVec3d(94, 124, 113), BossPhase.F7_GOLDOR_1, "Lever"),
    P1_DEVICE(SkyHanniVec3d(110, 119, 93), BossPhase.F7_GOLDOR_1, "Device"),

    P2_TERMINAL_1(SkyHanniVec3d(68, 109, 121), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_TERMINAL_2(SkyHanniVec3d(59, 120, 122), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_TERMINAL_3(SkyHanniVec3d(47, 109, 121), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_TERMINAL_4(SkyHanniVec3d(40, 124, 122), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_TERMINAL_5(SkyHanniVec3d(39, 108, 143), BossPhase.F7_GOLDOR_2, "Terminal"),
    P2_LEVER_1(SkyHanniVec3d(23, 132, 138), BossPhase.F7_GOLDOR_2, "Lever"),
    P2_LEVER_2(SkyHanniVec3d(27, 124, 127), BossPhase.F7_GOLDOR_2, "Lever"),
    P2_DEVICE(SkyHanniVec3d(60, 131, 142), BossPhase.F7_GOLDOR_2, "Device"),

    P3_TERMINAL_1(SkyHanniVec3d(-3, 109, 112), BossPhase.F7_GOLDOR_3, "Terminal"),
    P3_TERMINAL_2(SkyHanniVec3d(-3, 119, 93), BossPhase.F7_GOLDOR_3, "Terminal"),
    P3_TERMINAL_3(SkyHanniVec3d(19, 123, 93), BossPhase.F7_GOLDOR_3, "Terminal"),
    P3_TERMINAL_4(SkyHanniVec3d(-3, 109, 77), BossPhase.F7_GOLDOR_3, "Terminal"),
    P3_LEVER_1(SkyHanniVec3d(14, 122, 55), BossPhase.F7_GOLDOR_3, "Lever"),
    P3_LEVER_2(SkyHanniVec3d(2, 122, 55), BossPhase.F7_GOLDOR_3, "Lever"),
    P3_DEVICE(SkyHanniVec3d(-2, 119, 77), BossPhase.F7_GOLDOR_3, "Device"),

    P4_TERMINAL_1(SkyHanniVec3d(41, 109, 29), BossPhase.F7_GOLDOR_4, "Terminal"),
    P4_TERMINAL_2(SkyHanniVec3d(44, 121, 29), BossPhase.F7_GOLDOR_4, "Terminal"),
    P4_TERMINAL_3(SkyHanniVec3d(67, 109, 29), BossPhase.F7_GOLDOR_4, "Terminal"),
    P4_TERMINAL_4(SkyHanniVec3d(72, 115, 48), BossPhase.F7_GOLDOR_4, "Terminal"),
    P4_LEVER_1(SkyHanniVec3d(84, 121, 34), BossPhase.F7_GOLDOR_4, "Lever"),
    P4_LEVER_2(SkyHanniVec3d(86, 128, 46), BossPhase.F7_GOLDOR_4, "Lever"),
    P4_DEVICE(SkyHanniVec3d(63, 126, 35), BossPhase.F7_GOLDOR_4, "Device"),
    ;

    var highlight: Boolean = true

    companion object {
        fun resetTerminals() = entries.forEach { it.highlight = true }

        fun getClosestTerminal(input: SkyHanniVec3d): TerminalInfo? {
            return entries.filter { it.highlight }.minByOrNull { it.location.distance(input) }
        }
    }
}
