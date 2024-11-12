package at.hannibal2.skyhanni.features.event.hoppity

import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType.Companion.getEggType
import at.hannibal2.skyhanni.features.inventory.chocolatefactory.ChocolateFactoryAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object HoppityEggsShared {

    private val config get() = HoppityEggsManager.config

    /**
     * REGEX-TEST: CalMWolfs: [SkyHanni] Breakfast Chocolate Egg located at x: 142, y: 71, z: -453
     * REGEX-TEST: CalMWolfs: [SkyHanni] Breakfast Chocolate Egg located at x: 142, y: 71, z: -453 (hidden note)
     */
    private val sharedEggPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "egg.shared",
        ".*\\[SkyHanni] (?<meal>\\w+) Chocolate Egg located at x: (?<x>-?\\d+), y: (?<y>-?\\d+), z: (?<z>-?\\d+)(?: \\((?<note>.*)\\))?"
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        sharedEggPattern.matchMatcher(event.message.removeColor()) {
            val (x, y, z) = listOf(group("x"), group("y"), group("z")).map { it.formatInt() }
            val eggLocation = LorenzVec(x, y, z)

            val meal = getEggType(event)
            val note = groupOrNull("note")

            if (meal.isClaimed()) return
            if (!HoppityEggLocator.isValidEggLocation(eggLocation)) return

            HoppityEggLocator.sharedEggLocation = eggLocation
            HoppityEggLocator.currentEggType = meal
            HoppityEggLocator.currentEggNote = note
        }
    }

    fun shareNearbyEggLocation(playerLocation: LorenzVec, meal: HoppityEggType, note: String) {
        if (!isEnabled()) return
        val islandEggsLocations = HoppityEggLocations.islandLocations
        val closestEgg = islandEggsLocations.minByOrNull { it.distance(playerLocation) } ?: return

        val x = closestEgg.x.toInt()
        val y = closestEgg.y.toInt()
        val z = closestEgg.z.toInt()

        HypixelCommands.allChat("[SkyHanni] ${meal.mealName} Chocolate Egg located at x: $x, y: $y, z: $z ($note)")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.waypoints && config.sharedWaypoints
}
