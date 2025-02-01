package at.hannibal2.skyhanni.detektrules.compat

import at.hannibal2.skyhanni.detektrules.SkyHanniRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtProperty

class McPlayerRule(config: Config) : SkyHanniRule(config) {
    override val issue = Issue(
        "McPlayerRule",
        Severity.Style,
        "Avoid using Minecraft.getMinecraft().thePlayer",
        Debt.FIVE_MINS,
    )

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        checkForMinecraftPlayer(property.initializer)
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        checkForMinecraftPlayer(expression)
    }

    private fun checkForMinecraftPlayer(element: KtExpression?) {
        if (element?.text?.contains("Minecraft.getMinecraft().thePlayer") == true) {
            element.reportIssue("Usage of Minecraft.getMinecraft().thePlayer detected. Use McPlayer instead.")
        }
    }
}


