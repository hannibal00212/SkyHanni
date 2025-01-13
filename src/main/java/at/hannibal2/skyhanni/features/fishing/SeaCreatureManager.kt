package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.SeaCreatureJson
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.fishing.SeaCreatureFishEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SeaCreatureManager {

    private var doubleHook = false

    private val seaCreatureMap = mutableMapOf<String, SeaCreature>()
    var allFishingMobs = mapOf<String, SeaCreature>()
    var allVariants = mapOf<String, List<String>>()

    private val patternGroup = RepoPattern.group("fishing.seacreature")

    /**
     * REGEX-TEST: §eIt's a §r§aDouble Hook§r§e! Woot woot!
     * REGEX-TEST: §eIt's a §r§aDouble Hook§r§e!
     */
    private val doubleHookPattern by patternGroup.pattern(
        "doublehook",
        "§eIt's a §r§aDouble Hook§r§e!(?: Woot woot!)?"
    )

    /**
     * REGEX-TEST: §e> Your bottle of thunder has fully charged!
     */
    private val thunderBottleChargedPattern by patternGroup.pattern(
        "thundercharged",
        "§e> Your bottle of thunder has fully charged!"
    )

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (doubleHookPattern.matches(event.message)) {
            if (SkyHanniMod.feature.fishing.compactDoubleHook) {
                event.blockedReason = "double_hook"
            }
            doubleHook = true
        } else if (!doubleHook || !thunderBottleChargedPattern.matches(event.message)) {
            val seaCreature = getSeaCreatureFromMessage(event.message)
            if (seaCreature != null) {
                SeaCreatureFishEvent(seaCreature, event, doubleHook).post()
            }
            doubleHook = false
        }
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        seaCreatureMap.clear()
        allFishingMobs = emptyMap()
        var counter = 0

        val data = event.getConstant<Map<String, SeaCreatureJson>>("SeaCreatures", SeaCreatureJson.TYPE)
        val allFishingMobs = mutableMapOf<String, SeaCreature>()

        val variants = mutableMapOf<String, List<String>>()

        for ((variantName, variant) in data) {
            val chatColor = variant.chatColor
            val variantFishes = mutableListOf<String>()
            variants[variantName] = variantFishes
            for ((name, seaCreature) in variant.seaCreatures) {
                val chatMessage = seaCreature.chatMessage
                val fishingExperience = seaCreature.fishingExperience
                val rarity = seaCreature.rarity
                val rare = seaCreature.rare

                val creature = SeaCreature(name, fishingExperience, chatColor, rare, rarity)
                seaCreatureMap[chatMessage] = creature
                allFishingMobs[name] = creature
                variantFishes.add(name)
                counter++
            }
        }
        SeaCreatureManager.allFishingMobs = allFishingMobs
        allVariants = variants
    }

    private fun getSeaCreatureFromMessage(message: String): SeaCreature? {
        return seaCreatureMap.getOrDefault(message, null)
    }
}
