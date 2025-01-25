package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.data.mob.Mob
import net.minecraft.util.DamageSource

open class MobEvent(val mob: Mob) : SkyHanniEvent() {
    open class Spawn(mob: Mob) : MobEvent(mob) {
        class SkyblockMob(mob: Mob) : Spawn(mob)
        class Summon(mob: Mob) : Spawn(mob)
        class Player(mob: Mob) : Spawn(mob)
        class DisplayNpc(mob: Mob) : Spawn(mob)
        class Special(mob: Mob) : Spawn(mob)
        class Projectile(mob: Mob) : Spawn(mob)
    }

    open class DeSpawn(mob: Mob) : MobEvent(mob) {
        class SkyblockMob(mob: Mob) : DeSpawn(mob)
        class Summon(mob: Mob) : DeSpawn(mob)
        class Player(mob: Mob) : DeSpawn(mob)
        class DisplayNpc(mob: Mob) : DeSpawn(mob)
        class Special(mob: Mob) : DeSpawn(mob)
        class Projectile(mob: Mob) : DeSpawn(mob)
    }

    open class FirstSeen(mob: Mob) : MobEvent(mob) {
        class SkyblockMob(mob: Mob) : FirstSeen(mob)
        class Summon(mob: Mob) : FirstSeen(mob)
        class Player(mob: Mob) : FirstSeen(mob)
        class DisplayNpc(mob: Mob) : FirstSeen(mob)
        class Special(mob: Mob) : FirstSeen(mob)
        class Projectile(mob: Mob) : FirstSeen(mob)
    }

    open class Hurt(mob: Mob, val source: DamageSource, val amount: Float) : MobEvent(mob) {
        class SkyblockMob(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        class Summon(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        class Player(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        class DisplayNpc(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        class Special(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
        class Projectile(mob: Mob, source: DamageSource, amount: Float) : Hurt(mob, source, amount)
    }
}
