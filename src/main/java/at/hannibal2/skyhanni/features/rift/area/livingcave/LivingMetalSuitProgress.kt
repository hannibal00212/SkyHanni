package at.hannibal2.skyhanni.features.rift.area.livingcave

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getLivingMetalProgress
import at.hannibal2.skyhanni.utils.renderables.Container
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.itemStack
import net.minecraft.item.ItemStack

@SkyHanniModule
object LivingMetalSuitProgress {

    private val config get() = RiftApi.config.area.livingCave.livingMetalSuitProgress
    private var display: Renderable? = null
    private var progressMap = mapOf<ItemStack, Double?>()

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        display?.let { config.position.renderRenderable(it, posLabel = "Living Metal Armor Progress") }
    }

    private fun update() {
        display = drawDisplay()
    }

    private fun drawDisplay(): Renderable? {
        val piecesMaxed = progressMap.values.filterNotNull().count { it >= 1 }
        val isMaxed = piecesMaxed == 4

        if (progressMap.isEmpty()) return null

        val totalProgress = progressMap.values.map { it ?: 1.0 }.average().roundTo(1)
        val formatPercentage = LorenzUtils.formatPercentage(totalProgress)
        val title = Renderable.string("§7Living Metal Suit Progress: ${if (isMaxed) "§a§lMAXED!" else "§a$formatPercentage"}")

        if (config.compactWhenMaxed && isMaxed) return title

        return Container.vertical {
            renderable(title)
            for ((stack, progress) in progressMap.entries.reversed()) {
                horizontal {
                    string("§7- ")
                    itemStack(stack)
                    string("${stack.displayName}: ")
                    string(
                        progress?.let {
                            drawProgressBar(it) + " §b${LorenzUtils.formatPercentage(it)}"
                        } ?: "§cStart upgrading it!",
                    )
                }
            }
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        val old = progressMap
        progressMap = buildMap {
            for (armor in InventoryUtils.getArmor().filterNotNull()) {
                put(
                    armor,
                    armor.getLivingMetalProgress()?.toDouble()?.let {
                        it.coerceAtMost(100.0) / 100
                    },
                )
            }
        }
        if (old != progressMap) {
            update()
        }
    }

    private fun drawProgressBar(percentage: Double): String {
        val progressBarLength = 20
        val filledLength = (percentage * progressBarLength).toInt()

        val green = "§a"
        val grey = "§7"
        val reset = "§f"

        val progressBar = StringBuilder()
        progressBar.append(green)
        repeat(filledLength) { progressBar.append("|") }

        progressBar.append(grey)
        repeat(progressBarLength - filledLength) { progressBar.append("|") }

        progressBar.append(reset)
        return progressBar.toString()
    }

    private fun isEnabled() = RiftApi.inRift() && config.enabled
}
