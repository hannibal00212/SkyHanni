package at.hannibal2.skyhanni.utils.system

import at.hannibal2.skyhanni.data.NotificationManager
import at.hannibal2.skyhanni.data.SkyHanniNotification
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer
import kotlin.time.Duration.Companion.INFINITE

/**
 * This object contains utilities for all platform specific operations.
 * i.e. operations that are specific to the mod loader or the environment the mod is running in.
 */
object PlatformUtils {

    private val modPackages: Map<String, ModContainer> by lazy {
        Loader.instance().modList.flatMap { mod -> mod.ownedPackages.map { it to mod } }.toMap()
    }

    val isDevEnvironment: Boolean by lazy {
        Launch.blackboard?.get("fml.deobfuscatedEnvironment") as? Boolean ?: true
    }

    fun getModFromPackage(packageName: String?): ModInstance? = modPackages[packageName]?.let {
        ModInstance(it.modId, it.name, it.version)
    }

    fun Class<*>.getModInstance(): ModInstance? = getModFromPackage(canonicalName?.substringBeforeLast('.'))

    private var validNeuInstalled = false

    fun isNeuLoaded() = validNeuInstalled

    @JvmStatic
    fun checkIfNeuIsLoaded() {
        try {
            Class.forName("io.github.moulberry.notenoughupdates.NotEnoughUpdates")
        } catch (e: Throwable) {
            return
        }

        try {
            val clazz = Class.forName("io.github.moulberry.notenoughupdates.util.ItemResolutionQuery")

            for (field in clazz.methods) {
                if (field.name == "findInternalNameByDisplayName") {
                    validNeuInstalled = true
                    return
                }
            }
        } catch (_: Throwable) {
        }

        val text = listOf(
            "§c§lOutdated NotEnoughUpdates version detected!",
            "§cWhile Skyhanni doesn't require NotEnoughUpdates to function anymore,",
            "§cif you choose to still use NotEnoughUpdates, which is recommended,",
            "§cwe require you to use a newer version of NotEnoughUpdates to ensure",
            "§ccompatibility with some of our features.",
            "§cPlease update NotEnoughUpdates",
        )
        NotificationManager.queueNotification(SkyHanniNotification(text, INFINITE, true))
    }

}

data class ModInstance(val id: String, val name: String, val version: String)
