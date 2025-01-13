package skyhannibuildsystem

import at.hannibal2.changelog.ModVersion
import at.hannibal2.changelog.SkyHanniChangelogBuilder
import at.hannibal2.changelog.WhatToFetch
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ChangelogGeneration : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @Input
    var modVersion: String = ""

    @Input
    var outputType: String = ""

    @TaskAction
    fun generateChangelog() {

        if (modVersion.isEmpty()) {
            println()
            println("Generating example changelog")
            SkyHanniChangelogBuilder.generateChangelog(WhatToFetch.ALREADY_MERGED, ModVersion(0, 0, 0), null)
            return
        }

        println()
        println("Generating changelog for version $modVersion")
        println("Output type: $outputType")
        println()

        val changelog = SkyHanniChangelogBuilder.generateSpecificOutputType(modVersion, outputType)
        val file = File(outputDirectory.get().asFile, "changelog-$outputType.txt")
        file.writeText(changelog)

        println("Changelog saved to ${file.path}")
    }
}
