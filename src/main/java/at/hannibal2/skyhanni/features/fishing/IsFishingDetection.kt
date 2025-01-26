package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.fishing.FishingBobberInLiquidEvent
import at.hannibal2.skyhanni.events.minecraft.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.entity.item.EntityArmorStand
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object IsFishingDetection {

    var isFishing = false
    private var lastRodCastLocation: LorenzVec? = null
    private var lastRodCastTime = SimpleTimeMark.farPast()
    private var lastInAreaTime = SimpleTimeMark.farPast()

    private var lastSeaCreatureKillArea: LorenzVec? = null
    private var lastSeaCreatureKillAreaTime = SimpleTimeMark.farPast()

    @HandleEvent
    fun onBobber(event: FishingBobberInLiquidEvent) {
        lastRodCastLocation = LocationUtils.playerLocation()
        lastRodCastTime = SimpleTimeMark.now()
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTick(event: SkyHanniTickEvent) {
        if (inRodCastArea()) {
            lastInAreaTime = SimpleTimeMark.now()
        }

        if (lastInAreaTime.passedSince() < 5.seconds) {
            if (EntityUtils.getEntitiesNextToPlayer<EntityArmorStand>(5.0)
                    .filter { FishingApi.seaCreatureCount(it) > 0 }.any()
            ) {
                lastSeaCreatureKillArea = LocationUtils.playerLocation()
                lastSeaCreatureKillAreaTime = SimpleTimeMark.now()
            }
        }

        isFishing = testIsFishing()
    }

    private fun testIsFishing(): Boolean {
        if (inRodCastArea()) return true

        if (lastRodCastTime.passedSince() < 5.seconds) return true

        if (inKillArea()) return true


        return false
    }

    private fun inRodCastArea(): Boolean {
        if (lastRodCastTime.passedSince() < 2.minutes) {
            lastRodCastLocation?.let {
                if (it.distanceToPlayer() < 10) {
                    return true
                }
            }
        }

        return false
    }

    private fun inKillArea(): Boolean {
        if (lastSeaCreatureKillAreaTime.passedSince() < 2.minutes) {
            lastSeaCreatureKillArea?.let {
                if (it.distanceToPlayer() < 10) {
                    return true
                }
            }
        }

        return false
    }
}
