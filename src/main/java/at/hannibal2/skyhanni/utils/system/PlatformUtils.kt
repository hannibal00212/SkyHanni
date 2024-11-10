package at.hannibal2.skyhanni.utils.system

import at.hannibal2.skyhanni.api.enoughupdates.EnoughUpdatesManager
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer

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
            EnoughUpdatesManager.downloadRepo()
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
        EnoughUpdatesManager.downloadRepo()
    }

}

data class ModInstance(val id: String, val name: String, val version: String)
