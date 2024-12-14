package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.TablistFooterUpdateEvent
import at.hannibal2.skyhanni.events.effects.EffectDurationChangeEvent
import at.hannibal2.skyhanni.events.effects.EffectDurationChangeType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@SkyHanniModule
object EffectAPI {

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §cGod Potion§f: 4d
     */
    private val godPotTabPattern by RepoPattern.pattern(
        "stats.tabpatterns.godpot",
        "(?:§.)*God Potion(?:§.)*: (?:§.)*(?<time>[dhms0-9 ]+)(?:§.)*",
    )

    /**
     * REGEX-TEST: §a§lSCHLURP! §r§eThe effects of the §r§9Hot Chocolate Mixin §r§ehave been extended by §r§986h 24m§r§e!
     * They will pause if your §r§cGod Potion §r§eexpires.
     */
    private val hotChocolateMixinConsumePattern by RepoPattern.pattern(
        "stats.chatpatterns.hotchocolatemixinconsume",
        "(?:§.)+.*(?:§.)+Hot Chocolate Mixin ?(?:§.)+.*extended by (?:§.)+(?<time>[dhms0-9 ]*)(?:§.)+!.*",
    )

    /**
     * REGEX-TEST: §a§lGULP! §r§eThe §r§cGod Potion §r§egrants you powers for §r§928h 48m§r§e!
     * REGEX-TEST: §a§lSIP! §r§eThe §r§cGod Potion §r§egrants you powers for §r§928h 48m§r§e!
     * REGEX-TEST: §a§lSLURP! §r§eThe §r§cGod Potion §r§egrants you powers for §r§928h 48m§r§e!
     */
    private val godPotConsumePattern by RepoPattern.pattern(
        "stats.chatpatterns.godpotconsume",
        "(?:§.)+.*(?:§.)+God Potion ?(?:§.)+.*grants you powers for (?:§.)+(?<time>[dhms0-9 ]*)(?:§.)+!.*",
    )

    /**
     * REGEX-TEST: (1/2) Active Effects
     */
    private val effectsInventoryPattern by RepoPattern.pattern(
        "inventory.effects",
        "(?:§.)?(?:[(\\d\\/)]* )?Active Effects",
    )

    /**
     * REGEX-TEST: §aFilter
     */
    private val filterPattern by RepoPattern.pattern(
        "inventory.effects.filter",
        "§aFilter",
    )

    /**
     * REGEX-TEST: §b▶ God Potion Effects
     */
    private val godPotEffectsFilterSelectPattern by RepoPattern.pattern(
        "inventory.effects.filtergodpotselect",
        "§b▶ God Potion Effects",
    )

    /**
     * REGEX-TEST: §7Remaining: §f105:01:34
     */
    private val potionRemainingLoreTimerPattern by RepoPattern.pattern(
        "inventory.effects.effecttimeleft",
        "§7Remaining: §f(?<time>[\\d:]+)",
    )
    // </editor-fold>

    private val profileStorage get() = ProfileStorageData.profileSpecific

    // TODO move the whole list into the repo
    enum class NonGodPotEffect(
        val tabListName: String,
        val isMixin: Boolean = false,
        val inventoryItemName: String = tabListName,
    ) {

        SMOLDERING("§aSmoldering Polarization I"),
        GLOWY("§2Mushed Glowy Tonic I"),
        WISP("§bWisp's Ice-Flavored Water I"),
        GOBLIN("§2King's Scent I"),

        INVISIBILITY("§8Invisibility I"), // when wearing sorrow armor

        REV("§cZombie Brain Mixin", true),
        TARA("§6Spider Egg Mixin", true),
        SVEN("§bWolf Fur Mixin", true),
        VOID("§6Ender Portal Fumes", true),
        BLAZE("§fGabagoey", true),
        GLOWING_MUSH("§2Glowing Mush Mixin", true),
        HOT_CHOCOLATE("§6Hot Chocolate Mixin", true),

        DEEP_TERROR("§4Deepterror", true),

        GREAT_SPOOK("§fGreat Spook I", inventoryItemName = "§fGreat Spook Potion"),

        HARVEST_HARBINGER("§6Harvest Harbinger V"),

        PEST_REPELLENT("§6Pest Repellent I§r"),
        PEST_REPELLENT_MAX("§6Pest Repellent II"),

        CURSE_OF_GREED("§4Curse of Greed I"),

        COLD_RESISTANCE_4("§bCold Resistance IV"),

        POWDER_PUMPKIN("§fPowder Pumpkin I"),
        FILET_O_FORTUNE("§fFilet O' Fortune I"),
        CHILLED_PRISTINE_POTATO("§fChilled Pristine Potato I"),
    }

    // Todo : cleanup and add support for poison candy I, and add support for splash / other formats
    @SubscribeEvent
    @Suppress("MaxLineLength")
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        hotChocolateMixinConsumePattern.matchMatcher(event.message) {
            val durationAdded = TimeUtils.getDuration(group("time"))
            EffectDurationChangeEvent(
                NonGodPotEffect.HOT_CHOCOLATE,
                EffectDurationChangeType.ADD,
                durationAdded,
            ).post()
        }
        godPotConsumePattern.matchMatcher(event.message) {
            val durationAdded = TimeUtils.getDuration(group("time"))
            val existingValue = profileStorage?.godPotExpiryTime?.takeIfInitialized() ?: SimpleTimeMark.now()
            profileStorage?.godPotExpiryTime = existingValue + durationAdded
        }

        var effect: NonGodPotEffect? = null
        var changeType: EffectDurationChangeType? = null
        var duration: Duration? = null

        if (event.message == "§aYou ate a §r§aRe-heated Gummy Polar Bear§r§a!") {
            effect = NonGodPotEffect.SMOLDERING
            changeType = EffectDurationChangeType.SET
            duration = 1.hours
        }

        if (event.message == "§a§lBUFF! §fYou have gained §r§2Mushed Glowy Tonic I§r§f! Press TAB or type /effects to view your active effects!") {
            effect = NonGodPotEffect.GLOWY
            changeType = EffectDurationChangeType.SET
            duration = 1.hours
        }

        if (event.message == "§a§lBUFF! §fYou splashed yourself with §r§bWisp's Ice-Flavored Water I§r§f! Press TAB or type /effects to view your active effects!") {
            effect = NonGodPotEffect.WISP
            changeType = EffectDurationChangeType.SET
            duration = 5.minutes
        }

        if (event.message == "§eYou consumed a §r§fGreat Spook Potion§r§e!") {
            effect = NonGodPotEffect.GREAT_SPOOK
            changeType = EffectDurationChangeType.SET
            duration = 24.hours
        }

        if (event.message == "§a§lBUFF! §fYou have gained §r§6Harvest Harbinger V§r§f! Press TAB or type /effects to view your active effects!") {
            effect = NonGodPotEffect.HARVEST_HARBINGER
            changeType = EffectDurationChangeType.SET
            duration = 25.minutes
        }

        if (event.message == "§a§lYUM! §r§6Pests §r§7will now spawn §r§a2x §r§7less while you break crops for the next §r§a60m§r§7!") {
            effect = NonGodPotEffect.PEST_REPELLENT
            changeType = EffectDurationChangeType.SET
            duration = 1.hours
        }

        if (event.message == "§a§lYUM! §r§6Pests §r§7will now spawn §r§a4x §r§7less while you break crops for the next §r§a60m§r§7!") {
            effect = NonGodPotEffect.PEST_REPELLENT_MAX
            changeType = EffectDurationChangeType.SET
            duration = 1.hours
        }

        if (event.message == "§e[NPC] §6King Yolkar§f: §rThese eggs will help me stomach my pain.") {
            effect = NonGodPotEffect.CURSE_OF_GREED
            changeType = EffectDurationChangeType.SET
            duration = 20.minutes
        }

        if (event.message == "§cThe Goblin King's §r§afoul stench §r§chas dissipated!") {
            effect = NonGodPotEffect.GOBLIN
            changeType = EffectDurationChangeType.REMOVE
        }

        effect?.let {
            if (changeType == null) return@let
            EffectDurationChangeEvent(it, changeType, duration).post()
        }
    }

    @HandleEvent
    fun onTabUpdate(event: TablistFooterUpdateEvent) {
        if (!LorenzUtils.inSkyBlock) return
        for (line in event.footer.split("\n")) {
            godPotTabPattern.matchMatcher(line) {
                profileStorage?.godPotExpiryTime = SimpleTimeMark.now() + TimeUtils.getDuration(group("time"))
            }
            for (effect in NonGodPotEffect.entries) {
                val tabListName = effect.tabListName
                if ("$line§r".startsWith(tabListName)) {
                    val string = line.substring(tabListName.length)
                    try {
                        val duration = TimeUtils.getDuration(string.split("§f")[1])
                        EffectDurationChangeEvent(effect, EffectDurationChangeType.SET, duration).post()
                    } catch (e: IndexOutOfBoundsException) {
                        ChatUtils.debug("Error while reading non god pot effects from tab list! line: '$line'")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!LorenzUtils.inSkyBlock || !event.isGodPotEffectsFilterSelect()) return

        val potionLore = event.inventoryItems[10]?.getLore() ?: run {
            // No active god pot effects found, reset the expiry time
            profileStorage?.godPotExpiryTime = SimpleTimeMark.farPast()
            return
        }

        val expiryDuration = potionRemainingLoreTimerPattern.firstMatcher(potionLore) {
            TimeUtils.getDuration(group("time"))
        } ?: return

        profileStorage?.godPotExpiryTime = SimpleTimeMark.now() + expiryDuration
    }

    private fun InventoryUpdatedEvent.isGodPotEffectsFilterSelect(): Boolean =
        effectsInventoryPattern.matches(this.inventoryName) &&
            this.inventoryItems.values.firstOrNull {
                filterPattern.matches(it.displayName)
            }?.getLore()?.any {
                godPotEffectsFilterSelectPattern.matches(it)
            } ?: false

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!event.inventoryName.endsWith("Active Effects")) return

        for (stack in event.inventoryItems.values) {
            val name = stack.name
            for (effect in NonGodPotEffect.entries) {
                if (!name.contains(effect.inventoryItemName)) continue
                for (line in stack.getLore()) {
                    if (!line.contains("Remaining") || line == "§7Time Remaining: §aCompleted!" || line.contains("Remaining Uses")) continue
                    val duration = try {
                        TimeUtils.getDuration(line.split("§f")[1])
                    } catch (e: IndexOutOfBoundsException) {
                        ErrorManager.logErrorWithData(
                            e, "Error while reading Non God-Potion effects from tab list",
                            "line" to line,
                        )
                        continue
                    }
                    EffectDurationChangeEvent(effect, EffectDurationChangeType.SET, duration).post()
                }
            }
        }
    }
}
