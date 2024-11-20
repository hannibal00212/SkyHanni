package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils.enumJoinToPattern
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object UtilsPatterns {

    private val patternGroup = RepoPattern.group("utils")

    /** Examples:
     * §d§l§ka§r §d§lMYTHIC ACCESSORY §d§l§ka
     * §d§l§ka§r §d§lSHINY MYTHIC DUNGEON CHESTPLATE §d§l§ka
     * §c§l§ka§r §c§lVERY SPECIAL HATCESSORY §c§l§ka
     * §6§lSHINY LEGENDARY DUNGEON BOOTS
     * §6§lLEGENDARY DUNGEON BOOTS
     * §5§lEPIC BOOTS
     * §f§lCOMMON
     */
    val rarityLoreLinePattern by patternGroup.pattern(
        "item.lore.rarity.line",
        "^(?:§.){2,3}(?:.§. (?:§.){2})?(?:SHINY )?(?<rarity>" +
            enumJoinToPattern<LorenzRarity> { it.name.replace("_", " ") } +
            ") ?(?:DUNGEON )?(?<itemCategory>[^§]*)(?: (?:§.){3}.)?$",
    )

    val abiPhonePattern by patternGroup.pattern(
        "item.name.abiphone",
        ".{2}Abiphone .*",
    )

    /**
     * REGEX-TEST: §fEnchanted Book
     * REGEX-TEST: §f§f§fEnchanted Book
     */
    val enchantedBookPattern by patternGroup.pattern(
        "item.name.enchanted.book",
        "(?:§.)+Enchanted Book",
    )

    /**
     * REGEX-TEST: Obfuscated
     * REGEX-TEST: Hot Bait
     */
    val baitPattern by patternGroup.pattern(
        "item.name.bait",
        "^Obfuscated.*|.* Bait$",
    )

    val enchantmentNamePattern by patternGroup.pattern(
        "item.neuitems.enchantmentname",
        "^(?<format>(?:§.)*)(?<name>[^§]+) (?<level>[IVXL]+)(?: Book)?$",
    )

    /**
     * REGEX-TEST: duplex i
     * REGEX-TEST: ultimate wise v
     * REGEX-TEST: chimera 2
     */
    val cleanEnchantedNamePattern by patternGroup.pattern(
        "item.enchantment.clean.name",
        "(?i)(?<name>.*) (?<level>[IVXL]+|[0-9]+)",
    )

    val potionPattern by patternGroup.pattern(
        "item.name.potion",
        ".*Potion",
    )
    val readAmountBeforePattern by patternGroup.pattern(
        "item.amount.front",
        "(?: +§8(?:\\+§.)?(?<amount>[\\d.,]+[km]?)x? )?(?<name>.*)",
    )
    val readAmountAfterPattern by patternGroup.pattern(
        "item.amount.behind",
        "(?<name>(?:§.)*(?:[^§] ?)+)(?:§8x(?<amount>[\\d,]+))?",
    )

    /**
     * REGEX-TEST: §7Cost
     */
    val costLinePattern by patternGroup.pattern(
        "item.cost.line",
        "(?:§5§o)?§7Cost.*",
    )

    @Suppress("MaxLineLength")
    val timeAmountPattern by patternGroup.pattern(
        "time.amount",
        "(?:(?<y>\\d+) ?y(?:\\w* ?)?)?(?:(?<d>\\d+) ?d(?:\\w* ?)?)?(?:(?<h>\\d+) ?h(?:\\w* ?)?)?(?:(?<m>\\d+) ?m(?:\\w* ?)?)?(?:(?<s>\\d+) ?s(?:\\w* ?)?)?",
    )

    val playerChatPattern by patternGroup.pattern(
        "string.playerchat",
        "(?<important>.*?)(?:§[f7r])*: .*",
    )
    val chatUsernamePattern by patternGroup.pattern(
        "string.chatusername",
        "^(?:§\\w\\[§\\w\\d+§\\w] )?(?:(?:§\\w)+\\S )?(?<rankedName>(?:§\\w\\[\\w.+] )?(?:§\\w)?(?<username>\\w+))(?: (?:§\\w)?\\[.+?])?",
    )
    val isRomanPattern by RepoPattern.pattern(
        "string.isroman",
        "^M{0,3}(?:CM|CD|D?C{0,3})(?:XC|XL|L?X{0,3})(?:IX|IV|V?I{0,3})",
    )

    val sackPattern by patternGroup.pattern(
        "item.sack",
        ".*Sack",
    )

    /**
     * REGEX-TEST: Late Winter
     * REGEX-TEST: Early Spring
     * REGEX-TEST: Summer
     */
    val seasonPattern by patternGroup.pattern(
        "skyblocktime.season",
        "(?:Early |Late )?(?<season>Spring|Summer|Autumn|Winter)",
    )

    /**
     * REGEX-TEST: §l§r§e§lProfile: §r§aApple §r§7♲
     * REGEX-TEST: §l§r§e§lProfile: §r§aNot Allowed To Quit Skyblock Ever Again
     */
    val tabListProfilePattern by patternGroup.pattern(
        "tablist.profile",
        "(?:§.)+Profile: §r§a(?<profile>[\\w\\s]+[^ §]).*",
    )

    val shopOptionsPattern by patternGroup.pattern(
        "inventory.shopoptions",
        "Shop Trading Options",
    )

    val skyblockMenuGuiPattern by patternGroup.pattern(
        "inventory.skyblockmenu",
        "SkyBlock Menu",
    )
}
