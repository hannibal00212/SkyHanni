package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.data.MayorAPI.currentMayor
import at.hannibal2.skyhanni.data.MayorAPI.foxyExtraEventPattern
import at.hannibal2.skyhanni.data.jsonobjects.other.MayorPerk
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher

enum class Mayor(
    val mayorName: String,
    val color: String,
    vararg val perks: Perk,
) {
    AATROX(
        "Aatrox",
        "§3",
        Perk.SLASHED_PRICING,
        Perk.SLAYER_XP_BUFF,
        Perk.PATHFINDER,
    ),
    COLE(
        "Cole",
        "§e",
        Perk.PROSPECTION,
        Perk.MINING_XP_BUFF,
        Perk.MINING_FIESTA,
        Perk.MOLTEN_FORGE,
    ),
    DIANA(
        "Diana",
        "§2",
        Perk.LUCKY,
        Perk.MYTHOLOGICAL_RITUAL,
        Perk.PET_XP_BUFF,
        Perk.SHARING_IS_CARING,
    ),
    DIAZ(
        "Diaz",
        "§6",
        Perk.VOLUME_TRADING,
        Perk.SHOPPING_SPREE,
        Perk.STOCK_EXCHANGE,
        Perk.LONG_TERM_INVESTMENT,
    ),
    FINNEGAN(
        "Finnegan",
        "§c",
        Perk.PELT_POCALYPSE,
        Perk.GOATED,
        Perk.BLOOMING_BUSINESS,
        Perk.PEST_ERADICATOR,
    ),
    FOXY(
        "Foxy",
        "§d",
        Perk.SWEET_BENEVOLENCE,
        Perk.A_TIME_FOR_GIVING,
        Perk.CHIVALROUS_CARNIVAL,
        Perk.EXTRA_EVENT_MINING,
        Perk.EXTRA_EVENT_FISHING,
        Perk.EXTRA_EVENT_SPOOKY,
    ),
    MARINA(
        "Marina",
        "§b",
        Perk.FISHING_XP_BUFF,
        Perk.LUCK_OF_THE_SEA,
        Perk.FISHING_FESTIVAL,
        Perk.DOUBLE_TROUBLE,
    ),
    PAUL(
        "Paul",
        "§c",
        Perk.MARAUDER,
        Perk.EZPZ,
        Perk.BENEDICTION,
    ),

    SCORPIUS(
        "Scorpius",
        "§d",
        Perk.BRIBE,
        Perk.DARKER_AUCTIONS,
    ),
    JERRY(
        "Jerry",
        "§d",
        Perk.PERKPOCALYPSE,
        Perk.STATSPOCALYPSE,
        Perk.JERRYPOCALYPSE,
    ),
    DERPY(
        "Derpy",
        "§d",
        Perk.TURBO_MINIONS,
        Perk.QUAD_TAXES,
        Perk.DOUBLE_MOBS_HP,
        Perk.MOAR_SKILLZ,
    ),

    UNKNOWN("Unknown", "§c"),
    DISABLED("§cDisabled", "§7"),
    ;

    val activePerks: MutableList<Perk> = mutableListOf()

    override fun toString() = mayorName

    fun addAllPerks(): Mayor {
        activePerks.addAll(perks)
        perks.forEach { it.isActive = true }
        return this
    }

    fun isActive() = this == currentMayor

    companion object {

        fun getMayorFromName(name: String): Mayor? = entries.firstOrNull { it.mayorName == name }

        fun getMayorFromPerk(perk: Perk): Mayor? = entries.firstOrNull { it.perks.contains(perk) }

        fun setAssumeMayorJson(name: String, perksJson: List<MayorPerk>): Mayor? {
            val mayor = getMayorFromName(name)
            if (mayor == null) {
                ErrorManager.logErrorStateWithData(
                    "Unknown mayor found",
                    "mayor name not in Mayor enum",
                    "name" to name,
                    "perksJson" to perksJson,
                    betaOnly = true,
                )
                return null
            }

            val perks = perksJson.mapNotNull { perkJson ->
                val perk = Perk.entries.firstOrNull { it.perkName == perkJson.renameIfFoxyExtraEventPerkFound() }
                perk?.also {
                    it.description = perkJson.description
                }
            }

            mayor.addPerks(perks)
            return mayor
        }

        fun Mayor.addPerks(perks: List<Perk>) {
            perks.forEach { it.isActive = false }
            activePerks.clear()
            for (perk in perks.filter { perks.contains(it) }) {
                perk.isActive = true
                activePerks.add(perk)
            }
        }

        private fun MayorPerk.renameIfFoxyExtraEventPerkFound(): String {
            val foxyExtraEventPairs = mapOf(
                "Spooky Festival" to "Extra Event (Spooky)",
                "Mining Fiesta" to "Extra Event (Mining)",
                "Fishing Festival" to "Extra Event (Fishing)",
            )

            return foxyExtraEventPattern.matchMatcher(this.description) {
                foxyExtraEventPairs.entries.firstOrNull { it.key == group("event") }?.value
            } ?: this.name
        }
    }
}

enum class Perk(val perkName: String) {
    // Aatrox
    SLASHED_PRICING("SLASHED Pricing"),
    SLAYER_XP_BUFF("Slayer XP Buff"),
    PATHFINDER("Pathfinder"),

    // Cole
    PROSPECTION("Prospection"),
    MINING_XP_BUFF("Mining XP Buff"),
    MINING_FIESTA("Mining Fiesta"),
    MOLTEN_FORGE("Molten Forge"),

    // Diana
    LUCKY("Lucky!"),
    MYTHOLOGICAL_RITUAL("Mythological Ritual"),
    PET_XP_BUFF("Pet XP Buff"),
    SHARING_IS_CARING("Sharing is Caring"),

    // Diaz
    SHOPPING_SPREE("Shopping Spree"),
    VOLUME_TRADING("Volume Trading"),
    STOCK_EXCHANGE("Stock Exchange"),
    LONG_TERM_INVESTMENT("Long Term Investment"),

    // Finnegan
    PELT_POCALYPSE("Pelt-pocalypse"),
    GOATED("GOATed"),
    BLOOMING_BUSINESS("Blooming Business"),
    PEST_ERADICATOR("Pest Eradicator"),

    // Foxy
    SWEET_BENEVOLENCE("Sweet Benevolence"),
    A_TIME_FOR_GIVING("A Time for Giving"),
    CHIVALROUS_CARNIVAL("Chivalrous Carnival"),
    EXTRA_EVENT_MINING("Extra Event (Mining)"),
    EXTRA_EVENT_FISHING("Extra Event (Fishing)"),
    EXTRA_EVENT_SPOOKY("Extra Event (Spooky)"),

    // Marina
    FISHING_XP_BUFF("Fishing XP Buff"),
    LUCK_OF_THE_SEA("Luck of the Sea 2.0"),
    FISHING_FESTIVAL("Fishing Festival"),
    DOUBLE_TROUBLE("Double Trouble"),

    // Paul
    MARAUDER("Marauder"),
    EZPZ("EZPZ"),
    BENEDICTION("Benediction"),

    // Scorpius
    BRIBE("Bribe"),
    DARKER_AUCTIONS("Darker Auctions"),

    // Jerry
    PERKPOCALYPSE("Perkpocalypse"),
    STATSPOCALYPSE("Statspocalypse"),
    JERRYPOCALYPSE("Jerrypocalypse"),

    // Derpy
    TURBO_MINIONS("TURBO MINIONS!!!"),
    QUAD_TAXES("QUAD TAXES!!!"),
    DOUBLE_MOBS_HP("DOUBLE MOBS HP!!!"),
    MOAR_SKILLZ("MOAR SKILLZ!!!"),
    ;

    var isActive = false
    var description = "§cDescription failed to load from the API."

    override fun toString(): String = "$perkName: $description"

    companion object {
        fun getPerkFromName(name: String): Perk? = entries.firstOrNull { it.perkName == name }
    }
}
