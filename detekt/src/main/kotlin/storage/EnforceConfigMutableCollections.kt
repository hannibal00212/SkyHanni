package at.hannibal2.skyhanni.detektrules.storage

import at.hannibal2.skyhanni.detektrules.SkyHanniRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression

class EnforceConfigMutableCollections(config: Config): SkyHanniRule(config) {
    override val issue = Issue(
        "EnforceConfigMutableCollections",
        Severity.Defect,
        "Use specified mutable collections instead of immutable, or implied mutable ones.",
        Debt.TEN_MINS,
    )

    companion object {
        private const val CONFIG_PACKAGE = "at.hannibal2.skyhanni.config.features"

        private val badListInitializers = setOf(
            "listOf",
            "ArrayList",
            "arrayListOf"
        )

        private val badSetInitializers = setOf(
            "setOf",
            "HashSet",
            "hashSetOf"
        )

        private val badMapInitializers = setOf(
            "mapOf",
            "HashMap",
            "hashMapOf"
        )

        private val badInitializers by lazy {
            badListInitializers + badSetInitializers + badMapInitializers
        }
    }

    override fun visitKtFile(file: KtFile) {
        val packageName = file.packageDirective?.fqName?.asString() ?: ""
        if (!packageName.startsWith(CONFIG_PACKAGE)) return
        super.visitKtFile(file)
    }

    override fun visitProperty(property: KtProperty) {
        // Only look at properties annotated with @Expose
        if (!property.hasAnnotation("Expose")) return
        // Only look at properties with @ConfigOption
        if (!property.hasAnnotation("ConfigOption")) return

        // If the property is a List<*>, Set<*>, or Map<*>, check how it's initialized
        // We remove the "Property<" prefix from the type reference, e.g. "Property<List<String>>" -> "List<String>".
        val typeRef = property.typeReference?.text?.replaceFirst("Property<", "") ?: ""
        // println("typeRef = '${property.typeReference?.text}' initializer = '${property.initializer?.text}'")

        if (typeRef.startsWith("List<") ||
            typeRef.startsWith("Set<")  ||
            typeRef.startsWith("Map<")
        ) {
            // Examine the initializer call, e.g. listOf(...), setOf(...), mapOf(...).
            val callExpression = property.initializer as? KtCallExpression
            val calleeName = callExpression?.getCallNameExpression()?.getReferencedName() ?: ""

            val calleeIsBad = calleeName in badInitializers
            if (calleeIsBad) {
                val properCallee = getProperCallee(calleeName)
                property.reportIssue("Use $properCallee instead of $calleeName.")
            }
        }

        super.visitProperty(property)
    }

    private fun getProperCallee(calleeName: String) = when (calleeName) {
        in badListInitializers -> "mutableListOf"
        in badSetInitializers -> "mutableSetOf"
        in badMapInitializers -> "mutableMapOf"
        else -> ""
    }

    /**
     * Helper function to check if a property has a specific annotation.
     */
    private fun KtModifierListOwner.hasAnnotation(annotationName: String): Boolean {
        // shortName?.asString() should normally be 'Expose' even with @field:Expose
        return annotationEntries.any {
            it.shortName?.asString() == annotationName
        }
    }
}
