package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ItemAddManager
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.ItemAddEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.message
import at.hannibal2.skyhanni.utils.ChatUtils.passedSinceSent
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.colorCodeToRarity
import at.hannibal2.skyhanni.utils.LorenzUtils.inAnyIsland
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatchers
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.isVowel
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.chat.Text.asComponent
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object RareDropMessages {

    private val repoGroup = RepoPattern.group("raredrop")
    private val petGroup = repoGroup.group("pet")

    /**
     * REGEX-TEST: §6§lPET DROP! §r§5Baby Yeti §r§b(+§r§b168% §r§b✯ Magic Find§r§b)
     * REGEX-TEST: §6§lPET DROP! §r§5Slug §6(§6+1300☘)
     */
    private val petDroppedPattern by petGroup.pattern(
        "droppedmessage",
        "(?<start>(?:§.)*PET DROP! )(?:§.)*§(?<rarityColor>.)(?<petName>[^§(.]+)(?<end> .*)",
    )

    /**
     * REGEX-TEST: §5§lGREAT CATCH! §r§bYou found a §r§7[Lvl 1] §r§aGuardian§r§b.
     */
    private val petFishedPattern by petGroup.pattern(
        "fishedmessage",
        "(?<start>(?:§.)*GREAT CATCH! (?:§.)*You found a (?:§.)*\\[Lvl 1] )(?:§.)*§(?<rarityColor>.)(?<petName>[^§(.]+)(?<end>.*)",
    )

    /**
     * REGEX-TEST: §aYou claimed a §5Tarantula Pet§a! §r§aYou can manage your Pets in the §r§fPets Menu§r§a in your §r§fSkyBlock Menu§r§a.
     */
    private val petClaimedPattern by petGroup.pattern(
        "claimedmessage",
        "(?<start>(?:§.)*You claimed a )(?:§.)*§(?<rarityColor>.)(?<petName>[^§(.]+)(?<end>.*You can manage your Pets.*)",
    )

    /**
     * REGEX-TEST: §b[MVP§r§c+§r§b] Empa_§r§f §r§ehas obtained §r§a§r§7[Lvl 1] §r§6Bal§r§e!
     */
    private val petObtainedPattern by petGroup.pattern(
        "obtainedmessage",
        "(?<start>.*has obtained (?:§.)*\\[Lvl 1] )(?:§.)*§(?<rarityColor>.)(?<petName>[^§(.]+)(?<end>.*)",
    )

    /**
     * REGEX-TEST: §e[NPC] Oringo§f: §b✆ §f§r§8• §fBlue Whale Pet
     * REGEX-TEST: §e[NPC] Oringo§f: §b✆ §f§r§8• §5Giraffe Pet
     */
    private val oringoPattern by petGroup.pattern(
        "oringomessage",
        "(?<start>§e\\[NPC] Oringo§f: §b✆ §f§r§8• )§(?<rarityColor>.)(?<petName>[^§(.]+)(?<end> Pet)"
    )

    /**
     * REGEX-TEST: §6§lRARE DROP! §r§fEnchanted Book §r§b(+§r§b208% §r§b✯ Magic Find§r§b)
     */
    private val enchantedBookPattern by repoGroup.pattern(
        "enchantedbook",
        "(?<start>(?:§.)+RARE DROP!) (?<color>(?:§.)*)Enchanted Book (?<end>§r§b\\([+](?:§.)*(?<mf>\\d*)% §r§b✯ Magic Find§r§b\\)).*"
    )

    private val petPatterns = listOf(
        petDroppedPattern, petFishedPattern, petClaimedPattern, petObtainedPattern, oringoPattern,
    )

    private val ignoredBookIslands = setOf(
        IslandType.DARK_AUCTION,
        IslandType.DUNGEON_HUB,
        IslandType.CATACOMBS,
    )

    private val userLuck get() = ProfileStorageData.playerSpecific?.limbo?.userLuck

    private val config get() = SkyHanniMod.feature.chat.rareDropMessages

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!config.petRarity) return

        petPatterns.matchMatchers(event.message) {
            var start = group("start")
            val rarityColor = group("rarityColor")
            val rarityName = colorCodeToRarity(rarityColor.first()).uppercase()
            val petName = group("petName")
            val end = group("end")
            if (start.endsWith("a ") && rarityName.first().isVowel())
                start = start.substring(0..start.length - 2) + "n "

            event.chatComponent = ChatComponentText(
                "$start§$rarityColor§l$rarityName §$rarityColor$petName$end"
            )
        }
    }

    @SubscribeEvent
    fun onItemAdd(event: ItemAddEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (event.amount != 1 || event.source != ItemAddManager.Source.ITEM_ADD) return
        if (!config.enchantedBook) return
        val internalName = event.internalName
        val category = internalName.getItemStackOrNull()?.getItemCategoryOrNull() ?: return
        if (category != ItemCategory.ENCHANTED_BOOK) return
        if (inAnyIsland(ignoredBookIslands)) return

        val anyRecent = ChatUtils.chatLines.none {
            it.passedSinceSent() < 1.seconds &&
                (enchantedBookPattern.matches(it.message) || internalName.itemName in it.message.removeColor())
        }

        if (anyRecent) {
            var message = "§r§6§lRARE DROP! ${internalName.itemName}"
            userLuck?.takeIf { it != 0f }?.let { luck ->
                var luckString = luck.roundTo(2).toString()
                if (luck > 0) luckString = "+$luckString"
                message += " §a($luckString ✴ SkyHanni User Luck"
            }
            ChatUtils.chat(message, prefix = false)
            return
        }
        ChatUtils.editFirstMessage(
            component = { it.formattedText.replace("Enchanted Book", internalName.itemName).asComponent() },
            "enchanted book",
            predicate = { it.passedSinceSent() < 1.seconds && enchantedBookPattern.matches(it.message) }
        )

    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(47, "chat.petRarityDropMessage", "chat.rareDropMessages.petRarity")
    }
}
