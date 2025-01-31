package at.hannibal2.skyhanni.features.misc.trevor

import at.hannibal2.skyhanni.utils.SkyHanniVec3d

enum class TrapperMobArea(val location: String, val coordinates: SkyHanniVec3d) {
    OASIS("Oasis", SkyHanniVec3d(126.0, 77.0, -456.0)),
    GORGE("Mushroom Gorge", SkyHanniVec3d(300.0, 80.0, -509.0)),
    OVERGROWN("Overgrown Mushroom Cave", SkyHanniVec3d(242.0, 60.0, -389.0)),
    SETTLEMENT("Desert Settlement", SkyHanniVec3d(184.0, 86.0, -384.0)),
    GLOWING("Glowing Mushroom Cave", SkyHanniVec3d(199.0, 50.0, -512.0)),
    MOUNTAIN("Desert Mountain", SkyHanniVec3d(255.0, 148.0, -518.0)),
    FOUND("    ", SkyHanniVec3d(0.0, 0.0, 0.0)),
    NONE("   ", SkyHanniVec3d(0.0, 0.0, 0.0)),
}
