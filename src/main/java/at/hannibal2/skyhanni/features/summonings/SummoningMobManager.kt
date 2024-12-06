package at.hannibal2.skyhanni.features.summonings

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.MobEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.baseMaxHealth
import at.hannibal2.skyhanni.utils.MobUtils.mob
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SummoningMobManager {

    private val config get() = SkyHanniMod.feature.combat.summonings
    private var mobs = mutableSetOf<Mob>()

    private var lastChatTime: SimpleTimeMark = SimpleTimeMark.farPast()
    private val timeOut = 2.seconds

    private val patternGroup = RepoPattern.group("summoning.mobs")

    /**
     * REGEX-TEST: §aYou have spawned your Tank Zombie §r§asoul! §r§d(249 Mana)
     */
    private val spawnPattern by patternGroup.pattern(
        "spawn",
        "§aYou have spawned your .+ §r§asoul! §r§d\\(\\d+ Mana\\)",
    )

    /**
     * REGEX-TEST: §cYou have despawned your monster!
     * REGEX-TEST: §cYou have despawned your monsters!
     */
    private val despawnPattern by patternGroup.pattern(
        "despawn",
        "§cYou have despawned your monsters?!",
    )

    /**
     * REGEX-TEST: §cThe Seraph recalled your 3 summoned allies!
     * REGEX-TEST: §cThe Seraph recalled your 10 summoned allies!
     */
    private val seraphRecallPattern by patternGroup.pattern(
        "seraphrecall",
        "§cThe Seraph recalled your \\d+ summoned allies!",
    )

    private val despawnPatterns = listOf(
        despawnPattern,
        seraphRecallPattern,
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock || !config.summonMessages) return
        if (spawnPattern.matches(event.message)) event.blockedReason = "summoning_soul"

        if (despawnPatterns.any { it.matches(event.message) }) {
            event.blockedReason = "summoning_soul"
            lastChatTime = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onMobSpawn(event: MobEvent.Spawn.Summon) {
        if (event.mob.owner?.ownerName != LorenzUtils.getPlayerName()) return

        mobs += event.mob
        if (config.summoningMobColored) event.mob.highlight(LorenzColor.GREEN.toColor())
    }

    @SubscribeEvent
    fun onMobDeSpawn(event: MobEvent.DeSpawn.Summon) {
        val mob = event.mob
        if (mob !in mobs) return
        mobs -= mob

        // since MobEvent.DeSpawn can be fired while outside sb
        if (!LorenzUtils.inSkyBlock) return
        if (!config.summonMessages) return

        if (!mob.isInRender()) return
        DelayedRun.runNextTick {
            if (lastChatTime.passedSince() > timeOut) {
                ChatUtils.chat("Your Summoning Mob just §cdied!")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        if (!LorenzUtils.inSkyBlock || !config.summoningMobHideNametag) return
        if (event.entity.mob !in mobs) return
        event.cancel()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!LorenzUtils.inSkyBlock || !config.summoningMobDisplay) return
        if (mobs.isEmpty()) return

        val list = buildList {
            add("Summoning mobs: " + mobs.size)
            mobs.forEachIndexed { index, mob ->
                val entity = mob.baseEntity
                val health = entity.health
                val maxHealth = entity.baseMaxHealth
                val color = NumberUtil.percentageColor(health.toLong(), maxHealth.toLong()).getChatColor()
                add("#${index + 1} §a${mob.name} $color${health.shortFormat()}§2/${maxHealth.shortFormat()}§c❤")
            }
        }.map { Renderable.string(it) }

        val renderable = Renderable.verticalContainer(list)
        config.summoningMobDisplayPos.renderRenderable(renderable, posLabel = "Summoning Mob Display")
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "summonings", "combat.summonings")
    }
}
