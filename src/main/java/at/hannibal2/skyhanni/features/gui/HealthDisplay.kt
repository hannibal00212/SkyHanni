package at.hannibal2.skyhanni.features.gui

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ActionBarStatsData
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.rift.RiftAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.EntityUtils.hasPotionEffect
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.ColorRange
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.potion.Potion
import net.minecraftforge.client.GuiIngameForge
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HealthDisplay {
    private val config get() = SkyHanniMod.feature.gui.healthDisplay

    private var health = 0.0
    private var healthLast = 0.0
    private var healthTimer = SimpleTimeMark.farPast()
    private var maxHealth = 0.0
    private var colorList = listOf<ColorRange>()

    private var healthUpdate = 0
    private var healthUpdateTimer = SimpleTimeMark.farPast()

    private var actualHealth = 0
    private var absorptionRate = 0.0
    private var hasAbsorption = false

    private val healthActionBarPattern by RepoPattern.pattern(
        "gui.health.actionbar",
        "(?<string>§[c6][\\d,]+/[\\d,]+❤ +).*",
    )

    private enum class HealthColors(
        val range: ClosedFloatingPointRange<Double>,
        val color: Color,
    ) {
        RED(0.0..1.0, Color(255, 19, 19)),
        ORANGE(1.0..2.0, Color(238, 129, 0)),
        YELLOW(2.0..3.0, Color(229, 206, 0)),
        GREEN(3.0..4.0, Color(0, 218, 0)),
        BLUE(4.0..5.0, Color(12, 157, 241)),
        PURPLE(5.0..6.0, Color(180, 134, 255)),
        PINK(6.0..7.0, Color(236, 138, 251)),
        TAN(7.0..8.0, Color(251, 215, 139)),
        AQUA(8.0..9.0, Color(3, 239, 236)),
        LIGHT_BLUE(9.0..10.0, Color(183, 231, 253)),
        ALMOST_WHITE(10.0..Double.MAX_VALUE, Color(237, 237, 237)),
        ;

        companion object {
            fun getColors(input: Double): Pair<HealthColors?, HealthColors?> {
                val color1 = entries.firstOrNull { input in it.range }
                val color2 =
                    if (color1 == RED) {
                        RED
                    } else {
                        entries.firstOrNull { (input - 1.0) in it.range }
                    }
                return color1 to color2
            }
        }
    }

    @SubscribeEvent
    fun onActionBar(event: ActionBarUpdateEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (RiftAPI.inRift()) return

        maxHealth = ActionBarStatsData.MAX_HEALTH.value
            .replace(",", "")
            .toDoubleOrNull() ?: return
        val hp =
            ActionBarStatsData.HEALTH.value
                .replace(",", "")
                .toDoubleOrNull() ?: return
        val player = LorenzUtils.getPlayer() ?: return

        if (config.predictHealth) {
            if (maxHealth < hp && player.absorptionAmount != 0.0f) {
                absorptionRate =
                    (hp - maxHealth) / player.absorptionAmount
            }
            return
        }
        healthUpdate = healthUpdater(hp.toInt() - actualHealth)
        actualHealth = hp.toInt()
        healthLast = health
        health = hp / maxHealth
        healthTimer = SimpleTimeMark.now()

        setColors()
        if (config.hideActionBar) removeActionBar(event)
    }

    private fun removeActionBar(event: ActionBarUpdateEvent) {
        healthActionBarPattern.matchMatcher(event.actionBar) {
            val string = event.actionBar.replace(group("string"), "")
            event.changeActionBar(string)
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!RiftAPI.inRift() && !config.predictHealth) return

        val player = LorenzUtils.getPlayer() ?: return
        hasAbsorption = player.absorptionAmount > 0.0f

        healthLast = health
        if (RiftAPI.inRift()) {
            if (maxHealth < player.maxHealth || config.riftDynamicHP) maxHealth = player.maxHealth.toDouble()
            healthUpdate = healthUpdater(player.health.toInt() - actualHealth)
            actualHealth = (player.health + 0.1f).toInt() // player hp in rift sometimes is x.98 or something like that
            health = actualHealth / maxHealth
            healthTimer = SimpleTimeMark.now()
        } else {
            health = (((player.health) / player.maxHealth).toDouble())
            health += (player.absorptionAmount * absorptionRate) / maxHealth
            healthTimer = SimpleTimeMark.now()
            healthUpdate = healthUpdater((health * maxHealth).toInt() - actualHealth)
            actualHealth = (health * maxHealth).toInt()
        }

        setColors()
    }

    private fun getColorList(input: Pair<HealthColors, HealthColors>): List<ColorRange> {
        if (input.first == HealthColors.RED) return listOf(ColorRange(0.0, 1.0, Color.RED))

        var newHealth = health
        while (newHealth > 1.0) {
            newHealth -= 1.0
        }

        return listOf(
            ColorRange(0.0, newHealth, input.first.color),
            ColorRange(newHealth, 1.0, input.second.color),
        )
    }

    private fun setColors() {
        val colors = HealthColors.getColors(health)
        val firstColor = colors.first ?: return
        val secondColor = colors.second ?: return

        colorList = getColorList(Pair(firstColor, secondColor))
    }

    private fun healthUpdater(update: Int): Int {
        if (update == 0) {
            if (healthUpdateTimer.passedSince() > 2.seconds) {
                healthUpdateTimer = SimpleTimeMark.now()
                return 0
            } else {
                return healthUpdate
            }
        }
        healthUpdateTimer = SimpleTimeMark.now()
        return update
    }

    @SubscribeEvent
    fun onRiftEntry(event: IslandChangeEvent) {
        if (event.newIsland != IslandType.THE_RIFT) return

        val player = LorenzUtils.getPlayer() ?: return
        maxHealth = player.maxHealth.toDouble()
    }

    @SubscribeEvent
    fun onRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return

        if (config.enabledBar) renderBar()
        if (config.enabledText) renderText()
    }

    private fun renderBar() {
        val interpolatedHealth =
            NumberUtil.interpolate(health.toFloat(), healthLast.toFloat(), healthTimer.toMillis()).toDouble()
        val barRenderable =
            if (config.texture.enabled) {
                Renderable.progressBarMultipleColors(
                    if (interpolatedHealth > 1.0) 1.0 else interpolatedHealth,
                    colorList,
//                     texture = SkyHanniMod.feature.skillProgress.skillProgressBarConfig.texturedBar.usedTexture.get(),
                )
            } else {
                Renderable.progressBarMultipleColors(
                    if (interpolatedHealth > 1.0) 1.0 else interpolatedHealth,
                    colorList,
                    width = config.width,
                    height = config.height,
                )
            }
        config.positionBar.renderRenderables(listOf(barRenderable), posLabel = "HP Bar")
    }

    private fun renderText() {
        val colorCode =
            if (actualHealth > maxHealth) {
                "§6"
            } else {
                "§c"
            }

        var string = "$colorCode$actualHealth/${maxHealth.toInt()}❤"
        val updateText =
            when {
                healthUpdate == 0 -> ""
                healthUpdate > 0 -> "+$healthUpdate"
                else -> "$healthUpdate"
            }
        if (config.healthUpdates && updateText.isNotEmpty()) {
            string += " §c$updateText"
        }

        val textRenderable =
            Renderable.string(
                string,
            )
        config.positionText.renderRenderables(listOf(textRenderable), posLabel = "HP Text")
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("Health Display")

        if (!isEnabled()) {
            event.addIrrelevant("disabled in config")
            return
        }

        val player = LorenzUtils.getPlayer() ?: return
        event.addData {
            add("health: $health")
            add("healthUpdate: $healthUpdate")
            add("maxHealth: $maxHealth")

            add("playerhp: ${player.health}")
            add("player maxhp: ${player.maxHealth}")

            add("absorptionRate: $absorptionRate")
            add("has absorption: ${player.hasPotionEffect(Potion.absorption)}")
            add("absroption amt: ${player.absorptionAmount}")
        }
    }

    @SubscribeEvent
    fun onRenderRemoveBars(event: RenderGameOverlayEvent.Pre) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.hideVanillaHP.get()) return
        if (event.type != RenderGameOverlayEvent.ElementType.HEALTH) return

        GuiIngameForge.renderHealth = false
    }

    @SubscribeEvent
    fun onConfig(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(config.hideVanillaHP) {
            GuiIngameForge.renderHealth = true
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && (config.enabledBar || config.enabledText)
}
