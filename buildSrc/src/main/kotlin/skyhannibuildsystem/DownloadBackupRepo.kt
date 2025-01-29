package skyhannibuildsystem

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.net.URL

// Code taken from NotEnoughUpdates
abstract class DownloadBackupRepo : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract var branch: String

    @get:Internal
    val repoFile get() = outputDirectory.get().asFile.resolve("assets/skyhanni/repo.zip")

    @TaskAction
    fun downloadRepo() {
        val downloadUrl = URL("https://github.com/hannibal002/SkyHanni-Repo/archive/refs/heads/$branch.zip")
        val file = repoFile
        file.parentFile.mkdirs()
        file.outputStream().use { out ->
            downloadUrl.openStream().use { inp ->
                inp.copyTo(out)
            }
        }
    }
}
