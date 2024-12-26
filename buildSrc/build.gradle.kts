import skyhannibuildsystem.ChangelogVerification

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") {
        content {
            includeGroupByRegex("com\\.github\\..*")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.github.SkyHanniStudios:SkyHanniChangelogBuilder:1.0.3")
}

tasks.register("checkPrDescription", ChangelogVerification::class) {
    this.outputDirectory.set(layout.buildDirectory)
    this.prTitle = project.findProperty("prTitle") as String
    this.prBody = project.findProperty("prBody") as String
}
