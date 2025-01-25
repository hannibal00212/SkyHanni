package at.hannibal2.skyhanni.features.rift

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.rift.RiftConfig
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.skyblock.GraphAreaChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.isRiftExportable
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.wasRiftTransferred
import net.minecraft.item.ItemStack

@SkyHanniModule
object RiftApi {

    fun inRift() = IslandType.THE_RIFT.isInIsland()

    val config: RiftConfig get() = SkyHanniMod.feature.rift

    // internal name -> motes
    var motesPrice = emptyMap<NeuInternalName, Double>()

    val farmingTool = "FARMING_WAND".toInternalName()

    private val blowgun = "BERBERIS_BLOWGUN".toInternalName()

    val ItemStack?.isBlowgun: Boolean
        get() = this?.getInternalName() == blowgun

    fun ItemStack.motesNpcPrice(): Double? {
        if (isRiftExportable() && wasRiftTransferred()) return null
        val baseMotes = motesPrice[getInternalName()] ?: return null
        val burgerStacks = config.motes.burgerStacks
        val pricePer = baseMotes + (burgerStacks * 5) * baseMotes / 100
        return pricePer * stackSize
    }

    var inMirrorVerse = false
    private var inColosseum = false
    var inRiftRace = false
    var trackingButtons = false
    var allButtonsHit = false

    @HandleEvent
    fun onAreaChange(event: GraphAreaChangeEvent) {
        inMirrorVerse = event.area == "Mirrorverse"
        inColosseum = event.area == "Colosseum"
    }

    fun inLivingCave() = LorenzUtils.skyBlockArea == "Living Cave"
    fun inLivingStillness() = LorenzUtils.skyBlockArea == "Living Stillness"
    fun inStillgoreChateau() = LorenzUtils.skyBlockArea.let { it == "Stillgore Château" || it == "Oubliette" }
    fun inColosseum() = LorenzUtils.skyBlockArea == "Colosseum" || inColosseum
    fun inDreadfarm() = LorenzUtils.skyBlockArea == "Dreadfarm"
    fun inWestVillage() = LorenzUtils.skyBlockArea.let { it == "West Village" || it == "Infested House" }
    fun inMountainTop() = when (LorenzUtils.skyBlockArea) {
        "Continuum", "The Mountaintop", "Trial Grounds", "Time-Torn Isles",
        "Wizardman Bureau", "Wizard Brawl", "Walk of Fame", "Time Chamber" -> true
        else -> false
    }
}
