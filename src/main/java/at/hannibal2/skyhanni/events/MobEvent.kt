package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.data.mob.Mob

open class MobEvent(val mob: Mob) : LorenzEvent() {
    open class Spawn(mob: Mob) : MobEvent(mob) {
        class SkyblockMob(mob: Mob) : Spawn(mob)
        class Summon(mob: Mob) : Spawn(mob)
        class Player(mob: Mob) : Spawn(mob)
        class DisplayNPC(mob: Mob) : Spawn(mob)
        class Special(mob: Mob) : Spawn(mob)
        class Projectile(mob: Mob) : Spawn(mob)
    }

    open class DeSpawn(mob: Mob) : MobEvent(mob) {
        class SkyblockMob(mob: Mob) : DeSpawn(mob)
        class Summon(mob: Mob) : DeSpawn(mob)
        class Player(mob: Mob) : DeSpawn(mob)
        class DisplayNPC(mob: Mob) : DeSpawn(mob)
        class Special(mob: Mob) : DeSpawn(mob)
        class Projectile(mob: Mob) : DeSpawn(mob)
    }

    // TODO replace with "isFirstTime" parameter in the Spawn event. Also create an actual "player sees the mob for the first time" event
    @Deprecated("Old. Will get replaced soon.")
    open class FirstSeen(mob: Mob) : MobEvent(mob) {
        class SkyblockMob(mob: Mob) : FirstSeen(mob)
        class Summon(mob: Mob) : FirstSeen(mob)
        class Player(mob: Mob) : FirstSeen(mob)
        class DisplayNPC(mob: Mob) : FirstSeen(mob)
        class Special(mob: Mob) : FirstSeen(mob)
        class Projectile(mob: Mob) : FirstSeen(mob)
    }
}
