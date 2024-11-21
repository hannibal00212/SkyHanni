package at.hannibal2.skyhanni.features.event.diana

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.PetAPI
import at.hannibal2.skyhanni.events.diana.InquisitorFoundEvent
import at.hannibal2.skyhanni.events.entity.EntityEnterWorldEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.item.ItemStack

@SkyHanniModule
object DianaAPI {

    private val spade by lazy { "ANCESTRAL_SPADE".toInternalName() }

    fun hasSpadeInHand() = InventoryUtils.itemInHandId == spade

    private fun isRitualActive() = Perk.MYTHOLOGICAL_RITUAL.isActive ||
        Perk.PERKPOCALYPSE.isActive

    fun hasGriffinPet() = PetAPI.isCurrentPet("Griffin")

    fun isDoingDiana() = IslandType.HUB.isInIsland() && isRitualActive() && hasSpadeInInventory()

    val ItemStack.isDianaSpade get() = getInternalName() == spade

    private fun hasSpadeInInventory() = InventoryUtils.getItemsInOwnInventory().any { it.isDianaSpade }

    @HandleEvent(onlyOnSkyblock = true)
    fun onJoinWorld(event: EntityEnterWorldEvent<EntityOtherPlayerMP>) {
        if (event.entity.name == "Minos Inquisitor") {
            InquisitorFoundEvent(event.entity).post()
        }
    }
}
