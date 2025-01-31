package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.SkyHanniRenderEntityEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand

@SkyHanniModule
object OfflineCakeCounter {

    private val patternGroup = RepoPattern.group("misc.cakecounter")

    /**
     * REGEX-TEST: §7You placed a §r§eCake Counter§r§7. §r§7(9/15)
     */
    private val cakeCounterPlacedPattern by patternGroup.pattern(
        "placed",
        "§7You placed a §r§eCake Counter§r§7\\. §r§7\\([\\d/]+\\)",
    )

    /**
     * REGEX-TEST: Cakes Eaten: §d9,453,416
     */
    private val cakesEatenPattern by patternGroup.pattern(
        "cakeseaten",
        "Cakes Eaten: §d(?<cakes>[\\d,]+)",
    )

    /**
     * REGEX-TEST: Souls Found: §b9,341
     */
    private val soulsFoundPattern by patternGroup.pattern(
        "soulsfound",
        "Souls Found: §b(?<souls>[\\d,]+)",
    )

    private val config get() = SkyHanniMod.feature.misc
    private val storage get() = ProfileStorageData.profileSpecific?.cakeCounterData

    private var cakesEaten: Int
        get() = storage?.cakesEaten ?: -1
        set(value) {
            storage?.cakesEaten = value
        }

    private var soulsFound: Int
        get() = storage?.soulsFound ?: 0
        set(value) {
            storage?.soulsFound = value
        }

    private var newCakesEaten: Int? = null
    private var newSoulsFound: Int? = null

    private var statsToBeSent = true

    @HandleEvent(onlyOnIsland = IslandType.PRIVATE_ISLAND)
    fun onRenderEntity(event: SkyHanniRenderEntityEvent.Specials.Pre<EntityArmorStand>) {
        val entity = event.entity
        val name = entity.name

        cakesEatenPattern.matchMatcher(name) {
            newCakesEaten = group("cakes").formatInt()
            newCakesEaten?.let {
                // -1 means that cakesEaten has never been found before on this profile
                // stats should therefore not be sent as this likely means the Cake Counter has only just been placed
                if (cakesEaten == -1) {
                    statsToBeSent = false
                }
                if (it > cakesEaten) {
                    cakesEaten = it
                }
            }
        }

        soulsFoundPattern.matchMatcher(name) {
            newSoulsFound = group("souls").formatInt()
            newSoulsFound?.let {
                if (it > soulsFound) {
                    soulsFound = it
                }
            }
        }

        if (config.offlineCakeCounter && statsToBeSent && newCakesEaten != null) {
            val newCakesEaten = newCakesEaten ?: return
            val cakeDifference = newCakesEaten - cakesEaten
            val cakesFormat = StringUtils.pluralize(cakeDifference, "Century Cake")
            val message = "While you were away, players ate §d$cakeDifference§e $cakesFormat"

            if (newSoulsFound != null) {
                val soulDifference = newSoulsFound!! - soulsFound
                val soulsFormat = StringUtils.pluralize(soulDifference, "Cake Soul")
                ChatUtils.chat(
                    "$message and found §b$soulDifference§e $soulsFormat.",
                )
                return
            }
            ChatUtils.chat("$message.")
            statsToBeSent = false
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.PRIVATE_ISLAND)
    fun onChat(event: SkyHanniChatEvent) {
        if (config.offlineCakeCounter) return

        if (cakeCounterPlacedPattern.matches(event.message)) {
            DelayedRun.runNextTick {
                ChatUtils.clickableChat(
                    "Click here to be notified of any stat changes on your Cake Counter every time you rejoin your Private Island.",
                    onClick = {
                        config.offlineCakeCounter = true
                        ChatUtils.chat("Enabled Offline Cake Counter!")
                    },
                    hover = "§eClick to enable Offline Cake Counter!",
                    replaceSameMessage = true,
                    oneTimeClick = true,
                )
            }
        }
    }

    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        statsToBeSent = true
        newCakesEaten = null
        newSoulsFound = null
    }
}
