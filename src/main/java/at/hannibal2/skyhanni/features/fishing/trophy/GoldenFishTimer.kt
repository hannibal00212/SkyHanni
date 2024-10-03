package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.TitleManager
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.EntityMaxHealthUpdateEvent
import at.hannibal2.skyhanni.events.FishingBobberCastEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.features.fishing.FishingAPI.isLavaRod
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullTexture
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GoldenFishTimer {

    private val config get() = SkyHanniMod.feature.fishing.trophyFishing.goldenFishTimer

    private val patternGroup = RepoPattern.group("fishing.goldenfish")
    private val spawnPattern by patternGroup.pattern(
        "spawn",
        "§9You spot a §r§6Golden Fish §r§9surface from beneath the lava!",
    )
    private val interactPattern by patternGroup.pattern(
        "interact",
        "§9The §r§6Golden Fish §r§9escapes your hook but looks weakened\\.",
    )
    private val weakPattern by patternGroup.pattern(
        "weak",
        "§9The §r§6Golden Fish §r§9is weak!",
    )
    private val despawnPattern by patternGroup.pattern(
        "despawn",
        "§9The §r§6Golden Fish §r§9swims back beneath the lava\\.\\.\\.",
    )

    private val timeOut = 10.seconds
    private val despawnTime = 1.minutes
    private val maxRodTime = 3.minutes
    private val minimumSpawnTime = 15.minutes
    private const val MAX_INTERACTIONS = 3

    private var lastFishEntity = SimpleTimeMark.farPast()
    private var lastChatMessage = SimpleTimeMark.farPast()

    private var lastGoldenFishTime = SimpleTimeMark.farPast()

    private var lastRodThrowTime = SimpleTimeMark.farPast()
    private var goldenFishDespawnTimer = SimpleTimeMark.farFuture()
    private var timePossibleSpawn = SimpleTimeMark.farFuture()

    private val isFishing get() = FishingAPI.isFishing() || lastRodThrowTime.passedSince() < maxRodTime
    private var hasLavaRodInInventory = false

    private fun checkGoldenFish(entity: EntityLivingBase) {
        if (entity.inventory.none { it?.getSkullTexture() == GOLDEN_FISH_SKULL_TEXTURE }) return
        possibleGoldenFishEntity = entity
        lastFishEntity = SimpleTimeMark.now()
        handle()
    }

    private const val GOLDEN_FISH_SKULL_TEXTURE =
        "ewogICJ0aW1lc3RhbXAiIDogMTY0MzgzMTA2MDE5OCwKICAicHJvZmlsZUlkIiA6ICJiN2ZkYmU2N2NkMDA0NjgzYjlmYTllM2UxNzczODI1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzEyMGNmM2MwYTQwZmM2N2UwZTVmZTBjNDZiMGFlNDA5YWM3MTAzMGE3NjU2ZGExN2IxMWVkMDAxNjQ1ODg4ZmUiCiAgICB9CiAgfQp9"
    private val goldenFishSkullItem by lazy {
        ItemUtils.createSkull(
            displayName = "§6Golden Fish",
            uuid = "b7fdbe67cd004683b9fa9e3e17738254",
            value = GOLDEN_FISH_SKULL_TEXTURE,
        )
    }
    private var interactions = 0
    private var goingDownInit = true
    private var goingDownPost = false
    private var hasWarnedRod = false

    private var possibleGoldenFishEntity: EntityLivingBase? = null
    private var confirmedGoldenFishEntity: EntityLivingBase? = null

    private var display = listOf<Renderable>()

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isActive()) return
        if (spawnPattern.matches(event.message)) {
            lastChatMessage = SimpleTimeMark.now()
            handle()
            return
        }
        if (interactPattern.matches(event.message)) {
            goldenFishDespawnTimer = SimpleTimeMark.now() + despawnTime
            interactions++
            return
        }
        if (weakPattern.matches(event.message)) {
            goldenFishDespawnTimer = SimpleTimeMark.now() + despawnTime
            val entity = confirmedGoldenFishEntity ?: return
            if (config.highlight) RenderLivingEntityHelper.setEntityColorWithNoHurtTime(
                entity,
                LorenzColor.GREEN.toColor().withAlpha(100)
            ) { true }
            return
        }
        if (despawnPattern.matches(event.message)) {
            timePossibleSpawn = SimpleTimeMark.now() + minimumSpawnTime
            removeGoldenFish()
            return
        }
        TrophyFishMessages.trophyFishPattern.matchMatcher(event.message) {
            val internalName = TrophyFishMessages.getInternalName(group("displayName"))
            if (internalName != "goldenfish") return@matchMatcher
            timePossibleSpawn = SimpleTimeMark.now() + minimumSpawnTime
            removeGoldenFish()
            return
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isActive()) return
        if (!config.nametag) return
        val entity = confirmedGoldenFishEntity ?: return

        val location = event.exactLocation(entity).add(y = 2.5)
        if (location.distanceToPlayer() > 20) return
        event.drawString(location.add(y = 0.5), "§b${(goldenFishDespawnTimer + 1.seconds).timeUntil().format()}", false)
        if (interactions >= MAX_INTERACTIONS) event.drawString(location.add(y = 0.25), "§cPULL", false)
        event.drawString(location, "§6Golden Fish §a($interactions/$MAX_INTERACTIONS)", false)
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isActive()) return
        val list = display.takeIf { it.isNotEmpty() } ?: return
        val renderable = Renderable.horizontalContainer(list, verticalAlign = RenderUtils.VerticalAlignment.CENTER)
        config.position.renderRenderables(listOf(renderable), posLabel = "Golden Fish Timer")
    }

    private fun updateDisplay() {
        display = drawDisplay()
    }

    private fun drawDisplay() = buildList {
        if (config.showHead) add(
            Renderable.itemStack(
                goldenFishSkullItem,
                2.5,
                verticalAlign = RenderUtils.VerticalAlignment.CENTER,
            ),
        )
        val text = buildList {
            add("§6§lGolden Fish Timer")
            if (!isGoldenFishActive()) {
                if (lastGoldenFishTime.isFarPast()) add("§7Last Golden Fish: §cNone this session")
                else add("§7Last Golden Fish: §b${lastGoldenFishTime.passedSince().formatTime()}")
                if (lastRodThrowTime.isFarPast()) add("§7Last Row Throw: §cNone yet")
                else add(
                    "§7Last Rod Throw: §b${lastRodThrowTime.passedSince().formatTime()} " +
                        "§3(${(lastRodThrowTime + maxRodTime + 1.seconds).timeUntil().formatTime()})",
                )
                if (timePossibleSpawn.isFarFuture()) add("§7Can spawn in: §cUnknown")
                else if (timePossibleSpawn.isInFuture()) add(
                    "§7Can spawn in: §b${
                        (timePossibleSpawn + 1.seconds).timeUntil().formatTime()
                    }",
                )
                else {
                    add("§7Can spawn since: §b${timePossibleSpawn.passedSince().formatTime()}")
                    val chance = timePossibleSpawn.passedSince().inWholeSeconds.toDouble() / 5.minutes.inWholeSeconds
                    add("§7Chance: §b${LorenzUtils.formatPercentage(chance.coerceAtMost(1.0))}")
                }
            } else {
                add("§7Interactions: §b$interactions/$MAX_INTERACTIONS")
                add("§7Despawn in: §b${(goldenFishDespawnTimer + 1.seconds).timeUntil().formatTime()}")
            }
        }

        add(
            Renderable.verticalContainer(
                text.map { Renderable.string(it) },
                spacing = 1,
                verticalAlign = RenderUtils.VerticalAlignment.CENTER,
            ),
        )
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        hasLavaRodInInventory = InventoryUtils.containsInLowerInventory { it.getInternalNameOrNull()?.isLavaRod() == true }

        if (!isActive()) return

        if (lastRodThrowTime.passedSince() > maxRodTime) {
            timePossibleSpawn = SimpleTimeMark.farFuture()
            lastRodThrowTime = SimpleTimeMark.farPast()
        }
        if (!lastRodThrowTime.isFarPast() && (lastRodThrowTime + maxRodTime).timeUntil() < config.throwRodWarningTime.seconds) {
            rodWarning()
        }

        updateDisplay()
    }

    private fun rodWarning() {
        if (!config.throwRodWarning || hasWarnedRod) return
        hasWarnedRod = true
        TitleManager.sendTitle("§cThrow your rod!", 5.seconds, 3.6, 7.0f)
        SoundUtils.repeatSound(100, 10, SoundUtils.plingSound)
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isActive()) return
        // This makes it only count as the rod being throw into lava if the rod goes down, up, and down again.
        // Not confirmed that this is correct, but it's the best solution found.
        val bobber = FishingAPI.bobber ?: return
        if (!bobber.isInLava || bobber.ticksExisted < 5) return
        if (bobber.motionY > 0 && goingDownInit) goingDownInit = false
        else if (bobber.motionY < 0 && !goingDownInit && !goingDownPost) {
            hasWarnedRod = false
            goingDownPost = true
            lastRodThrowTime = SimpleTimeMark.now()
            if (timePossibleSpawn.isFarFuture()) timePossibleSpawn = SimpleTimeMark.now() + minimumSpawnTime
        }
    }

    @SubscribeEvent
    fun onBobberThrow(event: FishingBobberCastEvent) {
        if (!isActive()) return
        goingDownInit = true
        goingDownPost = false
    }

    @SubscribeEvent
    fun onEntityHealthUpdate(event: EntityMaxHealthUpdateEvent) {
        if (!isActive()) return
        if (isGoldenFishActive()) return
        val entity = event.entity as? EntityArmorStand ?: return
        entity.inventory.forEach { it?.getSkullTexture()?.let { texture -> println(texture) } }

        DelayedRun.runDelayed(1.seconds) { checkGoldenFish(entity) }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        lastChatMessage = SimpleTimeMark.farPast()
        lastFishEntity = SimpleTimeMark.farPast()
        lastGoldenFishTime = SimpleTimeMark.farPast()
        possibleGoldenFishEntity = null
        lastRodThrowTime = SimpleTimeMark.farPast()
        timePossibleSpawn = SimpleTimeMark.farFuture()
        interactions = 0
        display = listOf()
        removeGoldenFish()
    }

    @SubscribeEvent
    fun onDebugData(event: DebugDataCollectEvent) {
        event.title("Golden Fish Timer")
        if (!isEnabled()) {
            event.addIrrelevant("Not Enabled")
        } else {
            event.addIrrelevant {
                add("lastChatMessage: ${lastChatMessage.passedSince().format()}")
                add("lastFishEntity: ${lastFishEntity.passedSince().format()}")
                add("lastGoldenFishTime: ${lastGoldenFishTime.passedSince().format()}")
                add("lastRodThrowTime: ${lastRodThrowTime.passedSince().format()}")
                add("goldenFishDespawnTimer: ${goldenFishDespawnTimer.timeUntil().format()}")
                add("timePossibleSpawn: ${timePossibleSpawn.timeUntil().format()}")
                add("interactions: $interactions")
                add("goingDownInit: $goingDownInit")
                add("goingDownPost: $goingDownPost")
                add("hasWarnedRod: $hasWarnedRod")
                add("possibleGoldenFishEntity: $possibleGoldenFishEntity")
                add("confirmedGoldenFishEntity: $confirmedGoldenFishEntity")
            }
        }
    }

    private fun removeGoldenFish() {
        goldenFishDespawnTimer = SimpleTimeMark.farFuture()
        confirmedGoldenFishEntity?.let {
            confirmedGoldenFishEntity = null
            RenderLivingEntityHelper.removeEntityColor(it)
        }
    }

    private fun handle() {
        if (lastChatMessage.passedSince() > timeOut || lastFishEntity.passedSince() > timeOut) return
        lastFishEntity = SimpleTimeMark.farPast()
        lastChatMessage = SimpleTimeMark.farPast()
        lastGoldenFishTime = SimpleTimeMark.now()
        interactions = 0
        ChatUtils.debug("Found Golden Fish!")
        confirmedGoldenFishEntity = possibleGoldenFishEntity
        possibleGoldenFishEntity = null
        goldenFishDespawnTimer = SimpleTimeMark.now() + despawnTime
    }

    private fun Duration.formatTime() = format(showMilliSeconds = false, showSmallerUnits = true)

    private fun isGoldenFishActive() = confirmedGoldenFishEntity != null

    private fun isEnabled() = config.enabled && (IslandType.CRIMSON_ISLE.isInIsland() || LorenzUtils.isStrandedProfile)
    private fun isActive() = isEnabled() && isFishing && hasLavaRodInInventory

}
