package at.hannibal2.skyhanni.utils.system

import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer

/**
 * This object contains utilities for all platform specific operations.
 * i.e. operations that are specific to the mod loader or the environment the mod is running in.
 */
object PlatformUtils {

    const val MC_VERSION = "@MC_VERSION@"

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

    var validNeuInstalled = false

    fun isNeuLoaded() = validNeuInstalled

}

data class ModInstance(val id: String, val name: String, val version: String)
