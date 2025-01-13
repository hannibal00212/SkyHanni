package skyhannibuildsystem

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class PrepareRelease : DefaultTask() {

    @Input
    var modVersion: String = ""

    @Input
    var remote: String = "origin"

    private val versionPattern = "\\d+\\.\\d+\\.\\d+".toPattern()

    @TaskAction
    fun prepareRelease() {
        val version = modVersion

        if (version.isEmpty() || !versionPattern.matcher(version).matches()) {
            throw IllegalStateException("Version not properly set, You must set a version. Example: ./gradlew prepareRelease -Pver=1.2.3")
        }

        project.exec {
            commandLine("git", "tag", version)
        }

        project.exec {
            commandLine("git", "push", remote, version)
        }

        println("Tagged and pushed version $version")
    }
}
