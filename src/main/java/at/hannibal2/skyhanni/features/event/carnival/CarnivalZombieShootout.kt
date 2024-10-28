package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawHitbox
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CarnivalZombieShootout {

    private val config get() = SkyHanniMod.feature.event.carnival.zombieShootout

    private data class Lamp(var pos: LorenzVec, var time: SimpleTimeMark)
    private data class Zombie(val entity: EntityZombie, val type: ZombieType)

    private var content = Renderable.horizontalContainer(listOf())
    private var drawZombies = listOf<Zombie>()
    private val zombieTimes = mutableMapOf<Zombie, SimpleTimeMark>()
    private var maxType = ZombieType.LEATHER
    private var lamp: Lamp? = null
    private var started = false

    private val patternGroup = RepoPattern.group("event.carnival")

    /**
     * REGEX-TEST: [NPC] Carnival Cowboy: Good luck, pal!
     */
    private val startPattern by patternGroup.pattern(
        "shootout.start",
        "\\[NPC] Carnival Cowboy: Good luck, pal!",
    )

    /**
     * REGEX-TEST:                              Zombie Shootout
     */
    private val endPattern by patternGroup.pattern(
        "shootout.end",
        " {29}Zombie Shootout",
    )

    enum class ZombieType(val points: Int, val helmet: String, val color: Color, val lifetime: Duration) {
        LEATHER(30, "Leather Cap", Color(165, 42, 42), 8.seconds), // Brown
        IRON(50, "Iron Helmet", Color(192, 192, 192), 7.seconds), // Silver
        GOLD(80, "Golden Helmet", Color(255, 215, 0), 6.seconds), // Gold
        DIAMOND(120, "Diamond Helmet", Color(44, 214, 250), 5.seconds) // Diamond
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled() || (!config.coloredHitboxes && !config.coloredLines && !config.zombieTimer)) return

        if (config.zombieTimer) {
            val zombiesToRemove = mutableListOf<Zombie>()

            for ((zombie, time) in zombieTimes) {
                val lifetime = zombie.type.lifetime
                val exceeded = time.passedSince() >= lifetime

                if (config.highestOnly && zombie.type != maxType) continue

                if (!exceeded) {
                    val entity = EntityUtils.getEntityByID(zombie.entity.entityId) ?: continue
                    val isSmall = (entity as? EntityZombie)?.isChild ?: false

                    val timer = lifetime - time.passedSince()
                    val skips = lifetime / 3
                    val prefix = determinePrefix(timer, lifetime, lifetime - skips, lifetime - skips * 2)
                    val height = if (isSmall) entity.height / 2 else entity.height

                    event.drawString(
                        entity.getLorenzVec().add(0.0, height.plus(0.5), 0.0),
                        "$prefix$timer",
                    )
                } else {
                    zombiesToRemove.add(zombie)
                }
            }

            zombiesToRemove.forEach { zombieTimes.remove(it) }
        }

        if (config.coloredHitboxes) {
            lamp?.let {
                if (config.coloredLines) event.draw3DLine(
                    event.exactPlayerEyeLocation(),
                    it.pos.add(0.0, 0.5, 0.0),
                    Color.RED,
                    3,
                    false,
                )
                event.drawWaypointFilled(it.pos, Color.RED, minimumAlpha = 1.0f)
            }

            for ((zombie, type) in drawZombies) {
                val entity = EntityUtils.getEntityByID(zombie.entityId) ?: continue
                val isSmall = (entity as? EntityZombie)?.isChild ?: false

                val boundingBox = if (isSmall) entity.entityBoundingBox.expand(0.0, -0.4, 0.0).offset(0.0, -0.4, 0.0)
                else entity.entityBoundingBox

                event.drawHitbox(
                    boundingBox.expand(0.1, 0.05, 0.0).offset(0.0, 0.05, 0.0),
                    lineWidth = 3,
                    type.color,
                    depth = false,
                )
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled() || !config.lampTimer) return

        config.lampPosition.renderRenderable(content, posLabel = "Lantern Timer")
    }

    @SubscribeEvent
    fun onBlockChange(event: ServerBlockChangeEvent) {
        if (!isEnabled() || !started) return

        val old = event.old
        val new = event.new

        lamp = when {
            old == "redstone_lamp" && new == "lit_redstone_lamp" -> Lamp(event.location, SimpleTimeMark.now())
            old == "lit_redstone_lamp" && new == "redstone_lamp" -> null
            else -> lamp
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!config.enabled || LorenzUtils.skyBlockArea != "Carnival") return

        val message = event.message.removeColor()

        if (startPattern.matches(message)) {
            started = true
        } else if (endPattern.matches(message)) {
            started = false
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled() || (!config.coloredHitboxes && !config.zombieTimer && !config.lampTimer) || !event.isMod(2)) return

        if (config.coloredHitboxes || config.zombieTimer) {
            updateZombies()
        }

        if (config.lampTimer) {
            content = lamp?.let {
                updateContent(it.time)
            } ?: Renderable.horizontalContainer(listOf())
        }
    }

    private fun updateZombies() {
        val nearbyZombies = getZombies()
        maxType = nearbyZombies.maxBy { it.type.points }.type
        val maxZombies = nearbyZombies.filter { it.type == maxType }

        drawZombies = when {
            config.coloredHitboxes && config.highestOnly -> maxZombies
            config.coloredHitboxes -> nearbyZombies
            else -> emptyList()
        }

        if (config.zombieTimer) {
            for (zombie in nearbyZombies) {
                zombieTimes.putIfAbsent(zombie, SimpleTimeMark.now())
            }
        }
    }

    private fun updateContent(time: SimpleTimeMark): Renderable {
        val lamp = ItemStack(Blocks.redstone_lamp)
        val timer = 6.seconds - time.passedSince()
        val prefix = determinePrefix(timer, 6.seconds, 4.seconds, 2.seconds)

        return Renderable.horizontalContainer(
            listOf(
                Renderable.itemStack(lamp),
                Renderable.string("§6Disappears in $prefix$timer"),
            ),
            spacing = 1,
            verticalAlign = RenderUtils.VerticalAlignment.CENTER,
        )
    }

    private fun getZombies() =
        EntityUtils.getEntitiesNextToPlayer<EntityZombie>(50.0).mapNotNull { zombie ->
            if (zombie.health <= 0) return@mapNotNull null
            val armor = zombie.getCurrentArmor(3) ?: return@mapNotNull null
            val type = toType(armor) ?: return@mapNotNull null
            Zombie(zombie, type)
        }.toList()

    private fun determinePrefix(timer: Duration, good: Duration, mid: Duration, bad: Duration) =
        when (timer) {
            in mid..good -> "§a"
            in bad..mid -> "§e"
            else -> "§c"
        }

    private fun toType(item: ItemStack) = ZombieType.entries.find { it.helmet == item.displayName }

    private fun isEnabled() = config.enabled && LorenzUtils.skyBlockArea == "Carnival" && started
}
