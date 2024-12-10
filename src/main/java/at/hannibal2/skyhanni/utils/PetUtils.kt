package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.CurrentPetAPI
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.NEUPetData
import at.hannibal2.skyhanni.data.jsonobjects.repo.NEUPetsJson
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack

@SkyHanniModule
object PetUtils {
    private val patternGroup = RepoPattern.group("misc.pet")
    private const val FORGE_BACK_SLOT = 48

    private var baseXpLevelReqs: List<Int> = listOf()
    private var customXpLevelReqs: Map<String, NEUPetData>? = null

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: §e⭐ §7[Lvl 200] §6Golden Dragon§d ✦
     * REGEX-TEST: ⭐ [Lvl 100] Black Cat ✦
     */
    val petItemNamePattern by CurrentPetAPI.patternGroup.pattern(
        "item.name",
        "(?<favorite>(?:§.)*⭐ )?(?:§.)*\\[Lvl (?<level>\\d+)] (?<name>.*)",
    )

    /**
     * REGEX-TEST: Pets (1/3)
     * REGEX-TEST: Pets
     * REGEX-TEST: Pets (1/4)
     * REGEX-TEST: Pets (1/2)
     */
    private val petMenuPattern by patternGroup.pattern(
        "menu.title",
        "Pets(?: \\(\\d+/\\d+\\) )?",
    )

    /**
     * REGEX-TEST: §7[Lvl 1➡200] §6Golden Dragon
     * REGEX-TEST: §7[Lvl {LVL}] §6Golden Dragon
     */
    private val neuRepoPetItemNamePattern by CurrentPetAPI.patternGroup.pattern(
        "item.name.neu.format",
        "(?:§f§f)?§7\\[Lvl (?:1➡(?:100|200)|\\{LVL})] (?<name>.*)",
    )

    /**
     * REGEX-TEST: §7To Select Process (Slot #2)
     * REGEX-TEST: §7To Select Process (Slot #4)
     * REGEX-TEST: §7To Select Process (Slot #7)
     * REGEX-TEST: §7To Select Process
     */
    private val forgeBackMenuPattern by CurrentPetAPI.patternGroup.pattern(
        "menu.forge.goback",
        "§7To Select Process(?: \\(Slot #\\d\\))?",
    )
    // </editor-fold>

    // <editor-fold desc="Helpers">
    fun isPetMenu(inventoryTitle: String, inventoryItems: Map<Int, ItemStack>): Boolean {
        if (!petMenuPattern.matches(inventoryTitle)) return false

        // Otherwise make sure they're not in the Forge menu looking at pets
        return inventoryItems[FORGE_BACK_SLOT]?.getLore().orEmpty().none {
            forgeBackMenuPattern.matches(it)
        }
    }

    fun getCleanName(nameWithLevel: String): String? {
        petItemNamePattern.matchMatcher(nameWithLevel) {
            return group("name")
        }
        neuRepoPetItemNamePattern.matchMatcher(nameWithLevel) {
            return group("name")
        }

        return null
    }

    fun getFakePetLine(level: Int, rarity: LorenzRarity, petName: String, skin: String? = null): String {
        return "§r§7[Lvl $level] §r${rarity.chatColorCode}$petName§r${skin.orEmpty()}"
    }

    fun rarityByColorGroup(color: String): LorenzRarity = LorenzRarity.getByColorCode(color[0])
        ?: ErrorManager.skyHanniError(
            "Unknown rarity",
            Pair("rarity", color),
        )

    private fun levelToXPCommand(input: Array<String>) {
        if (input.size < 3) {
            ChatUtils.userError("Usage: /shcalcpetxp <level> <rarity> <pet>")
            return
        }

        val level = input[0].toIntOrNull()
        if (level == null) {
            ChatUtils.userError("Invalid level '${input[0]}'.")
            return
        }
        val rarity = LorenzRarity.getByName(input[1])
        if (rarity == null) {
            ChatUtils.userError("Invalid rarity '${input[1]}'.")
            return
        }

        val petName = input.slice(2..<input.size).joinToString(" ")
        val xp: Double = levelToXP(level, rarity, petName) ?: run {
            ChatUtils.userError("Invalid level or rarity.")
            return
        }
        ChatUtils.chat(xp.addSeparators())
        return
    }

    fun levelToXP(level: Int, rarity: LorenzRarity, petName: String): Double? {
        val newPetName = petName.toInternalName().toString()

        val rarityOffset = getRarityOffset(rarity, newPetName) ?: return null
        if (!isValidLevel(level, newPetName)) return null

        val xpList = baseXpLevelReqs + getCustomLeveling(newPetName)

        return xpList.slice(0 + rarityOffset..<level + rarityOffset - 1).sum().toDouble()
    }

    private fun isValidLevel(level: Int, petName: String): Boolean {
        val petsData = customXpLevelReqs ?: run {
            ErrorManager.skyHanniError("NEUPetsData is null")
        }

        val maxLevel = petsData[petName]?.maxLevel ?: return false
        return maxLevel >= level
    }

    private fun getCustomLeveling(petName: String): List<Int> {
        return customXpLevelReqs?.get(petName)?.petLevels.orEmpty()
    }

    private fun getRarityOffset(rarity: LorenzRarity, petName: String): Int? {
        val petsData = customXpLevelReqs ?: run {
            ErrorManager.skyHanniError("NEUPetsData is null")
        }

        return if (petName in petsData.keys) {
            val petData = petsData[petName]
            petData?.rarityOffset?.get(rarity)
        } else {
            when (rarity) {
                LorenzRarity.COMMON -> 0
                LorenzRarity.UNCOMMON -> 6
                LorenzRarity.RARE -> 11
                LorenzRarity.EPIC -> 16
                LorenzRarity.LEGENDARY -> 20
                LorenzRarity.MYTHIC -> 20
                else -> {
                    ChatUtils.userError("Invalid Rarity \"${rarity.name}\"")
                    null
                }
            }
        }
    }
    // </editor-fold>

    @HandleEvent
    fun onNEURepoReload(event: NeuRepositoryReloadEvent) {
        val data = event.getConstant<NEUPetsJson>("pets")
        baseXpLevelReqs = data.petLevels
        customXpLevelReqs = data.customPetLeveling
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shpetxp") {
            description = "Calculates the pet xp from a given level and rarity."
            category = CommandCategory.DEVELOPER_TEST
            callback { levelToXPCommand(it) }
        }
    }
}
