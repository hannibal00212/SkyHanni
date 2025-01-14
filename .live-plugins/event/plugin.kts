import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import liveplugin.registerInspection
import org.jetbrains.kotlin.idea.base.utils.fqname.fqName
import org.jetbrains.kotlin.idea.codeinsight.api.classic.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.util.AnnotationModificationHelper
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.nj2k.postProcessing.type
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.types.typeUtil.supertypes

// depends-on-plugin org.jetbrains.kotlin

val skyhanniEvent = "at.hannibal2.skyhanni.api.event.SkyHanniEvent"
val handleEvent = "HandleEvent"
val eventType = "eventType"

registerInspection(HandleEventInspectionKotlin())

class HandleEventInspectionKotlin : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        val visitor = object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                val hasEventAnnotation = function.annotationEntries.any { it.shortName!!.asString() == handleEvent }

                // Check if the function's parameter is a SkyHanniEvent or its subtype
                val isEvent = function.valueParameters.firstOrNull()?.type()?.supertypes()
                    ?.any { it.fqName?.asString() == skyhanniEvent } ?: false

                // Find the annotation entry
                val annotationEntry = function.annotationEntries
                    .find { it.shortName!!.asString() == handleEvent }

                // Check if the annotation specifies the eventType explicitly or as a positional parameter
                val hasEventType = annotationEntry?.valueArguments
                    ?.any { argument ->
                        // Check if it is a named parameter for `eventType`
                        argument.getArgumentName()?.asName?.asString() == "eventType" ||
                            // Check if it is a positional argument (first argument)
                            (annotationEntry.valueArguments.indexOf(argument) == 0 &&
                                argument.getArgumentExpression()?.text != null)
                    } ?: false

                // Validate function annotation and parameters
                if (isEvent && !hasEventAnnotation && function.valueParameters.size == 1 && function.isPublic) {
                    holder.registerProblem(
                        function,
                        "Event handler function should be annotated with @HandleEvent",
                        HandleEventQuickFix()
                    )
                } else if (!isEvent && !hasEventType && hasEventAnnotation) {
                    holder.registerProblem(
                        function,
                        "Function should not be annotated with @HandleEvent if it does not take a SkyHanniEvent",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                }
            }
        }

        return visitor
    }

    override fun getDisplayName() = "Event handler function should be annotated with @HandleEvent"
    override fun getShortName() = "HandleEventInspection"
    override fun getGroupDisplayName() = "SkyHanni"
    override fun isEnabledByDefault() = true
}

class HandleEventQuickFix : LocalQuickFix {
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val function = descriptor.psiElement as KtNamedFunction
        AnnotationModificationHelper.addAnnotation(
            function,
            FqName("at.hannibal2.skyhanni.api.event.HandleEvent"),
            null,
            null,
            { null },
            " ",
            null
        )
    }

    override fun getName() = "Annotate with @HandleEvent"

    override fun getFamilyName() = name
}
