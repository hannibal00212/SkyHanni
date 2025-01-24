package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.utils.LorenzColor

enum class BurrowType(val text: String, val color: LorenzColor) {
    START("§aStart", LorenzColor.GREEN),
    MOB("§cMob", LorenzColor.RED),
    TREASURE("§6Treasure", LorenzColor.GOLD),
    UNKNOWN("§fUnknown?!", LorenzColor.WHITE),
}
