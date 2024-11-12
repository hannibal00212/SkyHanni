package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.SlayerChangeEvent
import at.hannibal2.skyhanni.events.SlayerProgressChangeEvent
import at.hannibal2.skyhanni.events.SlayerQuestCompleteEvent
import at.hannibal2.skyhanni.features.slayer.SlayerType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getNpcPriceOrNull
import at.hannibal2.skyhanni.utils.NEUItems.getPrice
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RecalculatingValue
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SlayerAPI {

    private val nameCache = TimeLimitedCache<Pair<NEUInternalName, Int>, Pair<String, Double>>(1.minutes)

    var questStartTime = SimpleTimeMark.farPast()
    var isInCorrectArea = false
    var isInAnyArea = false
    var latestSlayerCategory = ""
    var latestWrongAreaWarning = SimpleTimeMark.farPast()
    var latestSlayerProgress = ""

    fun hasActiveSlayerQuest() = latestSlayerCategory != ""

    fun getItemNameAndPrice(internalName: NEUInternalName, amount: Int): Pair<String, Double> =
        nameCache.getOrPut(internalName to amount) {
            val amountFormat = if (amount != 1) "§7${amount}x §r" else ""
            val displayName = internalName.itemName

            val price = internalName.getPrice()
            val npcPrice = internalName.getNpcPriceOrNull() ?: 0.0
            val maxPrice = npcPrice.coerceAtLeast(price)
            val totalPrice = maxPrice * amount

            val format = totalPrice.shortFormat()

            if (internalName == NEUInternalName.SKYBLOCK_COIN) {
                "§6$format coins" to totalPrice
            } else {
                val priceFormat = " §7(§6$format coins§7)"
                "$amountFormat$displayName$priceFormat" to totalPrice
            }
        }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Slayer")

        if (!hasActiveSlayerQuest()) {
            event.addIrrelevant("no active slayer quest")
            return
        }

        event.addData {
            add("activeSlayer: $activeSlayer")
            add("isInCorrectArea: $isInCorrectArea")
            add("isInAnyArea: $isInAnyArea")
            add("latestSlayerProgress: ${latestSlayerProgress.removeColor()}")
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        if (event.message.contains("§r§5§lSLAYER QUEST STARTED!")) {
            questStartTime = SimpleTimeMark.now()
        }

        if (event.message == "  §r§a§lSLAYER QUEST COMPLETE!") {
            SlayerQuestCompleteEvent.post()
        }
    }

    val activeSlayer by RecalculatingValue(1.seconds) {
        grabActiveSlayer()
    }

    private fun grabActiveSlayer(): SlayerType? {
        for (line in ScoreboardData.sidebarLinesFormatted) {
            SlayerType.getByName(line)?.let {
                return it
            }
        }

        return null
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!LorenzUtils.inSkyBlock) return

        // wait with sending SlayerChangeEvent until profile is detected
        if (ProfileStorageData.profileSpecific == null) return

        val slayerQuest = ScoreboardData.sidebarLinesFormatted.nextAfter("Slayer Quest").orEmpty()
        if (slayerQuest != latestSlayerCategory) {
            val old = latestSlayerCategory
            latestSlayerCategory = slayerQuest
            SlayerChangeEvent(old, latestSlayerCategory).postAndCatch()
        }

        val slayerProgress = ScoreboardData.sidebarLinesFormatted.nextAfter("Slayer Quest", 2).orEmpty()
        if (latestSlayerProgress != slayerProgress) {
            SlayerProgressChangeEvent(latestSlayerProgress, slayerProgress).postAndCatch()
            latestSlayerProgress = slayerProgress
        }

        if (event.isMod(5)) {
            isInCorrectArea = if (LorenzUtils.isStrandedProfile) {
                isInAnyArea = true
                true
            } else {
                val slayerTypeForCurrentArea = getSlayerTypeForCurrentArea()
                isInAnyArea = slayerTypeForCurrentArea != null
                slayerTypeForCurrentArea == activeSlayer && slayerTypeForCurrentArea != null
            }
        }
    }

    // TODO USE SH-REPO
    fun getSlayerTypeForCurrentArea() = when (LorenzUtils.skyBlockArea) {
        "Graveyard",
        "Coal Mine",
        -> SlayerType.REVENANT

        "Spider Mound",
        "Arachne's Burrow",
        "Arachne's Sanctuary",
        "Burning Desert",
        -> SlayerType.TARANTULA

        "Ruins",
        "Howling Cave",
        -> SlayerType.SVEN

        "The End",
        "Dragon's Nest",
        "Void Sepulture",
        "Zealot Bruiser Hideout",
        -> SlayerType.VOID

        "Stronghold",
        "The Wasteland",
        "Smoldering Tomb",
        -> SlayerType.INFERNO

        "Stillgore Château",
        "Oubliette",
        -> SlayerType.VAMPIRE

        else -> null
    }
}
