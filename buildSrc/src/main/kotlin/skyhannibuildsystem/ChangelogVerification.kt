package skyhannibuildsystem

import at.hannibal2.changelog.SkyHanniChangelogBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ChangelogVerification : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @Input
    var prTitle: String = ""

    @Input
    var prBody: String = ""

    @get:Internal
    val prBodyLines get() = prBody.lines()

    private val prLink = "ignored"
    private val templateLocation = "https://github.com/hannibal002/SkyHanni/blob/beta/pull_request_template.md?plain=1"

    @TaskAction
    fun scanChangelog() {
        if (prBodyLines.let { it.contains("exclude_from_changelog") || it.contains("ignore_from_changelog") }) {
            println("PR is excluded from changelog verification")
            return
        }

        val (changes, bodyErrors) = SkyHanniChangelogBuilder.findChanges(prBodyLines, prLink)
        val titleErrors = SkyHanniChangelogBuilder.findPullRequestNameErrors(prTitle, changes)

        if (bodyErrors.isEmpty() && titleErrors.isEmpty()) {
            println("Changelog and title verification successful")
        } else {
            bodyErrors.forEach { println(it.message) }
            titleErrors.forEach { println(it.message) }

            // Export errors so that they can be listed in the PR comment
            val errorFile = File(outputDirectory.get().asFile, "changelog_errors.txt")
            println("saved error file to: ${errorFile.path}")

            errorFile.appendText("I have detected some issues with your pull request:\n\n")

            if (bodyErrors.isNotEmpty()) {
                errorFile.appendText("Body issues:\n${bodyErrors.joinToString("\n") { it.formatLine() }}\n\n")
            }
            if (titleErrors.isNotEmpty()) {
                errorFile.appendText("Title issues:\n${titleErrors.joinToString("\n") { it.message }}\n\n")
            }

            errorFile.appendText("Please fix these issues. For the correct format, refer to the [pull request template]($templateLocation).")

            throw GradleException("Changelog verification failed")
        }
    }
}
