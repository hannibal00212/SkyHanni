package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.PetData
import at.hannibal2.skyhanni.data.PetData.Companion.parsePetAsItem
import at.hannibal2.skyhanni.data.PetData.Companion.parsePetData
import at.hannibal2.skyhanni.data.PetData.Companion.parsePetDataLists
import at.hannibal2.skyhanni.data.PetData.Companion.petNameToInternalName
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.skyblock.PetChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.PetUtils.isPetMenu
import at.hannibal2.skyhanni.utils.PetUtils.levelToXp
import at.hannibal2.skyhanni.utils.PetUtils.rarityByColorGroup
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.hasGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.convertToUnformatted
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object CurrentPetAPI {
    private val config get() = SkyHanniMod.feature.misc.pets
    val patternGroup = RepoPattern.group("misc.pet")

    private var inPetMenu = false
    private var lastPetLine: String? = null

    var currentPet: PetData?
        get() = ProfileStorageData.profileSpecific?.currentPetData?.takeIf { it.isInitialized() }
        set(value) {
            ProfileStorageData.profileSpecific?.currentPetData = value
        }

    fun isCurrentPet(petName: String): Boolean = currentPet?.cleanName?.contains(petName) ?: false

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST:  §r§7[Lvl 100] §r§dEndermite
     * REGEX-TEST:  §r§7[Lvl 200] §r§8[§r§6108§r§8§r§4✦§r§8] §r§6Golden Dragon
     * REGEX-TEST:  §r§7[Lvl 100] §r§dBlack Cat§r§d ✦
     */
    @Suppress("MaxLineLength")
    private val petWidgetPattern by patternGroup.pattern(
        "widget.pet",
        "^ §r§7\\[Lvl (?<level>\\d+)](?: (?:§.)+\\[(?:§.)+(?<overflow>\\d+)(?:§.)+✦(?:§.)+])? §r§(?<rarity>.)(?<name>[\\w ]+)(?:§r(?<skin>§. ✦))?\$",
    )

    /**
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 100] §6Scatha§e! §a§lVIEW RULE
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 99] §6Flying Fish§e! §a§lVIEW RULE
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 100] §dBlack Cat§d ✦§e! §a§lVIEW RULE
     */
    private val autopetMessagePattern by patternGroup.pattern(
        "chat.autopet",
        "^§cAutopet §eequipped your §7(?<pet>\\[Lvl \\d{1,3}] §.[\\w ]+)(?:§. ✦)?§e! §a§lVIEW RULE\$",
    )

    /**
     * REGEX-TEST: §aYour pet is now holding §r§9Bejeweled Collar§r§a.
     */
    private val petItemMessagePattern by patternGroup.pattern(
        "chat.pet.item.equip",
        "^§aYour pet is now holding §r(?<petItem>§.[\\w -]+)§r§a\\.\$",
    )

    /**
     * REGEX-TEST: §7§7Selected pet: §6Hedgehog
     */
    private val inventorySelectedPetPattern by patternGroup.pattern(
        "inventory.selected",
        "§7§7Selected pet: §?(?<rarity>.)?(?<pet>.*)"
    )

    /**
     * REGEX-TEST: §7Progress to Level 91: §e0%
     * REGEX-TEST: §7Progress to Level 147: §e37.1%
     * REGEX-TEST: §b§lMAX LEVEL
     */
    private val inventorySelectedProgressPattern by patternGroup.pattern(
        "inventory.selected.progress",
        "§b§lMAX LEVEL|§7Progress to Level (?<level>\\d+): §e(?<percentage>[\\d.]+)%"
    )

    /**
     * REGEX-TEST: §2§l§m             §f§l§m            §r §e713,241.8§6/§e1.4M
     * REGEX-TEST: §2§l§m          §f§l§m               §r §e699,742.8§6/§e1.9M
     * REGEX-TEST: §f§l§m                         §r §e0§6/§e660
     * REGEX-TEST: §8▸ 30,358,983 XP'
     */
    private val inventorySelectedXpPattern by patternGroup.pattern(
        "inventory.selected.xp",
        "(?:§8▸ |(?:§.§l§m *)*)(?:§r §e)?(?<current>[\\d,.kM]+)(?:§6\\/§e)?(?<next>[\\d,.kM]+)?"
    )

    /**
     * REGEX-TEST:  §r§7No pet selected
     * REGEX-TEST:  §r§6Washed-up Souvenir
     * REGEX-TEST:  §r§9Dwarf Turtle Shelmet
     */
    private val widgetStringPattern by patternGroup.pattern(
        "widget.string",
        "^ §r(?<string>§.[\\w -]+)\$",
    )

    /**
     * REGEX-TEST:  §r§b§lMAX LEVEL
     * REGEX-TEST:  §r§6+§r§e21,248,020.7 XP
     * REGEX-TEST:  §r§e15,986.6§r§6/§r§e29k XP §r§6(53.6%)
     */
    @Suppress("MaxLineLength")
    private val xpWidgetPattern by patternGroup.pattern(
        "widget.xp",
        "^ §r§.(?:§l(?<max>MAX LEVEL)|\\+§r§e(?<overflow>[\\d,.]+) XP|(?<currentXP>[\\d,.]+)§r§6/§r§e(?<maxXP>[\\d.km]+) XP §r§6\\((?<percentage>[\\d.%]+)\\))$",
    )

    /**
     * REGEX-TEST: §r, §aEquip: §r, §7[Lvl 99] §r, §6Flying Fish
     * REGEX-TEST: §r, §aEquip: §r, §e⭐ §r, §7[Lvl 100] §r, §dBlack Cat§r, §d ✦
     * REGEX-TEST: §r, §aEquip: §r, §7[Lvl 47] §r, §5Lion
     */
    private val autopetHoverPetPattern by patternGroup.pattern(
        "chat.autopet.hover.pet",
        "^§r, §aEquip: §r,(?: §e⭐ §r,)? §7\\[Lvl (?<level>\\d+)] §r, §(?<rarity>.)(?<pet>[\\w ]+)(?:§r, (?<skin>§. ✦))?\$",
    )

    /**
     * REGEX-TEST: §r, §aHeld Item: §r, §9Mining Exp Boost§r]
     * REGEX-TEST: §r, §aHeld Item: §r, §5Lucky Clover§r]
     * REGEX-TEST: §r, §aHeld Item: §r, §5Fishing Exp Boost§r]
     */
    private val autopetHoverPetItemPattern by patternGroup.pattern(
        "chat.autopet.hover.item",
        "^§r, §aHeld Item: §r, (?<item>§.[\\w -]+)§r]\$",
    )

    /**
     * REGEX-TEST: §aYou despawned your §r§6Golden Dragon§r§a!
     * REGEX-TEST: §aYou despawned your §r§6Silverfish§r§5 ✦§r§a!
     */
    private val chatDespawnPattern by patternGroup.pattern(
        "chat.despawn",
        "§aYou despawned your §r.*§r§a!",
    )

    /**
     * REGEX-TEST: §aYou summoned your §r§6Silverfish§r§5 ✦§r§a!
     * REGEX-TEST: §aYou summoned your §r§6Golden Dragon§r§a!
     */
    private val chatSpawnPattern by patternGroup.pattern(
        "chat.spawn",
        "§aYou summoned your §r(?<pet>.*)§r§a!"
    )

    /**
     * REGEX-TEST: §e⭐ §7[Lvl 100] §6Ender Dragon
     * REGEX-TEST: §e⭐ §7[Lvl 100] §dBlack Cat§d ✦
     * REGEX-TEST: §7[Lvl 100] §6Mole
     */
    val petNameMenuPattern by patternGroup.pattern(
        "menu.pet.name",
        "^(?:§e(?<favorite>⭐) )?(?:§.)*\\[Lvl (?<level>\\d+)] §(?<rarity>.)(?<name>[\\w ]+)(?<skin>§. ✦)?\$",
    )

    /**
     * REGEX-TEST: §7§cClick to despawn!
     */
    val petDespawnMenuPattern by patternGroup.pattern(
        "menu.pet.despawn",
        "§7§cClick to despawn!",
    )
    // </editor-fold>

    // <editor-fold desc="Helpers">
    private fun updatePet(newPet: PetData?) {
        if (newPet == currentPet) return
        val oldPet = currentPet
        currentPet = newPet
        if (SkyHanniMod.feature.dev.debug.petEventMessages) {
            ChatUtils.debug("oldPet: " + oldPet.toString().convertToUnformatted())
            ChatUtils.debug("newPet: " + newPet?.toString()?.convertToUnformatted())
        }
        PetChangeEvent(oldPet, newPet).post()
    }

    private fun handlePetMessageBlock(event: LorenzChatEvent) {
        if (!config.hideAutopet) return
        val spawnMatches = chatSpawnPattern.matches(event.message)
        val despawnMatches = chatDespawnPattern.matches(event.message)
        val autoPetMatches = autopetMessagePattern.matches(event.message)
        if (spawnMatches || despawnMatches || autoPetMatches) {
            event.blockedReason = "pets"
        }
    }
    // </editor-fold>

    // <editor-fold desc="Pet Data Extractors (Widget)">
    private fun handleWidgetPetLine(line: String): PetData? = petWidgetPattern.matchMatcher(line) {
        val rarity = rarityByColorGroup(group("rarity"))
        val petName = groupOrNull("name").orEmpty()
        val level = groupOrNull("level")?.toInt() ?: 0
        val xp = levelToXp(level, rarity, petName) ?: return null

        return PetData(
            petItem = petNameToInternalName(petName, rarity),
            skinItem = null,
            heldItem = null,
            cleanName = petName,
            rarity = rarity,
            level = level,
            xp = xp,
        )
    }

    private fun handleWidgetStringLine(line: String): NEUInternalName? = widgetStringPattern.matchMatcher(line) {
        val string = group("string")
        if (string == "No pet selected") {
            updatePet(null)
            return null
        }
        return NEUInternalName.fromItemNameOrNull(string)
    }

    private fun handleWidgetXPLine(line: String): Double? = xpWidgetPattern.matchMatcher(line) {
        if (hasGroup("max")) return null

        group("overflow")?.formatDouble() ?: group("currentXP")?.formatDouble()
    }
    // </editor-fold>

    // <editor-fold desc="Pet Data Extractors (AutoPet)">
    private fun onAutopetMessage(event: LorenzChatEvent) {
        val hoverMessage = event.chatComponent.hover?.siblings?.joinToString("")?.split("\n") ?: return

        val (petData, _) = parsePetData(
            hoverMessage,
            { readAutopetItemMessage(it) },
            { null }, // No overflow XP handling in this case
            { readAutopetMessage(it) }
        ) ?: return

        updatePet(petData)
    }

    private fun readAutopetMessage(string: String): PetData? = autopetHoverPetPattern.matchMatcher(string) {
        val level = group("level").toInt()
        val rarity = rarityByColorGroup(group("rarity"))
        val petName = group("pet")

        return PetData(
            petItem = petNameToInternalName(petName, rarity),
            cleanName = petName,
            rarity = rarity,
            level = level,
            xp = levelToXp(level, rarity, petName) ?: 0.0,
        )
    }

    private fun readAutopetItemMessage(string: String): NEUInternalName? = autopetHoverPetItemPattern.matchMatcher(string) {
        NEUInternalName.fromItemNameOrNull(group("item"))
    }
    // </editor-fold>

    // <editor-fold desc="Pet Data Extractors (Selected Pet)">
    private fun extractSelectedPetData(lore: List<String>): Triple<Int, LorenzRarity, String>? {
        val level = inventorySelectedProgressPattern.firstMatchGroup(lore, "level")?.toInt()
        val rarity = inventorySelectedPetPattern.firstMatchGroup(lore, "rarity")?.let { rarityByColorGroup(it) }
        val petName = inventorySelectedPetPattern.firstMatchGroup(lore, "pet")

        return if (level != null && rarity != null && petName != null) {
            Triple(level, rarity, petName)
        } else null
    }

    private fun handleSelectedPetName(lore: List<String>): NEUInternalName? = inventorySelectedPetPattern.firstMatcher(lore) {
        val (_, rarity, petName) = extractSelectedPetData(lore) ?: return null
        petNameToInternalName(petName, rarity)
    }

    private fun handleSelectedPetOverflowXp(lore: List<String>): Double? {
        // Only have overflow if `next` group is absent
        if (inventorySelectedXpPattern.firstMatchGroup(lore, "next") != null) return 0.0
        val (level, rarity, petName) = extractSelectedPetData(lore) ?: return null
        val maxXpNeeded = levelToXp(level, rarity, petName)
        val currentXp = inventorySelectedXpPattern.firstMatchGroup(lore, "current")?.formatDouble() ?: 0.0
        return maxXpNeeded?.minus(currentXp) ?: 0.0
    }

    private fun handleSelectedPetData(lore: List<String>): PetData? {
        val (level, rarity, petName) = extractSelectedPetData(lore) ?: return null
        val partialXp = inventorySelectedXpPattern.firstMatchGroup(lore, "current")?.formatDouble() ?: 0.0
        val nextExists = inventorySelectedXpPattern.firstMatchGroup(lore, "next") != null
        val totalXp = partialXp + if (nextExists) (levelToXp(level, rarity, petName) ?: return null) else 0.0
        return PetData(
            petItem = petNameToInternalName(petName, rarity),
            cleanName = petName,
            rarity = rarity,
            heldItem = null,
            level = level,
            xp = totalXp,
        )
    }
    // </editor-fold>

    // <editor-fold desc="Event Handlers">
    @HandleEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.PET)) return

        val newPetLine = petWidgetPattern.firstMatches(event.lines)?.trim() ?: return
        if (newPetLine == lastPetLine) return
        lastPetLine = newPetLine

        val (petData, overflowXP) = parsePetData(
            event.lines,
            { handleWidgetStringLine(it) },
            { handleWidgetXPLine(it) },
            { handleWidgetPetLine(it) }
        ) ?: return

        updatePet(petData.copy(xp = petData.xp?.plus(overflowXP)))
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        handlePetMessageBlock(event)
        if (autopetMessagePattern.matches(event.message)) {
            onAutopetMessage(event)
            return
        }
        petItemMessagePattern.matchMatcher(event.message) {
            val item = NEUInternalName.fromItemNameOrNull(group("petItem")) ?: ErrorManager.skyHanniError(
                "Couldn't parse pet item name.",
                Pair("message", event.message),
                Pair("item", group("petItem")),
            )
            val newPet = currentPet?.copy(heldItem = item) ?: return
            updatePet(newPet)
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inPetMenu = isPetMenu(event.inventoryName, event.inventoryItems)
        if (!inPetMenu) return

        val lore = event.inventoryItems[4]?.getLore() ?: return
        val (petData, overflowXp) = parsePetDataLists(
            lore,
            { handleSelectedPetName(lore) },
            { handleSelectedPetOverflowXp(lore) },
            { handleSelectedPetData(lore) }
        ) ?: return
        updatePet(petData.copy(xp = petData.xp?.plus(overflowXp)))
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inPetMenu = false
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!inPetMenu) return
        if (event.clickTypeEnum != GuiContainerEvent.ClickType.NORMAL) return
        val category = event.item?.getItemCategoryOrNull() ?: return
        if (category != ItemCategory.PET) return

        updatePet(parsePetAsItem(event.item))
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("PetAPI")
        if (currentPet?.isInitialized() == false) {
            event.addIrrelevant("no pet equipped")
            return
        }
        event.addIrrelevant {
            add("petName: '${currentPet?.petItem ?: ""}'")
            add("petRarity: '${currentPet?.rarity?.rawName.orEmpty()}'")
            add("petItem: '${currentPet?.heldItem ?: ""}'")
            add("petLevel: '${currentPet?.level ?: 0}'")
            add("petXP: '${currentPet?.xp ?: 0.0}'")
        }
    }
    // </editor-fold>
}
