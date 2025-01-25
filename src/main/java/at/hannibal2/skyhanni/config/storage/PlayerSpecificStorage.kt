package at.hannibal2.skyhanni.config.storage

import at.hannibal2.skyhanni.features.bingo.card.goals.BingoGoal
import at.hannibal2.skyhanni.features.fame.UpgradeReminder.CommunityShopUpgrade
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.farPast
import com.google.gson.annotations.Expose

class PlayerSpecificStorage {
    @Expose
    var profiles: MutableMap<String, ProfileSpecificStorage> = mutableMapOf() // profile name

    @Expose
    var useRomanNumerals: Boolean = true

    @Expose
    var multipleProfiles: Boolean = false

    @Expose
    var gardenCommunityUpgrade: Int = -1

    @Expose
    var nextCityProjectParticipationTime: SimpleTimeMark = farPast()

    @Expose
    var communityShopAccountUpgrade: CommunityShopUpgrade? = null

    @Expose
    var guildMembers: MutableList<String> = mutableListOf()

    @Expose
    var winter: WinterStorage = WinterStorage()

    class WinterStorage {
        @Expose
        var playersThatHaveBeenGifted: MutableSet<String> = mutableSetOf()

        @Expose
        var amountGifted: Int = 0

        @Expose
        var cakeCollectedYear: Int = 0
    }

    @Expose
    var bingoSessions: MutableMap<Long, BingoSession> = mutableMapOf()

    class BingoSession {
        @Expose
        var tierOneMinionsDone: MutableSet<NeuInternalName> = mutableSetOf()

        @Expose
        var goals: MutableMap<Int, BingoGoal> = mutableMapOf()
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
