package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ComputerEnvDebug {

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        os(event)
        java(event)
        launcher(event)
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
}
