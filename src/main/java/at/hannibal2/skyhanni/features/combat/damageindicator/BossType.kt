package at.hannibal2.skyhanni.features.combat.damageindicator

import at.hannibal2.skyhanni.config.features.combat.damageindicator.DamageIndicatorConfig.BossCategory

typealias Type = BossCategory

enum class BossType(
    val fullName: String,
    val bossTypeToggle: Type,
    val shortName: String = fullName,
    val showDeathTime: Boolean = false,
) {

    // Nether Mini Bosses
    NETHER_BLADESOUL("§8Bladesoul", Type.NETHER_MINI_BOSSES),
    NETHER_MAGMA_BOSS("§4Magma Boss", Type.NETHER_MINI_BOSSES),
    NETHER_ASHFANG("§cAshfang", Type.NETHER_MINI_BOSSES),
    NETHER_BARBARIAN_DUKE("§eBarbarian Duke", Type.NETHER_MINI_BOSSES),
    NETHER_MAGE_OUTLAW("§5Mage Outlaw", Type.NETHER_MINI_BOSSES),

    NETHER_VANQUISHER("§5Vanquisher", Type.VANQUISHER),

    END_ENDSTONE_PROTECTOR("§cEndstone Protector", Type.ENDERSTONE_PROTECTOR),
    END_ENDER_DRAGON("Ender Dragon", Type.ENDER_DRAGON), // TODO fix totally

    SLAYER_ZOMBIE_1("§aRevenant Horror 1", Type.REVENANT_HORROR, "§aRev 1", showDeathTime = true),
    SLAYER_ZOMBIE_2("§eRevenant Horror 2", Type.REVENANT_HORROR, "§eRev 2", showDeathTime = true),
    SLAYER_ZOMBIE_3("§cRevenant Horror 3", Type.REVENANT_HORROR, "§cRev 3", showDeathTime = true),
    SLAYER_ZOMBIE_4("§4Revenant Horror 4", Type.REVENANT_HORROR, "§4Rev 4", showDeathTime = true),
    SLAYER_ZOMBIE_5("§5Revenant Horror 5", Type.REVENANT_HORROR, "§5Rev 5", showDeathTime = true),

    SLAYER_SPIDER_1("§aTarantula Broodfather 1", Type.TARANTULA_BROODFATHER, "§aTara 1", showDeathTime = true),
    SLAYER_SPIDER_2("§eTarantula Broodfather 2", Type.TARANTULA_BROODFATHER, "§eTara 2", showDeathTime = true),
    SLAYER_SPIDER_3("§cTarantula Broodfather 3", Type.TARANTULA_BROODFATHER, "§cTara 3", showDeathTime = true),
    SLAYER_SPIDER_4("§4Tarantula Broodfather 4", Type.TARANTULA_BROODFATHER, "§4Tara 4", showDeathTime = true),

    SLAYER_WOLF_1("§aSven Packmaster 1", Type.SVEN_PACKMASTER, "§aSven 1", showDeathTime = true),
    SLAYER_WOLF_2("§eSven Packmaster 2", Type.SVEN_PACKMASTER, "§eSven 2", showDeathTime = true),
    SLAYER_WOLF_3("§cSven Packmaster 3", Type.SVEN_PACKMASTER, "§cSven 3", showDeathTime = true),
    SLAYER_WOLF_4("§4Sven Packmaster 4", Type.SVEN_PACKMASTER, "§4Sven 4", showDeathTime = true),

    SLAYER_ENDERMAN_1("§aVoidgloom Seraph 1", Type.VOIDGLOOM_SERAPH, "§aVoid 1", showDeathTime = true),
    SLAYER_ENDERMAN_2("§eVoidgloom Seraph 2", Type.VOIDGLOOM_SERAPH, "§eVoid 2", showDeathTime = true),
    SLAYER_ENDERMAN_3("§cVoidgloom Seraph 3", Type.VOIDGLOOM_SERAPH, "§cVoid 3", showDeathTime = true),
    SLAYER_ENDERMAN_4("§4Voidgloom Seraph 4", Type.VOIDGLOOM_SERAPH, "§4Void 4", showDeathTime = true),

    SLAYER_BLAZE_1("§aInferno Demonlord 1", Type.INFERNO_DEMONLORD, "§aInferno 1", showDeathTime = true),
    SLAYER_BLAZE_2("§aInferno Demonlord 2", Type.INFERNO_DEMONLORD, "§aInferno 2", showDeathTime = true),
    SLAYER_BLAZE_3("§aInferno Demonlord 3", Type.INFERNO_DEMONLORD, "§aInferno 3", showDeathTime = true),
    SLAYER_BLAZE_4("§aInferno Demonlord 4", Type.INFERNO_DEMONLORD, "§aInferno 4", showDeathTime = true),

    SLAYER_BLAZE_TYPHOEUS_1("§aInferno Typhoeus 1", Type.INFERNO_DEMONLORD, "§aTyphoeus 1"),
    SLAYER_BLAZE_TYPHOEUS_2("§eInferno Typhoeus 2", Type.INFERNO_DEMONLORD, "§eTyphoeus 2"),
    SLAYER_BLAZE_TYPHOEUS_3("§cInferno Typhoeus 3", Type.INFERNO_DEMONLORD, "§cTyphoeus 3"),
    SLAYER_BLAZE_TYPHOEUS_4("§cInferno Typhoeus 4", Type.INFERNO_DEMONLORD, "§cTyphoeus 4"),

    SLAYER_BLAZE_QUAZII_1("§aInferno Quazii 1", Type.INFERNO_DEMONLORD, "§aQuazii 1"),
    SLAYER_BLAZE_QUAZII_2("§eInferno Quazii 2", Type.INFERNO_DEMONLORD, "§eQuazii 2"),
    SLAYER_BLAZE_QUAZII_3("§cInferno Quazii 3", Type.INFERNO_DEMONLORD, "§cQuazii 3"),
    SLAYER_BLAZE_QUAZII_4("§cInferno Quazii 4", Type.INFERNO_DEMONLORD, "§cQuazii 4"),

    SLAYER_BLOODFIEND_1("§aRiftstalker Bloodfiend 1", Type.RIFTSTALKER_BLOODFIEND, "§aBlood 1", showDeathTime = true),
    SLAYER_BLOODFIEND_2("§6Riftstalker Bloodfiend 2", Type.RIFTSTALKER_BLOODFIEND, "§6Blood 2", showDeathTime = true),
    SLAYER_BLOODFIEND_3("§cRiftstalker Bloodfiend 3", Type.RIFTSTALKER_BLOODFIEND, "§cBlood 3", showDeathTime = true),
    SLAYER_BLOODFIEND_4("§4Riftstalker Bloodfiend 4", Type.RIFTSTALKER_BLOODFIEND, "§4Blood 4", showDeathTime = true),
    SLAYER_BLOODFIEND_5("§5Riftstalker Bloodfiend 5", Type.RIFTSTALKER_BLOODFIEND, "§5Blood 5", showDeathTime = true),

    HUB_HEADLESS_HORSEMAN("§6Headless Horseman", Type.HEADLESS_HORSEMAN),

    DUNGEON_F1_BONZO_FIRST("§cFunny Bonzo", Type.DUNGEON_FLOOR_1),
    DUNGEON_F1_BONZO_SECOND("§cSad Bonzo", Type.DUNGEON_FLOOR_1),

    DUNGEON_F2_SUMMON("§eSummon", Type.DUNGEON_FLOOR_2),
    DUNGEON_F2_SCARF("§cScarf", Type.DUNGEON_FLOOR_2),

    DUNGEON_F3_GUARDIAN("§eGuardian", Type.DUNGEON_FLOOR_3),
    DUNGEON_F3_PROFESSOR_1("§cProfessor 1/2", Type.DUNGEON_FLOOR_3),
    DUNGEON_F3_PROFESSOR_2("§cProfessor 2/2", Type.DUNGEON_FLOOR_3),

    DUNGEON_F4_THORN("§cThorn", Type.DUNGEON_FLOOR_4),

    DUNGEON_F5("§dLivid", Type.DUNGEON_FLOOR_5),

    DUNGEON_F6_GIANT_1("§eBoulder Tosser", Type.DUNGEON_FLOOR_6, "§eGiant 1"),
    DUNGEON_F6_GIANT_2("§eSword Thrower", Type.DUNGEON_FLOOR_6, "§eGiant 2"),
    DUNGEON_F6_GIANT_3("§eBigfoot Jumper", Type.DUNGEON_FLOOR_6, "§eGiant 3"),
    DUNGEON_F6_GIANT_4("§eLaser Shooter", Type.DUNGEON_FLOOR_6, "§eGiant 4"),
    DUNGEON_F6_SADAN("§cSadan", Type.DUNGEON_FLOOR_6),

    // TODO implement
    DUNGEON_7("", Type.DUNGEON_FLOOR_7),

    MINOS_INQUISITOR("§5Minos Inquisitor", Type.DIANA_MOBS),
    MINOS_CHAMPION("§2Minos Champion", Type.DIANA_MOBS),
    GAIA_CONSTRUCT("§2Gaia Construct", Type.DIANA_MOBS),
    MINOTAUR("§2Minotaur", Type.DIANA_MOBS),

    THUNDER("§cThunder", Type.SEA_CREATURES),
    LORD_JAWBUS("§cLord Jawbus", Type.SEA_CREATURES),

    DUMMY("Dummy", Type.DUMMY),
    ARACHNE_SMALL("§cSmall Arachne", Type.ARACHNE),
    ARACHNE_BIG("§4Big Arachne", Type.ARACHNE),
    BROODMOTHER("§cBroodmother", Type.BROODMOTHER),

    // The Rift
    LEECH_SUPREME("§cLeech Supreme", Type.THE_RIFT_BOSSES),
    BACTE("§aBacte", Type.THE_RIFT_BOSSES),

    WINTER_REINDRAKE("Reindrake", Type.REINDRAKE), // TODO fix totally

    GARDEN_PEST_BEETLE("§cBeetle", Type.GARDEN_PESTS),
    GARDEN_PEST_CRICKET("§cCricket", Type.GARDEN_PESTS),
    GARDEN_PEST_FLY("§cFly", Type.GARDEN_PESTS),
    GARDEN_PEST_LOCUST("§cLocust", Type.GARDEN_PESTS),
    GARDEN_PEST_MITE("§cMite", Type.GARDEN_PESTS),
    GARDEN_PEST_MOSQUITO("§cMosquito", Type.GARDEN_PESTS),
    GARDEN_PEST_MOTH("§cMoth", Type.GARDEN_PESTS),
    GARDEN_PEST_RAT("§cRat", Type.GARDEN_PESTS),
    GARDEN_PEST_SLUG("§cSlug", Type.GARDEN_PESTS),
    GARDEN_PEST_EARTHWORM("§cEarthworm", Type.GARDEN_PESTS),

    // TODO Corleone
    // TODO bal

    /**
     * TODO dungeon mini bosses
     * shadow assassin
     * lost adventurer
     * frozen adventurer
     * king midas
     * in blood room: bonzo, scarf, ??
     * f7 blood room giants
     *
     */

    // TODO diana mythological creatures
}
