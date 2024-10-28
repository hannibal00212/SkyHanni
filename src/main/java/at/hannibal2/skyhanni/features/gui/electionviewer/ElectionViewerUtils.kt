package at.hannibal2.skyhanni.features.gui.electionviewer

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.jsonobjects.other.MayorCandidate
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.FakePlayer
import at.hannibal2.skyhanni.utils.ItemUtils.getSkullOwner
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.renderables.Renderable
import com.mojang.authlib.GameProfile
import net.minecraft.client.Minecraft
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.nbt.NBTUtil

@SkyHanniModule
object ElectionViewerUtils {

    private val specialMayorStart = mapOf(
        "Scorpius" to 1,
        "Derpy" to 9,
        "Jerry" to 17,
    )

    private val mayorRenderables = mutableMapOf<String, Renderable>()

    fun getFakeMayorRenderable(mayor: ElectionCandidate) = mayorRenderables.getOrPut(mayor.name) { getFakeMayor(mayor) }

    fun getFakeCandidateRenderable(candidate: MayorCandidate) =
        getFakeMayorRenderable(ElectionCandidate.getMayorFromName(candidate.name) ?: ElectionCandidate.UNKNOWN)

    /**
     * The code doesn't work correctly if the [currentYear] is below 17
     * @param currentYear The current year
     * @return A list of the next three special mayors and the year they will be elected
     */
    fun getNextSpecialMayors(currentYear: Int) =
        specialMayorStart.map { it.key to it.value + ((currentYear - it.value) / 24 + 1) * 24 }.sortedBy { it.second }

    private fun getFakeMayor(mayor: ElectionCandidate): Renderable {
        // Jerry is a Villager, not a player
        val entity = if (mayor == ElectionCandidate.JERRY) {
            EntityVillager(Minecraft.getMinecraft().theWorld)
        } else {
            FakePlayer(getGameProfileFromMayor(mayor))
        }

        return Renderable.fakePlayer(
            entity,
            followMouse = true,
            entityScale = 50,
        )
    }

    private fun getGameProfileFromMayor(mayor: ElectionCandidate): GameProfile? {
        val mayorName = if (mayor.isSpecial()) {
            "${mayor.name}_SPECIAL"
        } else {
            mayor.name
        } + "_MAYOR_MONSTER"

        val skullOwner = mayorName.toInternalName().getItemStack().getSkullOwner() ?: return null
        return NBTUtil.readGameProfileFromNBT(skullOwner)
    }

    @HandleEvent
    fun onCommand(event: CommandRegistrationEvent) {
        event.register("shelectionviewer") {
            aliases = listOf("shmayor", "shelection", "shmayorviewer")
            description = "Opens the Mayor Election Viewer"
            callback { SkyHanniMod.screenToOpen = CurrentMayorScreen }
        }
    }
}
