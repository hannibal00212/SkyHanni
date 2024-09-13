import at.skyhanni.sharedvariables.MinecraftVersion
import at.skyhanni.sharedvariables.MultiVersionStage
import at.skyhanni.sharedvariables.ProjectTarget
import at.skyhanni.sharedvariables.SHVersionInfo
import at.skyhanni.sharedvariables.versionString
import net.fabricmc.loom.task.RunGameTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("gg.essential.loom")
    id("dev.deftu.gradle.preprocess")
    kotlin("jvm")
    id("com.google.devtools.ksp")
    kotlin("plugin.power-assert")
    `maven-publish`
    id("moe.nea.shot") version "1.0.0"
}

val target = ProjectTarget.values().find { it.projectPath == project.path }!!

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.minecraftforge.net") {
        metadataSources {
            artifact() // We love missing POMs
        }
    }
    maven("https://repo.spongepowered.org/maven/") // mixin
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") // DevAuth
    maven("https://jitpack.io") { // NotEnoughUpdates (compiled against)
        content {
            includeGroupByRegex("(com|io)\\.github\\..*")
        }
    }
    maven("https://repo.nea.moe/releases") // libautoupdate
    maven("https://maven.notenoughupdates.org/releases") // NotEnoughUpdates (dev env)
    maven("https://repo.hypixel.net/repository/Hypixel/") // mod-api
    maven("https://maven.teamresourceful.com/repository/thatgravyboat/") // DiscordIPC
}

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
    if (this.isForgeLike)
        forge {
            pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
            mixinConfig("mixins.skyhanni.json")
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

if (target == ProjectTarget.MAIN)
    sourceSets.main {
        resources.destinationDirectory.set(kotlin.destinationDirectory)
        output.setResourcesDir(kotlin.destinationDirectory)
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
tasks.runClient {
    this.javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(target.minecraftVersion.javaLanguageVersion)
    })
}
val shot = shots.shot("minecraft", rootProject.file("shots.txt"))

dependencies {
    minecraft("com.mojang:minecraft:${target.minecraftVersion.versionName}")
    if (target.mappingDependency == "official") {
        mappings(loom.officialMojangMappings())
    } else {
        mappings(target.mappingDependency)
    }
    if (target.forgeDep != null)
        "forge"(target.forgeDep!!)

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
    }

    implementation(kotlin("stdlib-jdk8"))
    shadowImpl("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") {
        exclude(group = "org.jetbrains.kotlin")
    }

    modRuntimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.1.0")

    modCompileOnly("com.github.hannibal002:notenoughupdates:4957f0b:all") {
        exclude(module = "unspecified")
        isTransitive = false
    }
    // June 3, 2024, 9:30 PM AEST
    // https://github.com/NotEnoughUpdates/NotEnoughUpdates/tree/2.3.0
    devenvMod("com.github.NotEnoughUpdates:NotEnoughUpdates:2.3.0:all") {
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
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.12.5")

    implementation("net.hypixel:mod-api:0.3.1")
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

if (target == ProjectTarget.MAIN)
    tasks.compileJava {
        dependsOn(tasks.processResources)
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
    vars.put("FORGE", if (target.forgeDep != null) 1 else 0)
    vars.put("JAVA", target.minecraftVersion.javaVersion)
    patternAnnotation.set("at.hannibal2.skyhanni.utils.compat.Pattern")
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
