package at.hannibal2.skyhanni.features.inventory.experimentationtable

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.PetAPI
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.InventoryUtils.openInventoryName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SkullTextureHolder
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ExperimentationTableAPI {

    private val storage get() = ProfileStorageData.profileSpecific?.experimentation

    val inTable get() = inventoriesPattern.matches(openInventoryName())

    fun inDistanceToTable(vec: LorenzVec, max: Double): Boolean =
        storage?.tablePos?.let { it.distance(vec) <= max } ?: false

    fun getCurrentExperiment(): Experiment? =
        superpairsPattern.matchMatcher(openInventoryName()) {
            Experiment.entries.find { it.nameString == group("experiment") }
        }

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (LorenzUtils.skyBlockIsland != IslandType.PRIVATE_ISLAND || !inTable) return

        val entity = EntityUtils.getEntities<EntityArmorStand>().find {
            it.hasSkullTexture(EXPERIMENTATION_TABLE_SKULL)
        } ?: return
        val vec = entity.getLorenzVec()
        if (storage?.tablePos != vec) storage?.tablePos = vec
    }

    private val EXPERIMENTATION_TABLE_SKULL by lazy { SkullTextureHolder.getTexture("EXPERIMENTATION_TABLE") }
    private val patternGroup = RepoPattern.group("enchanting.experiments")

    /**
     * REGEX-TEST: Superpairs (Metaphysical)
     */
    private val superpairsPattern by patternGroup.pattern(
        "superpairs",
        "Superpairs \\((?<experiment>\\w+)\\)",
    )

    /**
     * REGEX-TEST: Gained +3 Clicks
     */
    val powerUpPattern by patternGroup.pattern(
        "powerups",
        "Gained \\+\\d Clicks?|Instant Find|\\+\\S* XP",
    )

    /**
     * REGEX-TEST: 123k Enchanting Exp
     * REGEX-TEST: Titanic Experience Bottle
     */
    val rewardPattern by patternGroup.pattern(
        "rewards",
        "\\d{1,3}k Enchanting Exp|Enchanted Book|(?:Titanic |Grand |\\b)Experience Bottle|Metaphysical Serum|Experiment the Fish",
    )

    /**
     * REGEX-TEST: Superpairs (Metaphysical)
     * REGEX-TEST: Chronomatron (Metaphysical)
     */
    val inventoriesPattern by patternGroup.pattern(
        "inventories",
        "(?:Superpairs|Chronomatron|Ultrasequencer) (?:\\(.+\\)|➜ Stakes|Rewards)|Experimentation Table",
    )

    /**
     * REGEX-TEST:  +42,000 Enchanting Exp
     */
    val enchantingExpChatPattern by patternGroup.pattern(
        "chatexp",
        "^ \\+(?<amount>\\d+|\\d+,\\d+)k? Enchanting Exp$",
    )

    /**
     * REGEX-TEST:  +Smite VII
     * REGEX-TEST:  +42,000 Enchanting Exp
     */
    val experimentsDropPattern by patternGroup.pattern(
        "drop",
        "^ \\+(?<reward>.*)\$",
    )

    /**
     * REGEX-TEST: You claimed the Superpairs rewards!
     */
    val claimMessagePattern by patternGroup.pattern(
        "claim",
        "You claimed the \\S+ rewards!",
    )

    /**
     * REGEX-TEST: 131k Enchanting Exp
     * REGEX-TEST: 42,000 Enchanting Exp
     */
    val enchantingExpPattern by patternGroup.pattern(
        "exp",
        "(?<amount>\\d+|\\d+,\\d+)k? Enchanting Exp",
    )

    /**
     * REGEX-TEST: Titanic Experience Bottle
     */
    val experienceBottleChatPattern by patternGroup.pattern(
        "chat.xpbottle",
        "(?:Colossal |Titanic |Grand |\\b)Experience Bottle",
    )

    /**
     * REGEX-TEST: TITANIC_EXP_BOTTLE
     */
    val experienceBottlePattern by patternGroup.pattern(
        "xpbottle",
        "(?:COLOSSAL_|TITANIC_|GRAND_|\\b)EXP_BOTTLE",
    )

    /**
     * REGEX-TEST: Remaining Clicks: 22
     */
    val remainingClicksPattern by patternGroup.pattern(
        "clicks",
        "Remaining Clicks: (?<clicks>\\d+)"
    )

    /**
     * REGEX-TEST: ☕ You renewed the experiment table! (1/3)
     */
    val experimentRenewPattern by patternGroup.pattern(
        "renew",
        "^☕ You renewed the experiment table! \\((?<current>\\d)/3\\)$",
    )

    /**
     * REGEX-TEST: §d§kXX§5 ULTRA-RARE BOOK! §d§kXX
     */
    val ultraRarePattern by patternGroup.pattern(
        "ultrarare",
        "§d§kXX§5 ULTRA-RARE BOOK! §d§kXX",
    )

    /**
     * REGEX-TEST: §9Smite VII
     */
    val bookPattern by patternGroup.pattern(
        "book",
        "§9(?<enchant>.*)",
    )

    /**
     * REGEX-TEST: §dGuardian
     * REGEX-TEST: §9Guardian§e
     */
    private val petNamePattern by patternGroup.pattern(
        "guardianpet",
        "§[956d]Guardian.*",
    )

    fun hasGuardianPet(): Boolean = petNamePattern.matches(PetAPI.currentPet)
}
