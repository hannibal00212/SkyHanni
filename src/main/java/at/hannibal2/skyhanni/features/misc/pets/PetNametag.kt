package at.hannibal2.skyhanni.features.misc.pets

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.entity.EntityDisplayNameEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class PetNametag {

    private val config get() = SkyHanniMod.feature.misc.pets.petNametag

    /**
     * REGEX-TEST: §8[§7Lv99§8] Ammonite
     * REGEX-TEST: §8[§7Lv100§8] Endermite§5 ✦
     */
    private val pattern by RepoPattern.pattern(
        "feature.pet.nametag",
        "(?<start>§8\\[§7Lv(?<lvl>\\d+)§8\\]) (?<rarity>§.)(?<pet>[\\w\\s]+)(?<skin>§. ✦)?"
    )

    @SubscribeEvent
    fun onNameTagRender(event: EntityDisplayNameEvent) {
        if (!isEnabled()) return
        if (event.entity !is EntityArmorStand) return

        pattern.matchMatcher(event.chatComponent.unformattedText) {
            val start = group("start")
            val lvl = group("lvl").formatInt()
            val rarity = group("rarity")
            val pet = group("pet")
            val skin = groupOrNull("skin") ?: ""

            var text = ""
            if (!config.hidePetLevel && (!config.hideMaxPetLevel || lvl == 100 || lvl == 200)) text += "$start "
            text += rarity + pet + skin

            event.chatComponent = text.asComponent()
        }
    }

    private fun isEnabled() = with(config) {
        (hidePetLevel || hideMaxPetLevel) && LorenzUtils.inSkyBlock
    }

}
