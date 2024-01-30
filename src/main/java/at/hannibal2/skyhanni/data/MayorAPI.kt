package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.data.Mayors.Companion.getMayorFromName
import at.hannibal2.skyhanni.data.jsonobjects.local.MayorJson
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.utils.APIUtil
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.put
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.asTimeMark
import io.github.moulberry.notenoughupdates.util.SkyBlockTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object MayorAPI {
    private var lastUpdate = SimpleTimeMark.farPast()
    private var dispatcher = Dispatchers.IO

    private var rawMayorData: MayorJson? = null
    var candidates = mapOf<Int, MayorJson.Candidate>()
        private set
    var currentMayor: Mayors? = null
        private set
    var timeTillNextMayor = Duration.ZERO
        private set

    private const val LATE_SPRING = 3

    fun isPerkActive(perk: Perks) = currentMayor?.perk?.contains(perk) ?: false

    /**
     * @param input: The name of the mayor
     * @return: The neu color of the mayor; If no mayor was found, it will return "§cUnknown: §7"
     */
    fun mayorNameToColorCode(input: String): String {
        return Mayors.getMayorFromName(input).color
    }

    /**
     * @param input: The name of the mayor
     * @return: The neu color of the mayor + the name of the mayor; If no mayor was found, it will return "§cUnknown: §7[input]"
     */
    fun mayorNameWithColorCode(input: String) = mayorNameToColorCode(input) + input

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.onHypixel) return

        if (event.repeatSeconds(3)) {
            checkHypixelAPI()
            getTimeTillNextMayor()
        }
    }

    private fun calculateNextMayorTime(): SimpleTimeMark {
        var currentYear = SkyBlockTime.now().year

        // Check if either the month is already over or the day is after 27th in the third month
        if (SkyBlockTime.now().month > LATE_SPRING || (SkyBlockTime.now().day >= 27 && SkyBlockTime.now().month == LATE_SPRING)) {
            // If so, the next mayor will be in the next year
            currentYear++
        }

        return SkyBlockTime(currentYear, LATE_SPRING, day = 27).asTimeMark()
    }

    private fun getTimeTillNextMayor() {
        val nextMayorTime = calculateNextMayorTime()
        timeTillNextMayor = nextMayorTime - SimpleTimeMark.now()
    }

    private fun checkCurrentMayor() {
        val nextMayorTime = calculateNextMayorTime()

        // Check if it is still the mayor from the old SkyBlock year
        currentMayor = (if (nextMayorTime > System.currentTimeMillis().asTimeMark()) {
            candidates[SkyBlockTime.now().year - 1]
        } else {
            candidates[SkyBlockTime.now().year]
        })?.let {
            getMayorFromName(
                it.name
            )
        }
    }

    private fun checkHypixelAPI() {
        if (lastUpdate.passedSince() < 20.minutes) return
        lastUpdate = SimpleTimeMark.now()

        SkyHanniMod.coroutineScope.launch {
            val url = "https://api.hypixel.net/v2/resources/skyblock/election"
            val jsonObject = withContext(dispatcher) { APIUtil.getJSONResponse(url) }
            rawMayorData = ConfigManager.gson.fromJson(jsonObject, MayorJson::class.java)
            val data = rawMayorData ?: return@launch
            val map = mutableMapOf<Int, MayorJson.Candidate>()
            map put data.mayor.election.getPairs()
            data.current?.let {
                map put data.current.getPairs()
            }
            candidates = map
        }

        checkCurrentMayor()
    }

    private fun MayorJson.Election.getPairs() = year + 1 to candidates.bestCandidate()

    private fun List<MayorJson.Candidate>.bestCandidate() = maxBy { it.votes }
}
