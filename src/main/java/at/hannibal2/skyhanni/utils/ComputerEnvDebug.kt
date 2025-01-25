package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import java.lang.management.ManagementFactory
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

@SkyHanniModule
object ComputerEnvDebug {

    @HandleEvent
    fun onDebug(event: DebugDataCollectEvent) {
        os(event)
        java(event)
        launcher(event)
        ram(event)
        uptime(event)
    }

    private fun launcher(event: DebugDataCollectEvent) {
        event.title("Computer Minecraft Launcher")

        val firstStack = getFirstStack() ?: run {
            event.addData("Could not load data!")
            return
        }

        val (launcher, relevant) = findLauncher(firstStack)

        launcher?.let {
            if (relevant) {
                event.addData(it)
            } else {
                event.addIrrelevant(it)
            }
            return
        }

        event.addData {
            add("Unknown launcher!")
            val launcherBrand = System.getProperty("minecraft.launcher.brand")
            add("System property of 'minecraft.launcher.brand': '$launcherBrand'")
            add("firstStack: '$firstStack'")
        }
    }

    // TODO put into repo
    private fun findLauncher(firstStack: String): Pair<String?, Boolean> {
        if (firstStack.contains("net.fabricmc.devlaunchinjector.Main.main")) {
            return Pair("Dev Env", false)
        }
        if (firstStack.contains("net.minecraft.launchwrapper.Launch.main")) {
            return Pair("Vanilla Launcher", false)
        }
        if (firstStack.contains("org.prismlauncher.EntryPoint.main")) {
            return Pair("Prism", false)
        }
        if (firstStack.contains("org.multimc.EntryPoint.main")) {
            return Pair("MultiMC", false)
        }
        if (firstStack.contains("net.digitalingot.vendor.")) {
            return Pair("Feather Client", true)
        }
        return Pair(null, true)
    }

    private fun getFirstStack(): String? {
        val firstStack = try {
            Thread.currentThread().stackTrace.last().toString()
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(e, "Failed loading current thread stack trace info")
            null
        }
        return firstStack
    }

    private fun java(event: DebugDataCollectEvent) {
        event.title("Computer Java Version")
        val version = System.getProperty("java.version")
        val pattern = "1\\.8\\.0_(?<update>.*)".toPattern()
        pattern.matchMatcher(version) {
            group("update").toIntOrNull()?.let {
                val devEnvironment = PlatformUtils.isDevEnvironment
                if (it < 300 && !devEnvironment) {
                    event.addData("Old update: $it")
                } else {
                    if (devEnvironment) {
                        event.addIrrelevant("Update version: $it (dev env)")
                    } else {
                        event.addIrrelevant("New update: $it")
                    }
                }
                return
            }
        }
        event.addData("Unknwon java version: '$version'")
    }

    private fun os(event: DebugDataCollectEvent) {
        event.title("Computer Operating System")
        val osType = OSUtils.getOperatingSystem()
        val exactName = OSUtils.getOperatingSystemRaw()
        if (osType != OSUtils.OperatingSystem.UNKNOWN) {
            event.addIrrelevant {
                add("OS type: $osType")
                add("Exact name: $exactName")
            }
        } else {
            event.addData("Unknwon OS: '$exactName'")
        }
    }

    private fun ram(event: DebugDataCollectEvent) {
        event.title("Computer RAM")
        val runtime = Runtime.getRuntime()

        val text = mutableListOf<String>()

        // Retrieve memory values in bytes
        val totalMemory = runtime.totalMemory() // Total memory currently allocated to JVM
        val maxMemory = runtime.maxMemory() // Maximum memory JVM can use
        val freeMemory = runtime.freeMemory() // Free memory within currently allocated memory
        val usedMemory = totalMemory - freeMemory // Memory currently in use

        // Calculate percentages
        val allocatedPercentage = (totalMemory.toDouble() / maxMemory * 100).toInt() // Allocated percentage
        val usedPercentage = (usedMemory.toDouble() / maxMemory * 100).toInt() // Used percentage

        // Convert memory values to GB for readability
        val totalMemoryGB = totalMemory.toDouble() / (1024 * 1024 * 1024)
        val maxMemoryGB = maxMemory.toDouble() / (1024 * 1024 * 1024)
        val usedMemoryGB = usedMemory.toDouble() / (1024 * 1024 * 1024)

        // Clear the console (optional, for better readability)
        text.add("Minecraft Memory: $usedPercentage% ${usedMemoryGB.formatGB()}/${maxMemoryGB.formatGB()} GB")
        text.add("Minecraft Allocated: $allocatedPercentage% ${totalMemoryGB.formatGB()} GB")

        // Get total system memory using OS-specific APIs
        val osBean = ManagementFactory.getOperatingSystemMXBean()
        val totalPhysicalMemory = (osBean as com.sun.management.OperatingSystemMXBean).totalPhysicalMemorySize
        val freePhysicalMemory = osBean.freePhysicalMemorySize
        val usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory

        // Convert system memory to GB
        val totalPhysicalGB = totalPhysicalMemory.toDouble() / (1024 * 1024 * 1024)
        val usedPhysicalGB = usedPhysicalMemory.toDouble() / (1024 * 1024 * 1024)
        val usedPhysicalPercentage = (usedPhysicalMemory.toDouble() / totalPhysicalMemory * 100).roundToInt()

        // System Memory Usage
        text.add("System Memory: $usedPhysicalPercentage% ${usedPhysicalGB.formatGB()}/${totalPhysicalGB.formatGB()} GB")

        var important = false
        if (maxMemoryGB < 3.5) {
            text.add("")
            text.add(
                "Minecraft has less than 3.5 GB of RAM! Change this to 4-6 GB! " +
                    "(Currently at ${maxMemoryGB.formatGB()} GB RAM)",
            )
            important = true
        } else if (maxMemoryGB > 6) {
            text.add("")
            text.add(
                "Minecraft has more than 6 GB of RAM! Change this to 4-6 GB! " +
                    "(Currently at ${maxMemoryGB.formatGB()} GB RAM)",
            )
            important = true
        }
        if (usedPhysicalPercentage > 90) {
            text.add("")
            text.add(
                "The computer uses more than 90% of system memory. Maybe close background apps! " +
                    "($usedPhysicalPercentage% used)",
            )
            important = true
        }

        if (important) {
            event.addData(text)
        } else {
            event.addIrrelevant(text)
        }
    }

    private fun Double.formatGB(): String {
        return roundTo(1).addSeparators()
    }

    private fun uptime(event: DebugDataCollectEvent) {
        event.title("Minecraft Uptime")
        val uptime = getUptime()
        val info = "The game is running for ${uptime.format()}"
        if (uptime > 5.hours) {
            event.addData {
                add("The game runs for more than 5 hours, memory leaks may accumulate to dangerous levels.")
                add(info)
            }
        } else {
            event.addIrrelevant(info)
        }
    }

    private fun getUptime() = ManagementFactory.getRuntimeMXBean().uptime.milliseconds

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shuptime") {
            description = "Shows the time since the start of minecraft"
            category = CommandCategory.USERS_RESET
            callback {
                val uptime = getUptime()
                ChatUtils.chat("Minecraft is running for §b${uptime.format()}§e.")
            }
        }
    }
}
