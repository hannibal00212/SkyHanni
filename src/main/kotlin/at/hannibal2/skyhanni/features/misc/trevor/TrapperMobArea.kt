package at.hannibal2.skyhanni.features.misc.trevor

import at.hannibal2.skyhanni.utils.LorenzVec

enum class TrapperMobArea(val location: String, val coordinates: LorenzVec) {
    OASIS("Oasis", LorenzVec(126.0, 77.0, -456.0)),
    GORGE("Mushroom Gorge", LorenzVec(300.0, 80.0, -509.0)),
    OVERGROWN("Overgrown Mushroom Cave", LorenzVec(242.0, 60.0, -389.0)),
    SETTLEMENT("Desert Settlement", LorenzVec(184.0, 86.0, -384.0)),
    GLOWING("Glowing Mushroom Cave", LorenzVec(199.0, 50.0, -512.0)),
    MOUNTAIN("Desert Mountain", LorenzVec(255.0, 148.0, -518.0)),
    FOUND("    ", LorenzVec(0.0, 0.0, 0.0)),
    NONE("   ", LorenzVec(0.0, 0.0, 0.0)),
}
