package at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps

abstract class NextStep(
    val displayName: String,
    var done: Boolean = false,
    val requirements: MutableList<NextStep> = mutableListOf(),
)
