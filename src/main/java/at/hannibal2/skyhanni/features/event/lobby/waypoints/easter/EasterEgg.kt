package at.hannibal2.skyhanni.features.event.lobby.waypoints.easter

import at.hannibal2.skyhanni.utils.SkyHanniVec3d

enum class EasterEgg(val eggName: String, val waypoint: SkyHanniVec3d) {
    EASTER_EGG_1("#1", SkyHanniVec3d(-47, 94, -3)),
    EASTER_EGG_2("#2", SkyHanniVec3d(-20, 86, -70)),
    EASTER_EGG_3("#3", SkyHanniVec3d(24, 62, -38)),
    EASTER_EGG_4("#4", SkyHanniVec3d(-38, 56, 195)),
    EASTER_EGG_5("#5", SkyHanniVec3d(-67, 82, 98)),
    EASTER_EGG_6("#6", SkyHanniVec3d(-91, 61, 140)),
    EASTER_EGG_7("#7", SkyHanniVec3d(103, 56, 194)),
    EASTER_EGG_8("#8", SkyHanniVec3d(81, 68, 108)),
    EASTER_EGG_9("#9", SkyHanniVec3d(10, 65, 58)),
    EASTER_EGG_10("#10", SkyHanniVec3d(9, 53, 249)),
    EASTER_EGG_11("#11", SkyHanniVec3d(216, 51, 93)),
    EASTER_EGG_12("#12", SkyHanniVec3d(113, 45, 161)),
    EASTER_EGG_13("#13", SkyHanniVec3d(133, 51, -8)),
    EASTER_EGG_14("#14", SkyHanniVec3d(141, 73, 3)),
    EASTER_EGG_15("#15", SkyHanniVec3d(107, 68, -9)),
    EASTER_EGG_16("#16", SkyHanniVec3d(167, 60, -42)),
    EASTER_EGG_17("#17", SkyHanniVec3d(58, 65, -2)),
    EASTER_EGG_18("#18", SkyHanniVec3d(118, 51, -85)), // 158, 68, -81 entrance
    EASTER_EGG_19("#19", SkyHanniVec3d(197, 60, 17)),
    EASTER_EGG_20("#20", SkyHanniVec3d(-136, 85, -16)),
    EASTER_EGG_21("#21", SkyHanniVec3d(-161, 57, -97)),
    EASTER_EGG_22("#22", SkyHanniVec3d(-138, 74, -133)),
    EASTER_EGG_23("#23", SkyHanniVec3d(-5, 77, -176)),
    EASTER_EGG_24("#24", SkyHanniVec3d(67, 60, -170)),
    EASTER_EGG_25("#25", SkyHanniVec3d(-177, 58, 70)),
    EASTER_EGG_26("#26", SkyHanniVec3d(-141, 102, -15)),
    EASTER_EGG_27("#27", SkyHanniVec3d(9, 32, 3)), // 11, 62, 0 entrance
    EASTER_EGG_28("#28", SkyHanniVec3d(150, 28, 19)),
    EASTER_EGG_29("#29", SkyHanniVec3d(47, 37, 52)),
    EASTER_EGG_30("#30 (get your code first!)", SkyHanniVec3d(-28, 11, 123)),
    ;

    var found = false
}
