package at.hannibal2.skyhanni.config

import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean

/**
 * Annotate a [ConfigEditorBoolean] to indicate that it is a feature toggle.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class FeatureToggle(
    /**
     * Indicate that this field being true means the corresponding feature is enabled.
     */
    val trueIsEnabled: Boolean = true,
)
