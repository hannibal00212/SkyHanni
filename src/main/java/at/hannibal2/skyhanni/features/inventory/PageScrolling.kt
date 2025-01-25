package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.ToolTipData
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.renderables.ScrollValue
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object PageScrolling {

    private val config get() = SkyHanniMod.feature.inventory.pageScrolling

    private val patternGroup = RepoPattern.group("inventory.pagescrolling")

    private val illegalInventory by patternGroup.list(
        "illegal",
        "Large Chest",
        "Chest",
    )

    private val forwardPattern by patternGroup.list(
        "forward",
        "§aNext Page",
        "§aScroll Up",
        "§aLevels 26 - 50",
        "§aNext Page →",
        "§aScroll Right",
    )

    private val backwardPattern by patternGroup.list(
        "backward",
        "§aPrevious Page",
        "§aScroll Down",
        "§aLevels 1 - 25",
        "§a← Previous Page",
        "§aScroll Left",
    )

    private val scroll = ScrollValue()

    private var cooldown = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onLorenzTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (InventoryUtils.inStorage() && InventoryUtils.isNeuStorageEnabled) return
        if (cooldown.isInFuture()) return
        if (!scroll.isMouseEventValid()) return

        val inventoryName = InventoryUtils.openInventoryName()
        if (inventoryName.isEmpty()) return
        if (illegalInventory.matches(inventoryName)) return

        if (((ToolTipData.lastSlot != null) xor config.invertBypass xor config.bypassKey.isKeyHeld())) return

        val dWheel = Mouse.getEventDWheel()
        if (dWheel == 0) return
        val patterns = if ((dWheel > 0) xor config.invertScroll) forwardPattern else backwardPattern
        val slot = InventoryUtils.getItemsInOpenChest().firstOrNull {
            patterns.matches(it.stack?.displayName)
        } ?: return
        InventoryUtils.clickSlot(slot.slotNumber)
        cooldown = 1.0.seconds.fromNow() // 1 second is not specific it is just a reasonable cooldown
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enable && InventoryUtils.inInventory()
}
