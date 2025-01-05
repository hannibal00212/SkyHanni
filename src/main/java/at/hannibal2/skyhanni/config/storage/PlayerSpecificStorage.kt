package at.hannibal2.skyhanni.config.storage

import at.hannibal2.skyhanni.features.bingo.card.goals.BingoGoal
import at.hannibal2.skyhanni.features.fame.UpgradeReminder.CommunityShopUpgrade
import at.hannibal2.skyhanni.utils.GenericWrapper.Companion.getSimpleTimeMark
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.farPast
import com.google.gson.annotations.Expose

class PlayerSpecificStorage {
    @Expose
    var profiles: MutableMap<String, ProfileSpecificStorage> = HashMap() // profile name

    @Expose
    var useRomanNumerals: Boolean = true

    @Expose
    var multipleProfiles: Boolean = false

    @Expose
    var gardenCommunityUpgrade: Int = -1

    @Expose
    var nextCityProjectParticipationTime: SimpleTimeMark = getSimpleTimeMark(farPast()).it

    @Expose
    var communityShopAccountUpgrade: CommunityShopUpgrade? = null

    @Expose
    var guildMembers: MutableList<String> = ArrayList()

    @Expose
    var winter: WinterStorage = WinterStorage()

    class WinterStorage {
        @Expose
        var playersThatHaveBeenGifted: MutableSet<String> = HashSet()

        @Expose
        var amountGifted: Int = 0

        @Expose
        var cakeCollectedYear: Int = 0
    }

    @Expose
    var bingoSessions: MutableMap<Long, BingoSession> = HashMap()

    class BingoSession {
        @Expose
        var tierOneMinionsDone: MutableSet<NEUInternalName> = HashSet()

        @Expose
        var goals: MutableMap<Int, BingoGoal> = HashMap()
    }

    @Expose
    var limbo: LimboStats = LimboStats()

    class LimboStats {
        @Expose
        var playtime: Int = 0

        @Expose
        var personalBest: Int = 0

        @Expose
        var userLuck: Float = 0f
    }
}
