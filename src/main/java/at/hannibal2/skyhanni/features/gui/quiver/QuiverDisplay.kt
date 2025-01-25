package at.hannibal2.skyhanni.features.gui.quiver

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.combat.QuiverDisplayConfig.ShowWhen
import at.hannibal2.skyhanni.data.ArrowType
import at.hannibal2.skyhanni.data.QuiverApi
import at.hannibal2.skyhanni.data.QuiverApi.NONE_ARROW_TYPE
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.QuiverUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

@SkyHanniModule
object QuiverDisplay {

    private val config get() = SkyHanniMod.feature.combat.quiverConfig.quiverDisplay

    private var display = emptyList<Renderable>()
    private var arrow: ArrowType? = null
    private var amount = QuiverApi.currentAmount
    private var hideAmount = false

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = emptyList()
        arrow = QuiverApi.currentArrow
        amount = QuiverApi.currentAmount
        updateDisplay()
    }

    private fun updateDisplay() {
        display = drawDisplay()
    }

    private fun drawDisplay() = buildList {
        val arrow = arrow ?: return@buildList
        val itemStack = NeuItems.getItemStackOrNull(arrow.internalName.asString()) ?: ItemStack(Items.arrow)

        val rarity = itemStack.getItemRarityOrNull()?.chatColorCode ?: "§f"
        val arrowDisplayName =
            if (hideAmount || arrow == NONE_ARROW_TYPE) arrow.arrow else StringUtils.pluralize(amount, arrow.arrow)

        if (config.showIcon.get()) {
            add(Renderable.itemStack(itemStack, 1.0))
        }
        if (!hideAmount) {
            add(Renderable.string("§b${amount.addSeparators()}x"))
        }
        add(Renderable.string(" $rarity$arrowDisplayName"))
    }

    @HandleEvent
    fun onQuiverUpdate(event: QuiverUpdateEvent) {
        arrow = event.currentArrow
        amount = event.currentAmount
        hideAmount = QuiverApi.wearingSkeletonMasterChestplate

        updateDisplay()
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (display.isEmpty()) updateDisplay()
        val whenToShow = config.whenToShow.get()
        if (whenToShow == ShowWhen.ALWAYS ||
            whenToShow == ShowWhen.ONLY_BOW_INVENTORY && QuiverApi.hasBowInInventory() ||
            whenToShow == ShowWhen.ONLY_BOW_HAND && QuiverApi.isHoldingBow()
        ) {
            val content =
                Renderable.horizontalContainer(display, 1, verticalAlign = RenderUtils.VerticalAlignment.CENTER)
            config.quiverDisplayPos.renderRenderables(listOf(content), posLabel = "Quiver Display")
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        ConditionalUtils.onToggle(
            config.whenToShow,
            config.showIcon
        ) {
            updateDisplay()
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
