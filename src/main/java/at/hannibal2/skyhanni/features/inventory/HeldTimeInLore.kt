package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addOrInsert
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getBottleOfJyrreSeconds
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getSecondsHeld
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object HeldTimeInLore {
    private val config get() = SkyHanniMod.feature.inventory

    private val jyrreBottle by lazy { "NEW_BOTTLE_OF_JYRRE".toInternalName() }
    private val cacaoTruffle by lazy { "DARK_CACAO_TRUFFLE".toInternalName() }

    @SubscribeEvent
    fun onTooltip(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.timeHeldInLore) return

        val seconds = event.itemStack.getSeconds() ?: return
        val formatted = seconds.seconds.format()

        event.toolTip.addOrInsert(10, "§7Time Held: §b$formatted")
    }

    private fun ItemStack.getSeconds(): Int? = when (getInternalName()) {
        jyrreBottle -> getBottleOfJyrreSeconds()
        cacaoTruffle -> getSecondsHeld()
        else -> null
    }
}


