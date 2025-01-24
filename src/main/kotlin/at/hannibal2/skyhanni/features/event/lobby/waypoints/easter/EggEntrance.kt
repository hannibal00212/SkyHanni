package at.hannibal2.skyhanni.features.event.lobby.waypoints.easter

import at.hannibal2.skyhanni.utils.LorenzVec

enum class EggEntrance(
    val eggEntranceName: String,
    val waypoint: LorenzVec,
    vararg val easterEgg: EasterEgg,
) {

    EASTER_EGG_ENTER_12_13_1_28_1(
        "#12, #13, #28 (tunnel)",
        LorenzVec(126, 52, 175),
        EasterEgg.EASTER_EGG_12,
        EasterEgg.EASTER_EGG_13,
        EasterEgg.EASTER_EGG_28
    ),
    EASTER_EGG_ENTER_13_2("#13 (keep going, stay on your left [<--])", LorenzVec(144, 30, 19), EasterEgg.EASTER_EGG_13),
    EASTER_EGG_ENTER_13_3("#13 (enter here)", LorenzVec(107, 42, -47), EasterEgg.EASTER_EGG_13),
    EASTER_EGG_ENTER_13_4(
        "#13 (straight ahead, don't take the stairs)",
        LorenzVec(105, 49, -34),
        EasterEgg.EASTER_EGG_13
    ),
    EASTER_EGG_ENTER_14_1("#14 (enter here)", LorenzVec(159, 68, -25), EasterEgg.EASTER_EGG_14),
    EASTER_EGG_ENTER_14_2("#14 (right turn [-->] here)", LorenzVec(153, 68, -8), EasterEgg.EASTER_EGG_14),
    EASTER_EGG_ENTER_14_3("#14 (left turn [<--] here)", LorenzVec(143, 68, -8), EasterEgg.EASTER_EGG_14),
    EASTER_EGG_ENTER_18_1("#18 (down this well)", LorenzVec(158, 67, -82), EasterEgg.EASTER_EGG_18),
    EASTER_EGG_ENTER_18_2("#18 (through this painting)", LorenzVec(142, 56, -82), EasterEgg.EASTER_EGG_18),
    EASTER_EGG_ENTER_21("#21 (through the waterfall)", LorenzVec(-137, 64, -82), EasterEgg.EASTER_EGG_21),
    EASTER_EGG_ENTER_28_2("#13, #28 (right turn [-->] here)", LorenzVec(94, 35, 145), EasterEgg.EASTER_EGG_28),
    EASTER_EGG_ENTER_28_3(
        "#13, #28 (straight ahead, stay on your right [-->])",
        LorenzVec(92, 21, 107),
        EasterEgg.EASTER_EGG_28
    ),
    EASTER_EGG_ENTER_29_2(
        "#29 (straight ahead, stay on your right [-->])",
        LorenzVec(16, 32, 2),
        EasterEgg.EASTER_EGG_29
    ),
    EASTER_EGG_ENTER_29_3("#29 (enter here)", LorenzVec(54, 22, 60), EasterEgg.EASTER_EGG_29),
    EASTER_EGG_ENTER_29_4("#29 (up stairs, make left turn [<--])", LorenzVec(25, 25, 51), EasterEgg.EASTER_EGG_29),
    EASTER_EGG_ENTER_29_5(
        "#29 (up stairs, make left turn [<--] again)",
        LorenzVec(17, 32, 66),
        EasterEgg.EASTER_EGG_29
    ),
    EASTER_EGG_ENTER_29_6("#29 (left turn [<--] here)", LorenzVec(35, 37, 66), EasterEgg.EASTER_EGG_29),
    EASTER_EGG_ENTER_29_7("#29 (past this door)", LorenzVec(39, 37, 48), EasterEgg.EASTER_EGG_29),
    EASTER_EGG_ENTER_30_1("#30 (1st digit, SNEAK + RCLICK)", LorenzVec(143, 65, -30), EasterEgg.EASTER_EGG_30),
    EASTER_EGG_ENTER_30_2("#30 (3rd digit, open chest)", LorenzVec(205, 34, -157), EasterEgg.EASTER_EGG_30),
    EASTER_EGG_ENTER_27_29_1_30_3(
        "#27, #29, #30 (inside this well)",
        LorenzVec(10, 63, 0),
        EasterEgg.EASTER_EGG_27,
        EasterEgg.EASTER_EGG_29,
        EasterEgg.EASTER_EGG_30
    ),
    EASTER_EGG_ENTER_30_4("#30 (left turn [<--] here)", LorenzVec(-28, 42, 14), EasterEgg.EASTER_EGG_30),
    EASTER_EGG_ENTER_30_5("#30 Vault (brute force 2nd digit)", LorenzVec(-35, 25, 63), EasterEgg.EASTER_EGG_30),
}

