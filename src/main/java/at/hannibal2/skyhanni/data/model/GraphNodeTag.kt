package at.hannibal2.skyhanni.data.model

import at.hannibal2.skyhanni.utils.LorenzColor

enum class GraphNodeTag(val internalName: String?, val color: LorenzColor, val cleanName: String, val description: String) {
    DEV("dev", LorenzColor.WHITE, "Dev", "Intentionally marked as dev."), // E.g. Spawn points, todos, etc

    // Everywhere
    NPC("npc", LorenzColor.YELLOW, "NPC", "A NPC entity."), // also take from neu repo
    AREA("area", LorenzColor.DARK_GREEN, "Area", "A big SkyBlock area."),
    SMALL_AREA("small_area", LorenzColor.GREEN, "Small Area", "A small SkyBlock area, e.g. a house."),
    POI("poi", LorenzColor.WHITE, "PoI", "Point of interest."),
    LAUNCH_PAD("launch", LorenzColor.WHITE, "Launch Pad", "Slime blocks sending you to another server."),

    // on multiple islands
    ROMEO("romeo", LorenzColor.WHITE, "Romeo & Juliette Quest", "Blocks related to the Romeo and Juliette/Ring of Love quest line."),
    RACE("race", LorenzColor.WHITE, "Race Start/Stop", "A race start or stop point."),
    SLAYER("slayer", LorenzColor.WHITE, "Slayer", "A Slayer area"),
    // hoppity

    // Hub
    HUB_12_STARTER("starter_npc", LorenzColor.WHITE, "Starter NPC", "One of the 12 starter NPC's you need to talk to."),
    // diana

    // Farming Islands: Pelts
    FARMING_CROP("farming_crop", LorenzColor.WHITE, "Farming Crop", "A spot where you can break crops on farming islands."),

    // Rift
    RIFT_ENIGMA("rift_enigma", LorenzColor.DARK_PURPLE, "Enigma Soul", "Enigma Souls in the rift."),
    RIFT_EYE("rift_eye", LorenzColor.DARK_RED, "Eye", "An Eye in the rift to teleport to."),

    // Spider's Den
    SPIDER_RELIC("SPIDER_RELIC", LorenzColor.DARK_PURPLE, "Relic", "An relic in the Spider's Den."),

    // Dwarven Mines
    MINES_EMISSARY("mines_emissary", LorenzColor.GOLD, "Emissary", "A Emissary from the king."),
    // commission areas

    ;

    val displayName: String = color.getChatColor() + cleanName

    companion object {
        fun byId(internalName: String?): GraphNodeTag? = values().firstOrNull { it.internalName == internalName }
    }
}
