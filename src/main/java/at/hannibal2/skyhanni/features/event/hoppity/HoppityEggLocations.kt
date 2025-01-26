package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.jsonobjects.repo.HoppityEggLocationsJson
import at.hannibal2.skyhanni.events.NeuProfileDataLoadedEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniRenderWorldEvent
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceSqToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.StringUtils

@SkyHanniModule
object HoppityEggLocations {

    // TODO add gui/command to show total data/missing islands
    private var collectedEggStorage: MutableMap<IslandType, MutableSet<LorenzVec>>
        get() = ChocolateFactoryApi.profileStorage?.collectedEggLocations ?: mutableMapOf()
        set(value) {
            ChocolateFactoryApi.profileStorage?.collectedEggLocations = value
        }

    var apiEggLocations: Map<IslandType, Map<String, LorenzVec>> = mapOf()

    val islandLocations
        get() = apiEggLocations[LorenzUtils.skyBlockIsland]?.values?.toSet().orEmpty()

    val islandCollectedLocations
        get() = collectedEggStorage[LorenzUtils.skyBlockIsland]?.toSet().orEmpty()

    fun getEggsIn(islandType: IslandType): Set<LorenzVec> {
        return collectedEggStorage[islandType].orEmpty()
    }

    fun hasCollectedEgg(location: LorenzVec): Boolean = islandCollectedLocations.contains(location)

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        // TODO: split Chocolate Factory and Hoppity repo data
        val data = event.getConstant<HoppityEggLocationsJson>("HoppityEggLocations")
        apiEggLocations = data.apiEggLocations
        legacyEggLocations = data.eggLocations.mapValues { it.value.toSet() }
    }

    fun saveNearestEgg() {
        val location = islandLocations.minByOrNull { it.distanceSqToPlayer() } ?: return
        if (location.distanceSqToPlayer() > 100) {
            ErrorManager.skyHanniError(
                "Player far from any known egg location!",
                "island" to LorenzUtils.skyBlockIsland,
                "distanceSqToPlayer" to location.distanceSqToPlayer(),
                "playerLocation" to LocationUtils.playerLocation(),
                "closestKnownEgg" to location,
            )
        }

        saveEggLocation(LorenzUtils.skyBlockIsland, location)
    }

    private fun saveEggLocation(island: IslandType, location: LorenzVec) {
        val locations = collectedEggStorage.getOrPut(island) { mutableSetOf() }
        locations += location
    }

    private var loadedNeuThisProfile = false

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        loadedNeuThisProfile = false
    }

    @HandleEvent
    fun onNeuProfileDataLoaded(event: NeuProfileDataLoadedEvent) {
        if (loadedNeuThisProfile || !HoppityEggsManager.config.loadFromNeuPv) return

        val rawLocations = event.getCurrentPlayerData()?.events?.easter?.rabbits?.collectedLocations ?: return
        loadedNeuThisProfile = true

        val apiCollectedLocations = rawLocations.values.flatten()

        val collectedEggsApiData = mutableMapOf<IslandType, MutableSet<LorenzVec>>()

        for ((island, locationNameToCoords) in apiEggLocations) {
            val coords = apiCollectedLocations.mapNotNull { locationNameToCoords[it] }
            collectedEggsApiData[island] = coords.toMutableSet()
        }

        val storedEggLocationCount = collectedEggStorage.values.sumOf { it.size }
        val diff = apiCollectedLocations.size - storedEggLocationCount
        if (diff <= 0) return

        val locationStr = StringUtils.pluralize(diff, "location", "locations")

        ChatUtils.clickableChat(
            message = "Click here to load $diff more collected egg $locationStr from NEU PV!",
            onClick = {
                loadApiCollectedEggs(collectedEggsApiData)
                ChatUtils.chat("Updated Hoppity egg location data!")
            },
            oneTimeClick = true
        )
    }

    private fun loadApiCollectedEggs(locations: Map<IslandType, Set<LorenzVec>>) {
        for ((island, coordinates) in locations.entries) {
            coordinates.forEach { saveEggLocation(island, it) }
        }
    }

    /* Debug logic, enabled using /shtoggleegglocationdebug */
    private var showEggLocationsDebug = false

    // to be removed - in case there are any issues with missing locations
    private var legacyEggLocations: Map<IslandType, Set<LorenzVec>> = mapOf()

    private fun toggleDebug() {
        showEggLocationsDebug = !showEggLocationsDebug
        val enabledDisabled = if (showEggLocationsDebug) "§aEnabled" else "§cDisabled"
        ChatUtils.chat("$enabledDisabled hoppity egg location debug viewer.")
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onRenderWorld(event: SkyHanniRenderWorldEvent) {
        if (!showEggLocationsDebug) return
        val legacyLocations = legacyEggLocations[LorenzUtils.skyBlockIsland] ?: return
        val apiLocations = apiEggLocations[LorenzUtils.skyBlockIsland] ?: return
        val collectedLocations = islandCollectedLocations
        for (location in legacyLocations) {
            val name = apiLocations.entries.find { it.value == location }?.key
            val isCollected = collectedLocations.contains(location)
            val color = if (isCollected) LorenzColor.GREEN else LorenzColor.RED
            val nameColorCode = (if (name != null) LorenzColor.GREEN else LorenzColor.RED).getChatColor()

            event.drawColor(location, color, false, 0.5f)
            event.drawDynamicText(location.up(0.5), "$nameColorCode$name", 1.2)
            if (location.distanceSqToPlayer() < 100) {
                event.drawDynamicText(location.up(0.5), location.toCleanString(), 1.0, yOff = 12f)
            }
        }
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shtoggleegglocationdebug") {
            description = "Shows Hoppity egg locations with their internal API names and status."
            category = CommandCategory.DEVELOPER_TEST
            callback { toggleDebug() }
        }
    }
}
