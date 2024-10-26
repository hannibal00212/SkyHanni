package at.hannibal2.skyhanni.utils.repopatterns

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.config.features.dev.RepoPatternConfig
import at.hannibal2.skyhanni.data.repo.RepoManager
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.LorenzEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.utils.PreInitFinishedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ConditionalUtils.afterChange
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.StringUtils.substringBeforeLastOrNull
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.NavigableMap
import java.util.TreeMap
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Manages [RepoPattern]s.
 */
@SkyHanniModule
object RepoPatternManager {

    val allPatterns: Collection<CommonPatternInfo<*, *>> get() = usedKeys.values

    /**
     * Remote loading data that will be used to compile regexes from, once such a regex is needed.
     */
    private var regexes: RepoPatternDump? = null

    /**
     * [regexes] but as a NavigableMap. (Creates the Map at call)
     */
    private val remotePattern: NavigableMap<String, String>
        get() = TreeMap(
            if (localLoading) mapOf()
            else regexes?.regexes.orEmpty()
        )

    /**
     * Map containing the exclusive owner of a regex key
     */
    private var exclusivity: MutableMap<String, RepoPatternKeyOwner> = mutableMapOf()

    /**
     * Map containing all keys and their repo patterns. Used for filling in new regexes after an update, and for
     * checking duplicate registrations.
     */
    private var usedKeys: NavigableMap<String, CommonPatternInfo<*, *>> = TreeMap()

    private var wasPreInitialized = false
    private val isInDevEnv = try {
        Launch.blackboard["fml.deobfuscatedEnvironment"] as Boolean
    } catch (_: Exception) {
        true
    }

    private val insideTest = Launch.blackboard == null

    var inTestDuplicateUsage = true

    private val config
        get() = if (!insideTest) {
            SkyHanniMod.feature.dev.repoPattern
        } else {
            RepoPatternConfig().apply {
                tolerateDuplicateUsage = inTestDuplicateUsage
            }
        }

    private val localLoading: Boolean
        get() = config.forceLocal.get() || (!insideTest && PlatformUtils.isDevEnvironment) || RepoManager.usingBackupRepo

    private val logger = LogManager.getLogger("SkyHanni")

    /**
     * Crash if in a development environment, or if inside a guarded event handler.
     */
    fun crash(reason: String) {
        if (isInDevEnv || LorenzEvent.isInGuardedEventHandler)
            throw RuntimeException(reason)
    }

    /**
     * Check that the [owner] has exclusive right to the specified [key], and locks out other code parts from ever
     * using that [key] again. Thread safe.
     */
    fun checkExclusivity(owner: RepoPatternKeyOwner, key: String) {
        val parentKeyHolder = owner.parent
        synchronized(exclusivity) {
            run {
                val previousOwner = exclusivity[key]
                if (previousOwner != owner && previousOwner != null && !previousOwner.transient) {
                    if (!config.tolerateDuplicateUsage)
                        crash(
                            "Non unique access to regex at \"$key\". " +
                                "First obtained by ${previousOwner.ownerClass} / ${previousOwner.property}, " +
                                "tried to use at ${owner.ownerClass} / ${owner.property}"
                        )
                } else {
                    exclusivity[key] = owner
                }
            }
            run {
                val transient = owner.copy(shares = true, transient = true)
                var parent = key
                var previousParentOwnerMutable: RepoPatternKeyOwner? = null
                while (previousParentOwnerMutable == null && parent.isNotEmpty()) {
                    parent = parent.substringBeforeLastOrNull(".") ?: return
                    previousParentOwnerMutable = exclusivity[parent]
                    previousParentOwnerMutable ?: run {
                        exclusivity[parent] = transient
                    }
                }
                val previousParentOwner = previousParentOwnerMutable

                if (previousParentOwner != null && previousParentOwner != parentKeyHolder &&
                    !(previousParentOwner.shares && previousParentOwner.parent == parentKeyHolder)
                ) {
                    if (!config.tolerateDuplicateUsage) crash(
                        "Non unique access to array regex at \"$parent\"." +
                            " First obtained by ${previousParentOwner.ownerClass} / ${previousParentOwner.property}," +
                            " tried to use at ${owner.ownerClass} / ${owner.property}" +
                            if (parentKeyHolder != null) "with parentKeyHolder ${parentKeyHolder.ownerClass} / ${parentKeyHolder.property}"
                            else "",
                    )
                }
            }

        }
    }

    /**
     * Check that the [owner] has exclusive right to the specified namespace and locks out other code parts from ever
     * using that [key] prefix again without permission of the [owner]. Thread safe.
     */
    fun checkNameSpaceExclusivity(owner: RepoPatternKeyOwner, key: String) {
        synchronized(exclusivity) {
            val preRegistered = exclusivity[key]
            if (preRegistered != null) {
                if (!config.tolerateDuplicateUsage) crash(
                    "Non unique access to array regex at \"$key\"." +
                        " First obtained by ${preRegistered.ownerClass} / ${preRegistered.property}," +
                        " tried to use at ${owner.ownerClass} / ${owner.property}",
                )
            }
        }
        checkExclusivity(owner, key)
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        loadPatternsFromDump(event.getConstant<RepoPatternDump>("regexes"))
    }

    fun loadPatternsFromDump(dump: RepoPatternDump) {
        regexes = null
        regexes = dump
        reloadPatterns()
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.forceLocal.afterChange { reloadPatterns() }
    }

    /**
     * Reload patterns in [usedKeys] from [regexes] or their fallbacks.
     */
    private fun reloadPatterns() {
        val remotePatterns = remotePattern
        for (it in usedKeys.values) {
            when (it) {
                is RepoPatternListImpl -> loadArrayPatterns(remotePatterns, it)
                is RepoPatternImpl -> loadStandalonePattern(remotePatterns, it)
            }

        }
    }

    private fun loadStandalonePattern(remotePatterns: NavigableMap<String, String>, it: RepoPatternImpl) {
        val remotePattern = remotePatterns[it.key]
        try {
            if (remotePattern != null) {
                it.value = Pattern.compile(remotePattern)
                it.isLoadedRemotely = true
                it.wasOverridden = remotePattern != it.defaultPattern
                return
            }
        } catch (e: PatternSyntaxException) {
            logger.error("Error while loading pattern from repo", e)
        }
        it.value = Pattern.compile(it.defaultPattern)
        it.isLoadedRemotely = false
        it.wasOverridden = false
    }

    private fun loadArrayPatterns(remotePatterns: NavigableMap<String, String>, arrayPattern: RepoPatternListImpl) {
        val prefix = arrayPattern.key + "."
        val remotePatternList = StringUtils.subMapOfStringsStartingWith(prefix, remotePatterns)
        val patternMap = remotePatternList.mapNotNull {
            val index = it.key.removePrefix(prefix).toIntOrNull()
            if (index == null) null
            else index to it.value
        }

        fun setDefaultPatterns() {
            arrayPattern.value = arrayPattern.defaultPattern.map(Pattern::compile)
            arrayPattern.isLoadedRemotely = false
            arrayPattern.wasOverridden = false
        }

        if (localLoading) {
            setDefaultPatterns()
            return
        }

        if (patternMap.mapTo(mutableSetOf()) { it.first } != patternMap.indices.toSet()) {
            logger.error("Incorrect index set for $arrayPattern")
            setDefaultPatterns()
        }

        val patternStrings = patternMap.sortedBy { it.first }.map { it.second }
        try {
            arrayPattern.value = patternStrings.map(Pattern::compile)
            arrayPattern.isLoadedRemotely = true
            arrayPattern.wasOverridden = patternStrings != arrayPattern.defaultPattern
            return
        } catch (e: PatternSyntaxException) {
            logger.error("Error while loading pattern from repo", e)
        }
        setDefaultPatterns()
    }

    private val keyShape = Pattern.compile("^(?:[a-z0-9]+\\.)*[a-z0-9]+$")

    /**
     * Verify that a key has a valid shape or throw otherwise.
     */
    fun verifyKeyShape(key: String) {
        require(keyShape.matches(key)) {
            "pattern key: \"$key\" failed shape requirements. Make sure your key only includes lowercase letters, numbers and dots."
        }
    }

    /**
     * Dump all regexes labeled with the label into the file.
     */
    fun dump(sourceLabel: String, file: File) {
        val data =
            ConfigManager.gson.toJson(
                RepoPatternDump(
                    sourceLabel,
                    usedKeys.values.flatMap { it.dump().toList() }.toMap(),
                ),
            )
        file.parentFile.mkdirs()
        file.writeText(data)
    }

    @HandleEvent
    fun onPreInitFinished(event: PreInitFinishedEvent) {
        wasPreInitialized = true
        val dumpDirective = System.getenv("SKYHANNI_DUMP_REGEXES")
        if (dumpDirective.isNullOrBlank()) return
        val (sourceLabel, path) = dumpDirective.split(":", limit = 2)
        dump(sourceLabel, File(path))
        if (System.getenv("SKYHANNI_DUMP_REGEXES_EXIT") != null) {
            logger.info("Exiting after dumping RepoPattern regex patterns to $path")
            FMLCommonHandler.instance().exitJava(0, false)
        }
    }

    fun of(key: String, fallback: String, parentKeyHolder: RepoPatternKeyOwner? = null): RepoPattern {
        verifyKeyShape(key)
        if (wasPreInitialized && !config.tolerateLateRegistration) {
            crash("Illegal late initialization of repo pattern. Repo pattern needs to be created during pre-initialization.")
        }
        if (key in usedKeys) {
            usedKeys[key]?.hasObtainedLock = false
        }
        return RepoPatternImpl(fallback, key, parentKeyHolder).also { usedKeys[key] = it }
    }

    fun ofList(
        key: String,
        fallbacks: Array<out String>,
        parentKeyHolder: RepoPatternKeyOwner? = null,
    ): RepoPatternList {
        verifyKeyShape(key)
        if (wasPreInitialized && !config.tolerateLateRegistration) {
            crash("Illegal late initialization of repo pattern. Repo pattern needs to be created during pre-initialization.")
        }
        if (key in usedKeys) {
            usedKeys[key]?.hasObtainedLock = false
        }
        StringUtils.subMapOfStringsStartingWith(key, usedKeys).forEach {
            it.value.hasObtainedLock = false
        }
        return RepoPatternListImpl(fallbacks.toList(), key, parentKeyHolder).also { usedKeys[key] = it }

    }

    /**
     * The caller must ensure the exclusivity to the [prefix]!
     *
     * @param prefix the prefix to search without the dot at the end (the match includes the .)
     * @return returns any pattern on the [prefix] key space (including list or any other complex structure, but as a simple pattern
     * */
    internal fun getUnusedPatterns(prefix: String): List<Pattern> {
        if (localLoading) return emptyList()
        try {
            verifyKeyShape(prefix)
        } catch (e: IllegalArgumentException) {
            ErrorManager.logErrorWithData(e, "getUnusedPatterns failed do to invalid key shape", "prefix" to prefix)
            return emptyList()
        }
        val prefixWithDot = "$prefix."
        val patterns = StringUtils.subMapOfStringsStartingWith(prefixWithDot, remotePattern)
        val holders = StringUtils.subMapOfStringsStartingWith(prefixWithDot, usedKeys)

        val noShareHolder = holders.filter { !it.value.shares }.map { it.key.removePrefix(prefixWithDot) }
            .groupBy { it.count { it == '.' } }

        return patterns.filter { it.key !in holders.keys }.filter { unused ->
            val dot = unused.key.count { it == '.' }
            val possibleConflicts = noShareHolder.filter { it.key < dot }.flatMap { it.value }.toSet()
            var key: String = unused.key.removePrefix(prefixWithDot)
            while (key.isNotEmpty()) {
                if (possibleConflicts.contains(key)) return@filter false
                key = key.substringBeforeLastOrNull(".") ?: return@filter true
            }
            true
        }.map { it.value.toPattern() }
    }

}
