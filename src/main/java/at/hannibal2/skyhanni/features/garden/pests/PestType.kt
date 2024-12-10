package at.hannibal2.skyhanni.features.garden.pests

import at.hannibal2.skyhanni.features.combat.damageindicator.BossType
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName

enum class PestType(
    val displayName: String,
    val damageIndicatorBoss: BossType,
    val spray: SprayType?,
    val vinyl: VinylType?,
    val internalName: NEUInternalName,
    val crop: CropType?,
    val pluralName: String? = "${displayName}s",
) {
    BEETLE(
        "Beetle",
        BossType.GARDEN_PEST_BEETLE,
        SprayType.DUNG,
        VinylType.NOT_JUST_A_PEST,
        "PEST_BEETLE_MONSTER".toInternalName(),
        CropType.NETHER_WART,
    ),
    CRICKET(
        "Cricket",
        BossType.GARDEN_PEST_CRICKET,
        SprayType.HONEY_JAR,
        VinylType.CRICKET_CHOIR,
        "PEST_CRICKET_MONSTER".toInternalName(),
        CropType.CARROT,
    ),
    EARTHWORM(
        "Earthworm",
        BossType.GARDEN_PEST_EARTHWORM,
        SprayType.COMPOST,
        VinylType.EARTHWORM_ENSEMBLE,
        "PEST_EARTHWORM_MONSTER".toInternalName(),
        CropType.MELON,
    ),
    FIELD_MOUSE(
        "Field Mouse",
        BossType.GARDEN_PEST_FIELD_MOUSE,
        spray = null,
        vinyl = null,
        "PEST_FIELD_MOUSE_MONSTER".toInternalName(),
        crop = null,
        pluralName = "Field Mice",
    ),
    FLY(
        "Fly",
        BossType.GARDEN_PEST_FLY,
        SprayType.DUNG,
        VinylType.PRETTY_FLY,
        "PEST_FLY_MONSTER".toInternalName(),
        CropType.WHEAT,
        pluralName = "Flies",
    ),
    LOCUST(
        "Locust",
        BossType.GARDEN_PEST_LOCUST,
        SprayType.PLANT_MATTER,
        VinylType.CICADA_SYMPHONY,
        "PEST_LOCUST_MONSTER".toInternalName(),
        CropType.POTATO,
    ),
    MITE(
        "Mite",
        BossType.GARDEN_PEST_MITE,
        SprayType.TASTY_CHEESE,
        VinylType.DYNAMITES,
        "PEST_MITE_MONSTER".toInternalName(),
        CropType.CACTUS,
    ),
    MOSQUITO(
        "Mosquito",
        BossType.GARDEN_PEST_MOSQUITO,
        SprayType.COMPOST,
        VinylType.BUZZIN_BEATS,
        "PEST_MOSQUITO_MONSTER".toInternalName(),
        CropType.SUGAR_CANE,
        pluralName = "Mosquitoes",
    ),
    MOTH(
        "Moth",
        BossType.GARDEN_PEST_MOTH,
        SprayType.HONEY_JAR,
        VinylType.WINGS_OF_HARMONY,
        "PEST_MOTH_MONSTER".toInternalName(),
        CropType.COCOA_BEANS,
    ),
    RAT(
        "Rat",
        BossType.GARDEN_PEST_RAT,
        SprayType.TASTY_CHEESE,
        VinylType.RODENT_REVOLUTION,
        "PEST_RAT_MONSTER".toInternalName(),
        CropType.PUMPKIN,
    ),
    SLUG(
        "Slug",
        BossType.GARDEN_PEST_SLUG,
        SprayType.PLANT_MATTER,
        VinylType.SLOW_AND_GROOVY,
        "PEST_SLUG_MONSTER".toInternalName(),
        CropType.MUSHROOM,
    ),
    // For use in the Pest Profit Tracker, in cases where an item cannot have an identified PestType
    // Display name intentionally omitted to aid in filtering out this entry.
    UNKNOWN(
        "",
        BossType.DUMMY,
        spray = null,
        vinyl = null,
        "DUMMY".toInternalName(),
        crop = null,
    ),
    ;

    override fun toString(): String {
        return displayName
    }

    @SkyHanniModule
    companion object {
        val filterableEntries by lazy { entries.filter { it.displayName.isNotEmpty() } }

        fun getByNameOrNull(name: String): PestType? {
            return filterableEntries.firstOrNull { it.displayName.lowercase() == name.lowercase() }
        }

        fun getByName(name: String) = getByNameOrNull(name) ?: error("No valid pest type '$name'")

        private val internalNameRareDropMap: Map<NEUInternalName, PestType?> = mapOf(
            // Beetle deterministic drops
            "ENCHANTED_NETHER_STALK" to BEETLE,
            "PESTERMINATOR;1" to BEETLE,
            "VINYL_BEETLE" to BEETLE,
            "MUTANT_NETHER_STALK" to BEETLE,

            // Cricket deterministic drops
            "ENCHANTED_CARROT" to CRICKET,
            "ENCHANTED_GOLDEN_CARROT" to CRICKET,
            "VINYL_CRICKET_CHOIR" to CRICKET,
            "CHIRPING_STEREO" to CRICKET,

            // Earthworm deterministic drops
            "ENCHANTED_MELON" to EARTHWORM,
            "VINYL_EARTHWORM_ENSEMBLE" to EARTHWORM,
            "ENCHANTED_MELON_BLOCK" to EARTHWORM,
            "BOOKWORM_BOOK" to EARTHWORM,

            // Field Mouse deterministic drops
            "SQUEAKY_TOY" to FIELD_MOUSE,
            "SQUEAKY_MOUSEMAT" to FIELD_MOUSE,

            // Fly deterministic drops
            "ENCHANTED_WHEAT" to FLY,
            "BEADY_EYES" to FLY,
            "VINYL_PRETTY_FLY" to FLY,
            "ENCHANTED_HAY_BALE" to FLY,
            // Old fly drops
            "TIGHTLY_TIED_HAY_BALE" to FLY,
            "ENCHANTED_HAY_BLOCK" to FLY,

            // Locust deterministic drops
            "ENCHANTED_POTATO" to LOCUST,
            "VINYL_CICADA_SYMPHONY" to LOCUST,
            "ENCHANTED_BAKED_POTATO" to LOCUST,
            "SUNDER;6" to LOCUST,

            // Mite deterministic drops
            "ENCHANTED_CACTUS_GREEN" to MITE,
            "VINYL_DYNAMITES" to MITE,
            "ENCHANTED_CACTUS" to MITE,
            "ATMOSPHERIC_FILTER" to MITE,

            // Mosquito deterministic drops
            "ENCHANTED_SUGAR" to MOSQUITO,
            "VINYL_BUZZIN_BEATS" to MOSQUITO,
            "ENCHANTED_SUGAR_CANE" to MOSQUITO,
            "CLIPPED_WINGS" to MOSQUITO,

            // Moth deterministic drops
            "ENCHANTED_COCOA" to MOTH,
            "VINYL_WINGS_OF_HARMONY" to MOTH,
            "ENCHANTED_COOKIE" to MOTH,
            "WRIGGLING_LARVA" to MOTH,

            // Rat deterministic drops
            "ENCHANTED_PUMPKIN" to RAT,
            "VINYL_RODENT_REVOLUTION" to RAT,
            "POLISHED_PUMPKIN" to RAT,
            "RAT;4" to RAT,

            // Slug deterministic drops
            "ENCHANTED_RED_MUSHROOM" to SLUG,
            "ENCHANTED_BROWN_MUSHROOM" to SLUG,
            "VINYL_SLOW_AND_GROOVY" to SLUG,
            "ENCHANTED_HUGE_MUSHROOM_1" to SLUG,
            "ENCHANTED_HUGE_MUSHROOM_2" to SLUG,
            "SLUG;3" to SLUG,
            "SLUG;4" to SLUG,

            // Spray drops only send chat message from mice
            "COMPOST" to FIELD_MOUSE,
            "HONEY_JAR" to FIELD_MOUSE,
            "DUNG" to FIELD_MOUSE,
            "PLANT_MATTER" to FIELD_MOUSE,
            "CHEESE_FUEL" to FIELD_MOUSE,

            // Indeterministic drops
            "DYE_DUNG" to UNKNOWN,
        ).map {
            it.key.toInternalName() to it.value
        }.toMap()

        fun getByInternalNameItemOrNull(internalName: NEUInternalName): PestType? = internalNameRareDropMap[internalName]
    }
}
