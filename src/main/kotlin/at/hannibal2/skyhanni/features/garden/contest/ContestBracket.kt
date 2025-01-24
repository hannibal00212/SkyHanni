package at.hannibal2.skyhanni.features.garden.contest

import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

enum class ContestBracket(val color: String) {
    DIAMOND("b"),
    PLATINUM("3"),
    GOLD("6"),
    SILVER("f"),
    BRONZE("c"),
    ;

    val displayName = "§$color§l$name"
    val bracketPattern by RepoPattern.pattern(
        "garden.farming.contest.bracket" + this.name.lowercase(),
        "$displayName §7\\(§bTop \\d{1,2}%§7\\): §6(?<amount>.*)"
    )
}
