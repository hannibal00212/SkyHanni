package at.hannibal2.skyhanni.features.bingo.card.nextstephelper.steps

import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat

class CollectionStep(collectionName: String, amountNeeded: Int) :
    ProgressionStep(amountNeeded.shortFormat() + " $collectionName Collection", amountNeeded.toLong()) {

    val internalName by lazy {
        NEUInternalName.fromItemName(if (collectionName == "Mushroom") "Red Mushroom" else collectionName)
    }
}
