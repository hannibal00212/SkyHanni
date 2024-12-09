import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.findParentOfType
import liveplugin.openInBrowser
import liveplugin.registerIntention
import liveplugin.show
import org.intellij.markdown.html.urlEncode
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.*

// depends-on-plugin org.jetbrains.kotlin

registerIntention(RenameKotlinFunctionToUseCamelCaseIntention())
if (!isIdeStartup) show("Reloaded Regex intentions")
val logger =
    Logger.getInstance("SkyHanni")

val regexTestPrefix = "REGEX-TEST: "
val regexTestFailPrefix = "REGEX-FAIL: "

class RegexInfo(
    val regex: KtValueArgument,
    val comment: KDoc?,
) {
    fun getRegexText(): String? {
        val templateExpr = regex.getArgumentExpression() as? KtStringTemplateExpression ?: return null
        val sb = StringBuilder()
        for (x in templateExpr.entries) {
            when (x) {
                is KtEscapeStringTemplateEntry -> sb.append(x.unescapedValue)
                is KtLiteralStringTemplateEntry -> sb.append(x.text)
                else -> return null
            }
        }
        return sb.toString()
    }

    val commentText by lazy {
        comment?.text
            ?.replace("/*", "")
            ?.replace("*/", "")
            ?.lines()
            ?.map {
                it.trim().trimStart('*').trim()
            }
    }

    fun getExamples(): List<String> {
        val examples = commentText?.filter { it.startsWith(regexTestPrefix) || it.startsWith(regexTestFailPrefix) }
            ?.map { it.substring(regexTestPrefix.length) }
        if (examples == null) return listOf()
        return examples
    }
}

inner class RenameKotlinFunctionToUseCamelCaseIntention : PsiElementBaseIntentionAction() {
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return findRegexInfo(element) != null
    }

    fun findRegexInfo(element: PsiElement): RegexInfo? {
        val call = element.findParentOfType<KtCallExpression>() ?: return null
        if (call.valueArguments.size != 2) return null
        val methodName = call.calleeExpression as? KtSimpleNameExpression ?: return null
        if (methodName.getReferencedName() != "pattern") return null
        val field = call.findParentOfType<KtProperty>() ?: return null
        val regex = call.valueArguments[1] ?: return null
        val comment = field.docComment
        return RegexInfo(regex, comment)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val info = findRegexInfo(element) ?: return
        val regex = info.getRegexText()
        if (regex == null) {
            show("Regex needs to be a bare string literal in order to open it in the browser!")
            return
        }
        openInBrowser(
            "https://regex101.com/?regex=${urlEncode(regex)}&testString=${
                urlEncode(
                    info.getExamples().joinToString("\n")
                )
            }"
        )
    }

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
        val element = getElement(editor, file) ?: return IntentionPreviewInfo.EMPTY
        val info = findRegexInfo(element) ?: return IntentionPreviewInfo.EMPTY
        val exampleCount = info.getExamples().size
        return IntentionPreviewInfo.Html(
            """
            Opens this regex on regex101.com with $exampleCount example${s(exampleCount)}.
            """
        )
    }

    override fun getText() = "Open regex101.com"
    override fun getFamilyName() = "OpenRegexExplorerIntention"
}

fun s(count: Int): String {
    return if (count == 1) "" else "s"
}
