package at.hannibal2.skyhanni.features.misc.massconfiguration

data class FeatureToggleableOption(
    val name: String, val description: String, val previouslyEnabled: Boolean,
    val isTrueEnabled: Boolean, val category: Category,
    val setter: (Boolean) -> Unit,
    val path: String,
    var toggleOverride: ResetSuggestionState? = null,
)
