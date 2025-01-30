package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object OfflineCakeCounter {
    private val patternGroup = RepoPattern.group("misc.cakecounter")

    /**
     * REGEX-TEST: Cakes Eaten: §d9,453,416
     */
    private val cakesEatenPattern by patternGroup.pattern(
        "cakeseaten",
        "Cakes Eaten: §d(?<eaten>[\\d,]+)",
    )

    /**
     * REGEX-TEST: Souls Found: §b9,341
     */
    private val soulsFoundPattern by patternGroup.pattern(
        "soulsfound",
        "Souls Found: §b(?<souls>[\\d,]+)",
    )

    private val storage get() = ProfileStorageData.profileSpecific?.cakeCounterData

    private var cakesEaten: Int
        get() = storage?.cakesEaten ?: 0
        set(value) {
            storage?.cakesEaten = value
        }

    private var soulsFound: Int
        get() = storage?.soulsFound ?: 0
        set(value) {
            storage?.soulsFound = value
        }

    @HandleEvent(onlyOnIsland = IslandType.PRIVATE_ISLAND)
    fun onRenderLiving(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        if (!SkyHanniMod.feature.misc.offlineCakeCounter) return
        val entity = event.entity
        val name = entity.name

        // -1 means that the Cakes Eaten / Souls Found display has never been checked before
        cakesEatenPattern.matchMatcher(name) {
            val newCakesEaten = group("eaten").formatInt()
            if (newCakesEaten > cakesEaten) {
                val cakeDifference = newCakesEaten - cakesEaten
                val cakesFormat = StringUtils.pluralize(cakeDifference, "Century Cake")
                val message = "While you were away, players ate §d$cakeDifference§e $cakesFormat"

                soulsFoundPattern.matchMatcher(name) {
                    val newSoulsFound = group("souls").formatInt()

                    if (newSoulsFound > soulsFound) {
                        if (soulsFound != -1) {
                            val soulDifference = newSoulsFound - soulsFound
                            val soulsFormat = StringUtils.pluralize(soulDifference, "Cake Soul")
                            ChatUtils.chat("$message and found §b$soulDifference§e $soulsFormat.")
                        }
                        soulsFound = newSoulsFound
                    }
                }

                // if there's no "Souls Found:" display, no soul has ever been found on the player's Private Island
                if (!soulsFoundPattern.matcher(name).find()) {
                    soulsFound = 0
                }

                if (cakesEaten != -1) {
                    ChatUtils.chat("$message.")
                }
                cakesEaten = newCakesEaten
            }
        }
    }
}
