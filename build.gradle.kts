import at.skyhanni.sharedvariables.MinecraftVersion
import at.skyhanni.sharedvariables.MultiVersionStage
import at.skyhanni.sharedvariables.ProjectTarget
import at.skyhanni.sharedvariables.SHVersionInfo
import at.skyhanni.sharedvariables.versionString
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import moe.nea.shot.ShotParser
import moe.nea.shot.Shots
import net.fabricmc.loom.api.processor.MinecraftJarProcessor
import net.fabricmc.loom.api.processor.ProcessorContext
import net.fabricmc.loom.api.processor.SpecContext
import net.fabricmc.loom.task.RunGameTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import skyhannibuildsystem.ChangelogVerification
import skyhannibuildsystem.DownloadBackupRepo
import java.io.Serializable
import java.nio.file.Path
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream

plugins {
    idea
    java
    id("com.gradleup.shadow") version "8.3.4"
    id("gg.essential.loom")
    id("dev.deftu.gradle.preprocess")
    kotlin("jvm")
    id("com.google.devtools.ksp")
    kotlin("plugin.power-assert")
    `maven-publish`
    id("moe.nea.shot") version "1.0.0"
    id("io.gitlab.arturbosch.detekt")
    id("net.kyori.blossom")
}

val target = ProjectTarget.values().find { it.projectPath == project.path }!!

// Toolchains:
java {
    toolchain.languageVersion.set(target.minecraftVersion.javaLanguageVersion)
    // We specifically request ADOPTIUM because if we do not restrict the vendor DCEVM is a
    // possible candidate. Some DCEVMs are however incompatible with some things gradle is doing,
    // causing crashes during tests. You can still manually select DCEVM in the Minecraft Client
    // IntelliJ run configuration.
    toolchain.vendor.set(JvmVendorSpec.ADOPTIUM)
}
val runDirectory = rootProject.file("run")
runDirectory.mkdirs()
// Minecraft configuration:
loom {
    if (this.isForgeLike) {
        forge {
            pack200Provider.set(
                dev.architectury.pack200.java
                    .Pack200Adapter(),
            )
            mixinConfig("mixins.skyhanni.json")
        }
    }
    mixin {
        useLegacyMixinAp.set(true)
        defaultRefmapName.set("mixins.skyhanni.refmap.json")
    }
    runs {
        named("client") {
            if (target == ProjectTarget.MAIN) {
                isIdeConfigGenerated = true
                appendProjectPathToConfigName.set(false)
            }
            this.runDir(runDirectory.relativeTo(projectDir).toString())
            property("mixin.debug", "true")
            if (System.getenv("repo_action") != "true") {
                property("devauth.configDir", rootProject.file(".devauth").absolutePath)
            }
            vmArgs("-Xmx4G")
            programArgs("--tweakClass", "at.hannibal2.skyhanni.tweaker.SkyHanniTweaker")
            programArgs("--tweakClass", "io.github.notenoughupdates.moulconfig.tweaker.DevelopmentResourceTweaker")
        }
        removeIf { it.name == "server" }
    }
}

if (target == ProjectTarget.MAIN) {
    sourceSets.main {
        resources.destinationDirectory.set(kotlin.destinationDirectory)
        output.setResourcesDir(kotlin.destinationDirectory)
    }
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val shadowModImpl: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

val devenvMod: Configuration by configurations.creating {
    isTransitive = false
    isVisible = false
}

val headlessLwjgl by configurations.creating {
    isTransitive = false
    isVisible = false
}

val includeBackupRepo by tasks.registering(DownloadBackupRepo::class) {
    this.outputDirectory.set(layout.buildDirectory.dir("downloadedRepo"))
    this.branch = "main"
}

tasks.runClient {
    this.javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(target.minecraftVersion.javaLanguageVersion)
        },
    )
}

tasks.register("checkPrDescription", ChangelogVerification::class) {
    this.outputDirectory.set(layout.buildDirectory)
    this.prTitle = project.findProperty("prTitle") as String
    this.prBody = project.findProperty("prBody") as String
}

file("shots.txt")
    .takeIf(File::exists)
    ?.readText()
    ?.lines()
    ?.let(ShotParser()::parse)
    ?.let(::Shots)
    ?.let {
        loom.addMinecraftJarProcessor(ShotApplicationJarProcessor::class.java, it)
    }

dependencies {
    minecraft("com.mojang:minecraft:${target.minecraftVersion.versionName}")
    if (target.mappingDependency == "official") {
        mappings(loom.officialMojangMappings())
    } else {
        mappings(target.mappingDependency)
    }
    if (target.forgeDep != null) {
        "forge"(target.forgeDep!!)
    }

    // Discord RPC client
    shadowImpl("com.jagrosh:DiscordIPC:0.5.3") {
        exclude(module = "log4j")
        because("Different version conflicts with Minecraft's Log4J")
        exclude(module = "gson")
        because("Different version conflicts with Minecraft's Log4j")
    }
    compileOnly(libs.jbAnnotations)

    headlessLwjgl(libs.headlessLwjgl)

    compileOnly(ksp(project(":annotation-processors"))!!)

    val mixinVersion = if (target.minecraftVersion >= MinecraftVersion.MC11200) "0.8.2" else "0.7.11-SNAPSHOT"

    if (!target.isFabric) {
        shadowImpl("org.spongepowered:mixin:$mixinVersion") {
            isTransitive = false
        }
        annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
        annotationProcessor("com.google.code.gson:gson:2.10.1")
        annotationProcessor("com.google.guava:guava:17.0")
    } else if (target == ProjectTarget.BRIDGE116FABRIC) {
        modCompileOnly("net.fabricmc:fabric-loader:0.16.7")
        modCompileOnly("net.fabricmc.fabric-api:fabric-api:0.42.0+1.16")
    } else if (target == ProjectTarget.MODERN) {
        modCompileOnly("net.fabricmc:fabric-loader:0.16.7")
        modCompileOnly("net.fabricmc.fabric-api:fabric-api:0.102.0+1.21")
    }

    implementation(kotlin("stdlib-jdk8"))
    shadowImpl("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") {
        exclude(group = "org.jetbrains.kotlin")
    }

    if (target.isForge) modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")
    else modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

    modCompileOnly("com.github.hannibal002:notenoughupdates:4957f0b:all") {
        exclude(module = "unspecified")
        isTransitive = false
    }
    // October 3, 2024, 11:43 PM AEST
    // https://github.com/NotEnoughUpdates/NotEnoughUpdates/tree/2.4.0
    devenvMod("com.github.NotEnoughUpdates:NotEnoughUpdates:2.4.0:all") {
        exclude(module = "unspecified")
        isTransitive = false
    }

    shadowModImpl(libs.moulconfig)
    shadowImpl(libs.libautoupdate) {
        exclude(module = "gson")
    }
    shadowImpl("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
    implementation(libs.hotswapagentforge)

    testImplementation("com.github.NotEnoughUpdates:NotEnoughUpdates:faf22b5dd9:all") {
        exclude(module = "unspecified")
        isTransitive = false
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("io.mockk:mockk:1.12.5")

    implementation("net.hypixel:mod-api:0.3.1")

    // getting clock offset
    shadowImpl("commons-net:commons-net:3.8.0")

    detektPlugins("org.notenoughupdates:detektrules:1.0.0")
    detektPlugins(project(":detekt"))
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

afterEvaluate {
    loom.runs.named("client") {
        programArgs("--mods", devenvMod.resolve().joinToString(",") { it.relativeTo(runDirectory).path })
    }
}

tasks.withType(Test::class) {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    workingDir(file(runDirectory))
    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
            enableLanguageFeature("BreakContinueInInlineLambdas")
        }
    }
}

// Tasks:
tasks.processResources {
    from(includeBackupRepo)
    inputs.property("version", version)
    filesMatching(listOf("mcmod.info", "fabric.mod.json")) {
        expand("version" to version)
    }
    if (target.isFabric) {
        exclude("mcmod.info")
    } // else do NOT exclude fabric.mod.json. We use fabric.mod.json in order to show a logo in prism launcher.
}

if (target == ProjectTarget.MAIN) {
    tasks.create("generateRepoPatterns", RunGameTask::class, loom.runs.named("client").get()).apply {
        javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
        dependsOn(tasks.configureLaunch)
        jvmArgs(
            "-Dorg.lwjgl.opengl.Display.allowSoftwareOpenGL=true",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006",
            "-javaagent:${headlessLwjgl.singleFile.absolutePath}",
        )
        val outputFile = project.file("build/regexes/constants.json")
        environment("SKYHANNI_DUMP_REGEXES", "${SHVersionInfo.gitHash}:${outputFile.absolutePath}")
        environment("SKYHANNI_DUMP_REGEXES_EXIT", "true")
    }
}

if (target == ProjectTarget.MAIN) {
    tasks.compileJava {
        dependsOn(tasks.processResources)
    }
}

fun includeBuildPaths(buildPathsFile: File, sourceSet: Provider<SourceSet>) {
    if (buildPathsFile.exists()) {
        sourceSet.get().apply {
            val buildPaths = buildPathsFile.readText().lineSequence()
                .map { it.substringBefore("#").trim() }
                .filter { it.isNotBlank() }
                .toSet()
            kotlin.include(buildPaths)
            java.include(buildPaths)
        }
    }
}
includeBuildPaths(file("buildpaths.txt"), sourceSets.main)
includeBuildPaths(file("buildpaths-test.txt"), sourceSets.test)

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(target.minecraftVersion.formattedJavaLanguageVersion))
}

if (target.parent == ProjectTarget.MAIN) {
    val mainRes = project(ProjectTarget.MAIN.projectPath).tasks.getAt("processResources")
    tasks.named("processResources") {
        dependsOn(mainRes)
    }
    tasks.named("preprocessCode") {
        dependsOn(mainRes)
    }
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(org.gradle.jvm.tasks.Jar::class) {
    archiveBaseName.set("SkyHanni")
    archiveAppendix.set(target.minecraftVersion.versionName)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Why do we have this here? This only *hides* errors.
    manifest.attributes.run {
        this["Main-Class"] = "SkyHanniInstallerFrame"
        if (target == ProjectTarget.MAIN) {
            this["FMLCorePluginContainsFMLMod"] = "true"
            this["ForceLoadAsMod"] = "true"
            this["TweakClass"] = "at.hannibal2.skyhanni.tweaker.SkyHanniTweaker"
            this["MixinConfigs"] = "mixins.skyhanni.json"
        }
    }
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl, shadowModImpl)
    doLast {
        configurations.forEach {
            println("Config: ${it.files}")
        }
    }
    exclude("META-INF/versions/**")
    mergeServiceFiles()
    relocate("io.github.notenoughupdates.moulconfig", "at.hannibal2.skyhanni.deps.moulconfig")
    relocate("moe.nea.libautoupdate", "at.hannibal2.skyhanni.deps.libautoupdate")
    relocate("com.jagrosh.discordipc", "at.hannibal2.skyhanni.deps.discordipc")
    relocate("org.apache.commons.net", "at.hannibal2.skyhanni.deps.commons.net")
}
tasks.jar {
    archiveClassifier.set("nodeps")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
}
tasks.assemble.get().dependsOn(tasks.remapJar)

tasks.withType(KotlinCompile::class) {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(target.minecraftVersion.javaLanguageVersion.versionString()))
    }
}

if (!MultiVersionStage.activeState.shouldCompile(target)) {
    tasks.withType<JavaCompile> {
        onlyIf { false }
    }
    tasks.withType<KotlinCompile> {
        onlyIf { false }
    }
    tasks.withType<AbstractArchiveTask> {
        onlyIf { false }
    }
    tasks.withType<ProcessResources> {
        onlyIf { false }
    }
}

preprocess {
    vars.put("MC", target.minecraftVersion.versionNumber)
    vars.put("FORGE", if (target.isForge) 1 else 0)
    vars.put("FABRIC", if (target.isFabric) 1 else 0)
    vars.put("JAVA", target.minecraftVersion.javaVersion)
    patternAnnotation.set("at.hannibal2.skyhanni.utils.compat.Pattern")
}

blossom {
    replaceToken("@MOD_VERSION@", version)
    replaceToken("@MC_VERSION@", target.minecraftVersion.versionName)
}

val sourcesJar by tasks.creating(Jar::class) {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("src")
    from(sourceSets.main.get().allSource)
}

publishing.publications {
    create<MavenPublication>("maven") {
        artifact(tasks.remapJar)
        artifact(sourcesJar) { classifier = "sources" }
        pom {
            name.set("SkyHanni")
            licenses {
                license {
                    name.set("GNU Lesser General Public License")
                    url.set("https://github.com/hannibal002/SkyHanni/blob/HEAD/LICENSE")
                }
            }
            developers {
                developer { name.set("hannibal002") }
                developer { name.set("The SkyHanni contributors") }
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    config.setFrom(rootProject.layout.projectDirectory.file("detekt/detekt.yml")) // point to your custom config defining rules to run, overwriting default behavior
    baseline = file(layout.projectDirectory.file("detekt/baseline.xml")) // a way of suppressing issues before introducing detekt
    source.setFrom(project.sourceSets.named("main").map { it.allSource })
}

tasks.withType<Detekt>().configureEach {
    onlyIf {
        target == ProjectTarget.MAIN && project.findProperty("skipDetekt") != "true"
    }
    jvmTarget = target.minecraftVersion.formattedJavaLanguageVersion
    outputs.cacheIf { false } // Custom rules won't work if cached

    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
        md.required.set(true) // simple Markdown format
    }
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = target.minecraftVersion.formattedJavaLanguageVersion
    outputs.cacheIf { false } // Custom rules won't work if cached
}

abstract class ShotApplicationJarProcessor @Inject constructor(private val shots: Shots) :
    MinecraftJarProcessor<MinecraftJarProcessor.Spec>,
    Serializable {

    override fun buildSpec(context: SpecContext?): MinecraftJarProcessor.Spec? = ShotSpec(shots)

    override fun processJar(source: Path, spec: MinecraftJarProcessor.Spec?, context: ProcessorContext?) {
        val dest = source.resolveSibling(source.fileName.toString() + "-temp-shot")
        ZipFile(source.toFile()).use { input ->
            ZipOutputStream(dest.outputStream()).use { output ->
                shots.processZipFile(input, output)
            }
        }
        dest.moveTo(source, overwrite = true)
    }

    override fun getName(): String = "Shots"

    private data class ShotSpec(val shots: Shots) : MinecraftJarProcessor.Spec, Serializable
}
