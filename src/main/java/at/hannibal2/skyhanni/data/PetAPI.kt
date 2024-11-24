package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

@SkyHanniModule
object PetAPI {
    private val patternGroup = RepoPattern.group("misc.pet")

    /**
     * REGEX-TEST: Pets
     * REGEX-TEST: Pets (1/2)
     */
    private val petMenuPattern by patternGroup.pattern(
        "menu.title",
        "Pets(?: \\(\\d+/\\d+\\) )?",
    )

    /**
     * REGEX-TEST: §e⭐ §7[Lvl 200] §6Golden Dragon§d ✦
     * REGEX-TEST: ⭐ [Lvl 100] Black Cat ✦
     */
    private val petItemName by patternGroup.pattern(
        "item.name",
        "(?<favorite>(?:§.)*⭐ )?(?:§.)*\\[Lvl (?<level>\\d+)] (?<name>.*)",
    )

    /**
     * REGEX-TEST: §7[Lvl 1➡200] §6Golden Dragon
     * REGEX-TEST: §7[Lvl {LVL}] §6Golden Dragon
     */
    private val neuRepoPetItemName by patternGroup.pattern(
        "item.name.neu.format",
        "(?:§f§f)?§7\\[Lvl (?:1➡(?:100|200)|\\{LVL})] (?<name>.*)",
    )

    private val ignoredPetStrings = listOf(
        "Archer",
        "Berserk",
        "Mage",
        "Tank",
        "Healer",
        "➡",
    )

    fun isPetMenu(inventoryTitle: String): Boolean = petMenuPattern.matches(inventoryTitle)

    // Contains color code + name and for older SkyHanni users maybe also the pet level
    var currentPet: String?
        get() = ProfileStorageData.profileSpecific?.currentPet?.takeIf { it.isNotEmpty() }
        set(value) {
            ProfileStorageData.profileSpecific?.currentPet = value
        }

    fun isCurrentPet(petName: String): Boolean = currentPet?.contains(petName) ?: false

    fun getCleanName(nameWithLevel: String): String? {
        petItemName.matchMatcher(nameWithLevel) {
            return group("name")
        }
        neuRepoPetItemName.matchMatcher(nameWithLevel) {
            return group("name")
        }

        return null
    }

    fun getPetLevel(nameWithLevel: String): Int? = petItemName.matchMatcher(nameWithLevel) {
        group("level").toInt()
    }

    fun hasPetName(name: String): Boolean = petItemName.matches(name) && !ignoredPetStrings.any { name.contains(it) }
}
