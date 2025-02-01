package at.hannibal2.skyhanni.detektrules.compat

import at.hannibal2.skyhanni.detektrules.SkyHanniRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtProperty

class McWorldRule(config: Config) : SkyHanniRule(config) {
    override val issue = Issue(
        "McWorldRule",
        Severity.Style,
        "Avoid using Minecraft.getMinecraft().theWorld",
        Debt.FIVE_MINS,
    )

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        checkforMinecraftWorld(property.initializer)
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        checkforMinecraftWorld(expression)
    }

    private fun checkforMinecraftWorld(element: KtExpression?) {
        if (element?.text?.contains("Minecraft.getMinecraft().theWorld") == true) {
            element.reportIssue("Usage of Minecraft.getMinecraft().theWorld detected. Use McWorld instead.")
        }
    }
}
