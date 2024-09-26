import at.skyhanni.sharedvariables.ProjectTarget
import com.replaymod.gradle.preprocess.Node

plugins {
    id("dev.deftu.gradle.preprocess") version "0.6.1"
    id("net.kyori.blossom") version "1.3.2" apply false
    id("gg.essential.loom") version "1.6.+" apply false
    kotlin("jvm") version "2.0.0" apply false
    kotlin("plugin.power-assert") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.24" apply false
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("io.gitlab.arturbosch.detekt") version "1.23.7" apply false
}

allprojects {
    group = "at.hannibal2.skyhanni"
    version = "0.27.Beta.12"
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
}

preprocess {
    val nodes = mutableMapOf<ProjectTarget, Node>()
    ProjectTarget.activeVersions().forEach { target ->
        nodes[target] = createNode(target.projectName, target.minecraftVersion.versionNumber, target.mappingStyle.identifier)
        val p = project(target.projectPath)
        if (target.isForge)
            p.extra.set("loom.platform", "forge")
    }
    ProjectTarget.activeVersions().forEach { child ->
        val parent = child.linkTo ?: return@forEach
        val pNode = nodes[parent]
        if (pNode == null) {
            println("Parent target to ${child.projectName} not available in this multi version stage. Not setting parent.")
            return@forEach
        }
        val mappingFile = file("versions/mapping-${parent.projectName}-${child.projectName}.txt")
        if (mappingFile.exists()) {
            pNode.link(nodes[child]!!, mappingFile)
            println("Loading mappings from $mappingFile")
        } else {
            pNode.link(nodes[child]!!)
            println("Skipped loading mappings from $mappingFile")
        }
    }
}
