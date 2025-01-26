package at.hannibal2.skyhanni.detektrules.potentialbugs

import at.hannibal2.skyhanni.detektrules.SkyHanniRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.rules.hasAnnotation
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty

class StorageNeedsExpose(config: Config): SkyHanniRule(config) {
    override val issue = Issue(
        "StorageNeedsExpose",
        Severity.Defect,
        "Config/storage properties that are intended to store data should be annotated with @Expose.",
        Debt.TEN_MINS,
    )

    companion object {
        const val STORAGE_PACKAGE = "at.hannibal2.skyhanni.config.storage"
        const val CONFIG_PACKAGE = "at.hannibal2.skyhanni.config.features"
    }

    override fun visitKtFile(file: KtFile) {
        val packageName = file.packageDirective?.fqName?.asString() ?: ""
        if (!packageName.startsWith(CONFIG_PACKAGE) && !packageName.startsWith(STORAGE_PACKAGE)) return
        super.visitKtFile(file)
    }

    override fun visitProperty(property: KtProperty) {
        // Skip local variables inside functions
        if (property.isLocal) return

        // If the property is not annotated with @Expose, report it
        if (!property.hasAnnotation("Expose")) {

            if (property.hasAnnotation("ConfigOption")) {
                // Valid reasons to not have the @Expose annotation on a config option:
                //  - Has the ConfigEditorInfoText annotation
                //  - Has the ConfigEditorButton annotation
                if(property.hasAnnotation("ConfigEditorInfoText")) return
                if(property.hasAnnotation("ConfigEditorButton")) return
            }

            property.reportIssue("@Expose annotation is missing from property ${property.name}")
        }

        super.visitProperty(property)
    }
}
