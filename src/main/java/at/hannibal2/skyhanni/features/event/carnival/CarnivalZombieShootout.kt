package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.ServerBlockChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.drawHitbox
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object CarnivalZombieShootout {

    private val config get() = SkyHanniMod.feature.event.carnival.zombieShootout

    private data class Lamp(var pos: LorenzVec, var time: SimpleTimeMark)
    private data class Updates(var zombie: SimpleTimeMark, var content: SimpleTimeMark)

    private var lastUpdate = Updates(SimpleTimeMark.farPast(), SimpleTimeMark.farPast())

    private var content = Renderable.horizontalContainer(listOf())
    private var drawZombies = mapOf<EntityZombie, ZombieType>()
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

    enum class ZombieType(val points: Int, val helmet: String, val color: Color) {
        LEATHER(30, "Leather Cap", Color(165, 42, 42)), // Brown
        IRON(50, "Iron Helmet", Color(192, 192, 192)), // Silver
        GOLD(80, "Golden Helmet", Color(255, 215, 0)), // Gold
        DIAMOND(120, "Diamond Helmet", Color(44, 214, 250)) // Diamond
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!isEnabled() || !started || (!config.coloredHitboxes && !config.coloredLines)) return

        lamp?.let {
            if (config.coloredLines) event.draw3DLine(event.exactPlayerEyeLocation(), it.pos.add(0.0, 0.5, 0.0), Color.RED, 3, false)
            if (config.coloredHitboxes) event.drawWaypointFilled(it.pos, Color.RED, minimumAlpha = 1.0f)
        }

        if (!config.coloredHitboxes) return

        if (lastUpdate.zombie.passedSince() >= 0.25.seconds) {
            val nearbyZombies = EntityUtils.getEntitiesNextToPlayer<EntityZombie>(50.0).mapNotNull { zombie ->
                if (zombie.health <= 0) return@mapNotNull null
                val armor = zombie.getCurrentArmor(3) ?: return@mapNotNull null
                val type = toType(armor) ?: return@mapNotNull null
                zombie to type
            }.toMap()

            drawZombies =
                if (config.highestOnly) nearbyZombies.filterValues { zombieType -> zombieType == nearbyZombies.values.maxByOrNull { it.points } }
                else nearbyZombies

            lastUpdate.zombie = SimpleTimeMark.now()
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

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled() || !started || !config.lampTimer) return

        val time = lamp?.time ?: return

        val lamp = ItemStack(Blocks.redstone_lamp)
        val timer = 6.seconds - (SimpleTimeMark.now() - time)
        val prefix = when (timer) {
            in 4.seconds..6.seconds -> "§a"
            in 2.seconds..4.seconds -> "§e"
            else -> "§c"
        }

        if (lastUpdate.content.passedSince() >= 0.1.seconds) {
            content = Renderable.horizontalContainer(
                listOf(
                    Renderable.itemStack(lamp),
                    Renderable.string("§6Disappears in $prefix$timer"),
                ),
                spacing = 1,
                verticalAlign = RenderUtils.VerticalAlignment.CENTER,
            )
            lastUpdate.content = SimpleTimeMark.now()
        }

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
        if (!isEnabled()) return

        val message = event.message.removeColor()

        if (startPattern.matches(message)) {
            started = true
        } else if (endPattern.matches(message)) {
            started = false
        }
    }

    private fun toType(item: ItemStack) = ZombieType.entries.find { it.helmet == item.displayName }

    private fun isEnabled() = config.enabled && LorenzUtils.skyBlockArea == "Carnival"
}
