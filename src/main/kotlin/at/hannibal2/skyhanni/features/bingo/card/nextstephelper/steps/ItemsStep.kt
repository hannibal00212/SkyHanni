package at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps

open class ItemsStep(displayName: String, val itemName: String, amountNeeded: Long, val variants: Map<String, Int>) :
    ProgressionStep(displayName, amountNeeded)

class PartialProgressItemsStep(displayName: String, itemName: String, amountNeeded: Long, variants: Map<String, Int>) :
    ItemsStep(displayName, itemName, amountNeeded, variants) {

    var amountHavingHidden: Int = 0
    override val amountNeeded: Long
        get() = super.amountNeeded - amountHavingHidden
}
