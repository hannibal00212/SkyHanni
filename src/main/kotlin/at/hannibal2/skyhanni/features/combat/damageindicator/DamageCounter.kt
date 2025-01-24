package at.hannibal2.skyhanni.features.combat.damageindicator

import java.util.LinkedList

class DamageCounter {

    var currentDamage = 0L
    var currentHealing = 0L
    var oldDamages = LinkedList<OldDamage>()
    var firstTick = 0L
}
