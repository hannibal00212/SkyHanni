package at.hannibal2.skyhanni.events.skyblock

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.PetData

/**
 * This event fires when a pet change occurs and when joining SkyBlock for the first time in a session.
 * The XP value in the PetData might not be accurate.
 *
 * @property oldPet The previous pet before the change.
 * @property newPet The new pet after the change.
 */
class PetChangeEvent(val oldPet: PetData?, val newPet: PetData?) : SkyHanniEvent()
