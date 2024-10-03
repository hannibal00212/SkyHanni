package at.hannibal2.skyhanni.features.rift

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.rift.RiftConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import net.minecraft.item.ItemStack

object RiftAPI {

    fun inRift() = IslandType.THE_RIFT.isInIsland()

    val config: RiftConfig get() = SkyHanniMod.feature.rift

    // internal name -> motes
    var motesPrice = emptyMap<NEUInternalName, Double>()

    val farmingTool by lazy { "FARMING_WAND".asInternalName() }

    private val blowgun by lazy  { "BERBERIS_BLOWGUN".asInternalName() }

    val ItemStack?.isBlowgun: Boolean
        get() = this?.getInternalName() == blowgun

    fun ItemStack.motesNpcPrice(): Double? {
        val baseMotes = motesPrice[getInternalName()] ?: return null
        val burgerStacks = config.motes.burgerStacks
        val pricePer = baseMotes + (burgerStacks * 5) * baseMotes / 100
        return pricePer * stackSize
    }

    var inRiftRace = false
    var trackingButtons = false
    var allButtonsHit = false

    fun inLivingCave() = LorenzUtils.skyBlockArea == "Living Cave"
    fun inLivingStillness() = LorenzUtils.skyBlockArea == "Living Stillness"
    fun inStillgoreChateau() = LorenzUtils.skyBlockArea.let { it == "Stillgore Château" || it == "Oubliette" }
    fun inDreadfarm() = LorenzUtils.skyBlockArea == "Dreadfarm"
    fun inWestVillage() = LorenzUtils.skyBlockArea.let { it == "West Village" || it == "Infested House" }
}
