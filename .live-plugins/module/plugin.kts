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
import org.jetbrains.kotlin.psi.*

// depends-on-plugin org.jetbrains.kotlin

val forgeEvent = "SubscribeEvent"
val handleEvent = "HandleEvent"
val skyHanniModule = "SkyHanniModule"

val skyhanniPath = "at.hannibal2.skyhanni"
val patternGroup = "at.hannibal2.skyhanni.utils.repopatterns.RepoPatternGroup"
val pattern = "java.util.regex.Pattern"

registerInspection(ModuleInspectionKotlin())

fun isEvent(function: KtNamedFunction): Boolean {
    return function.annotationEntries.any {
        it.shortName!!.asString() == handleEvent || it.shortName!!.asString() == forgeEvent
    }
}

fun isRepoPattern(property: KtProperty): Boolean {
    val type = property.type()?.fqName?.asString() ?: return false
    if (type == patternGroup) return true
    if (type == pattern && property.hasDelegate()) return true
    return false
}

fun isFromSkyhanni(declaration: KtNamedDeclaration): Boolean {
    return declaration.fqName?.asString()?.startsWith(skyhanniPath) ?: false
}

class ModuleInspectionKotlin : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        val visitor = object : KtVisitorVoid() {

            override fun visitClass(klass: KtClass) {
                if (!isFromSkyhanni(klass)) return
                val hasAnnotation = klass.annotationEntries.any { it.shortName?.asString() == skyHanniModule }

                if (hasAnnotation) {
                    holder.registerProblem(
                        klass.nameIdentifier!!,
                        "@SkyHanniModule can only be applied to objects",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                }
            }

            override fun visitObjectDeclaration(declaration: KtObjectDeclaration) {
                if (!isFromSkyhanni(declaration)) return
                val hasAnnotation = declaration.annotationEntries.any { it.shortName?.asString() == skyHanniModule }
                if (hasAnnotation) return

                val hasSkyHanniEvents = declaration.body!!.functions.any { function -> isEvent(function) }
                val hasRepoPatterns = declaration.body!!.properties.any { property -> isRepoPattern(property) }
                if (!hasSkyHanniEvents && !hasRepoPatterns) return

                holder.registerProblem(
                    declaration,
                    "Module should have a @SkyHanniModule annotation",
                    ModuleQuickFix()
                )
            }
        }

        return visitor
    }

    override fun getDisplayName() = "Modules should have a @SkyHanniModule annotation"
    override fun getShortName() = "SkyHanniModuleInspection"
    override fun getGroupDisplayName() = "SkyHanni"
    override fun isEnabledByDefault() = true
}

class ModuleQuickFix : LocalQuickFix {
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val obj = descriptor.psiElement as KtObjectDeclaration
        AnnotationModificationHelper.addAnnotation(
            obj,
            FqName("at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule"),
            null,
            null,
            { null },
            " ",
            null
        )
    }

    override fun getName() = "Annotate with @SkyHanniModule"

    override fun getFamilyName() = name
}
