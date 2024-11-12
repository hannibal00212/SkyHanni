package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.SeaCreatureFishEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.LocationUtils.distanceTo
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.getLorenzVec
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object FishingTimer {

    private val config get() = SkyHanniMod.feature.fishing.barnTimer
    private val barnLocation = LorenzVec(108, 89, -252)
    private val mobDespawnTime = mutableMapOf<Mob, SimpleTimeMark>()

    private var lastSeaCreatureFished = SimpleTimeMark.farPast()
    private var display: String? = null
    private var lastNameFished: String? = null

    private var babyMagmaSlugsToFind = 0
    private var lastMagmaSlugLocation: LorenzVec? = null
    private var lastMagmaSlugTime = SimpleTimeMark.farPast()
    private var recentBabyMagmaSlugs = TimeLimitedSet<Mob>(2.seconds)

    private var mobsToFind = 0

    private val recentMobs = TimeLimitedSet<Mob>(2.seconds)
    private val currentCap by RecalculatingValue(1.seconds) {
        when (LorenzUtils.skyBlockIsland) {
            IslandType.CRYSTAL_HOLLOWS -> 20
            IslandType.CRIMSON_ISLE -> 5
            else -> config.fishingCapAmount
        }
    }

    private var rightLocation = false
    private var currentCount = 0
    private var startTime = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        rightLocation = updateLocation()
        if (startTime.passedSince().inWholeSeconds - config.alertTime in 0..3) {
            playSound()
        }
        if (config.wormLimitAlert && IslandType.CRYSTAL_HOLLOWS.isInIsland()) {
            if (currentCount >= 20) {
                playSound()
                LorenzUtils.sendTitle("§cWORM CAP FULL!!!", 2.seconds)
            }
        } else if (config.fishingCapAlert && currentCount >= currentCap) {
            playSound()
        }
    }

    private fun playSound() = SoundUtils.repeatSound(250, 4, SoundUtils.plingSound)

    @SubscribeEvent
    fun onMobSpawn(event: MobEvent.Spawn.SkyblockMob) {
        if (!isEnabled()) return
        val mob = event.mob
        if (babyMagmaSlugsToFind != 0 && mob.name == "Baby Magma Slug") {
            recentBabyMagmaSlugs += mob
            handleBabySlugs()
            return
        }
        if (mob.name !in SeaCreatureManager.allFishingMobs) return
        recentMobs += mob
        handle()
    }

    @SubscribeEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn.SkyblockMob) {
        val mob = event.mob
        if (mob in mobDespawnTime) {
            mobDespawnTime.remove(mob)
            if (mob.name == "Magma Slug") {
                lastMagmaSlugLocation = mob.baseEntity.getLorenzVec()
                babyMagmaSlugsToFind += 3
                lastMagmaSlugTime = SimpleTimeMark.now()
                handleBabySlugs()
            }
        }
        recentMobs -= mob
        updateInfo()
    }

    @SubscribeEvent
    fun onSeaCreatureFish(event: SeaCreatureFishEvent) {
        if (!isEnabled()) return
        if (!rightLocation) return
        lastSeaCreatureFished = SimpleTimeMark.now()
        lastNameFished = event.seaCreature.name
        mobsToFind = if (event.doubleHook) 2 else 1
        handle()
    }

    private fun handle() {
        if (lastSeaCreatureFished.passedSince() > 2.seconds) return
        val name = lastNameFished ?: return
        val mobs = recentMobs.toSet().filter { it.name == name && it !in mobDespawnTime }
            .sortedBy { it.baseEntity.distanceToPlayer() }
            .take(mobsToFind)
        if (mobs.isEmpty()) return
        mobsToFind -= mobs.size
        mobs.forEach { mobDespawnTime[it] = SimpleTimeMark.now() }
        if (mobsToFind == 0) {
            recentMobs.clear()
            lastNameFished = null
        }
        updateInfo()
    }

    private fun handleBabySlugs() {
        if (lastMagmaSlugTime.passedSince() > 2.seconds) return
        if (babyMagmaSlugsToFind == 0) return
        val location = lastMagmaSlugLocation ?: return
        val slugs = recentBabyMagmaSlugs.filter { it !in mobDespawnTime }
            .sortedBy { it.baseEntity.distanceTo(location) }
            .take(babyMagmaSlugsToFind)
        if (slugs.isEmpty()) return
        babyMagmaSlugsToFind -= slugs.size
        slugs.forEach { mobDespawnTime[it] = SimpleTimeMark.now() }
        if (babyMagmaSlugsToFind == 0) {
            recentBabyMagmaSlugs.clear()
            lastMagmaSlugLocation = null
        }
        updateInfo()
    }

    @SubscribeEvent
    fun onKeyPress(event: LorenzKeyPressEvent) {
        if (!isEnabled()) return
        if (Minecraft.getMinecraft().currentScreen != null) return
        if (config.manualResetTimer.isKeyClicked()) {
            mobDespawnTime.replaceAll { _, _ ->
                SimpleTimeMark.now()
            }
        }
    }

    private fun updateInfo() {
        currentCount = mobDespawnTime.size
        startTime = mobDespawnTime.maxByOrNull { it.value.passedSince() }?.value ?: SimpleTimeMark.farPast()
        display = createDisplay()
    }

    private fun updateLocation(): Boolean {
        if (config.showAnywhere) return true

        return when (LorenzUtils.skyBlockIsland) {
            IslandType.CRYSTAL_HOLLOWS -> config.crystalHollows.get()
            IslandType.CRIMSON_ISLE -> config.crimsonIsle.get()
            IslandType.WINTER -> config.winterIsland.get()
            IslandType.HUB -> barnLocation.distanceToPlayer() < 50
            IslandType.PRIVATE_ISLAND -> config.forStranded.get() && LorenzUtils.isStrandedProfile
            else -> false
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!rightLocation) return
        if (currentCount == 0) return
        if (!FishingAPI.isFishing()) return

        display = createDisplay()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (!rightLocation) return
        if (currentCount == 0) return
        if (!FishingAPI.isFishing()) return

        val text = display ?: return
        config.pos.renderString(text, posLabel = "BarnTimer")
    }

    private fun createDisplay(): String {
        val passedSince = startTime.passedSince()
        val timeColor = if (passedSince > config.alertTime.seconds) "§c" else "§e"
        val timeFormat = passedSince.format(TimeUnit.MINUTE)
        val countColor = if (config.fishingCapAlert && currentCount >= currentCap) "§c" else "§e"
        val name = StringUtils.pluralize(currentCount, "sea creature")
        return "$timeColor$timeFormat §8($countColor$currentCount §b$name§8)"
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Barn Fishing Timer")
        event.addIrrelevant {
            "lastSeaCreatureFished: $lastSeaCreatureFished"
            "lastNameFished: $lastNameFished"
            "babyMagmaSlugsToFind: $babyMagmaSlugsToFind"
            "lastMagmaSlugLocation: $lastMagmaSlugLocation"
            "lastMagmaSlugTime: $lastMagmaSlugTime"
            "recentBabyMagmaSlugs: $recentBabyMagmaSlugs"
            "mobsToFind: $mobsToFind"
            "recentMobs: $recentMobs"
            "currentCap: $currentCap"
            "mobDespawnTime: $mobDespawnTime"
            "startTime: $startTime"
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        mobDespawnTime.clear()
        recentMobs.clear()
        babyMagmaSlugsToFind = 0
        display = null
        lastMagmaSlugLocation = null
        lastMagmaSlugTime = SimpleTimeMark.farPast()
        recentBabyMagmaSlugs.clear()
        mobsToFind = 0
        currentCount = 0
        startTime = SimpleTimeMark.farPast()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled.get()

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "fishing.barnTimer", "fishing.barnTimer.enabled")
        event.move(3, "fishing.barnTimerAlertTime", "fishing.barnTimer.alertTime")
        event.move(3, "fishing.barnTimerCrystalHollows", "fishing.barnTimer.crystalHollows")
        event.move(3, "fishing.barnTimerForStranded", "fishing.barnTimer.forStranded")
        event.move(3, "fishing.wormLimitAlert", "fishing.barnTimer.wormLimitAlert")
        event.move(3, "fishing.manualResetTimer", "fishing.barnTimer.manualResetTimer")
        event.move(3, "fishing.barnTimerPos", "fishing.barnTimer.pos")
    }
}
