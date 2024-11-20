package at.hannibal2.skyhanni.features.garden.pests

enum class VinylType(val displayName: String) {
    PRETTY_FLY("Pretty Fly"),
    CRICKET_CHOIR("Cricket Choir"),
    CICADA_SYMPHONY("Cicada Symphony"),
    RODENT_REVOLUTION("Rodent Revolution"),
    BUZZIN_BEATS("Buzzin' Beats"),
    EARTHWORM_ENSEMBLE("Earthworm Ensemble"),
    DYNAMITES("DynaMITES"),
    WINGS_OF_HARMONY("Wings of Harmony"),
    SLOW_AND_GROOVY("Slow and Groovy"),
    NOT_JUST_A_PEST("Not Just a Pest"),
    MOUSE_VINLY("Mouse Vinly"),
    NONE("Nothing")

    ;

    companion object {

        fun getByName(name: String) = VinylType.entries.firstOrNull { it.displayName == name } ?: NONE
    }
}
