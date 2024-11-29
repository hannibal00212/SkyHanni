package at.hannibal2.skyhanni.features.misc.pets

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.entity.EntityDisplayNameEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrEmpty
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PetNametag {

    private val config get() = SkyHanniMod.feature.misc.pets.nametag

    /**
     * REGEX-TEST: §8[§7Lv99§8] §6Ammonite
     * REGEX-TEST: §8[§7Lv100§8] §dEndermite§5 ✦
     */
    private val petNametagPattern by RepoPattern.pattern(
        "pet.nametag",
        "(?<start>§8\\[§7Lv(?<lvl>\\d+)§8]) (?<rarity>§.)(?<pet>[\\w\\s]+)(?<skin>§. ✦)?",
    )

    @SubscribeEvent
    fun onNameTagRender(event: EntityDisplayNameEvent) {
        if (!isEnabled()) return
        if (event.entity !is EntityArmorStand) return

        petNametagPattern.matchMatcher(event.chatComponent.unformattedText) {
            val start = group("start")
            val lvl = group("lvl").formatInt()
            val rarity = group("rarity")
            val pet = group("pet")
            val skin = groupOrEmpty("skin")

            val hideLevel = config.hidePetLevel
            val hideMaxLevel = config.hideMaxPetLevel && (lvl == 100 || lvl == 200)

            val text = buildString {
                if (!hideLevel && !hideMaxLevel) {
                    append(start)
                }
                append(rarity + pet + skin)
            }

            event.chatComponent = text.asComponent()
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && (config.hidePetLevel || config.hideMaxPetLevel)
}
