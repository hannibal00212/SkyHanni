package at.hannibal2.skyhanni.config.storage;

import at.hannibal2.skyhanni.features.bingo.card.goals.BingoGoal;
import at.hannibal2.skyhanni.features.fame.UpgradeReminder;
import at.hannibal2.skyhanni.utils.GenericWrapper;
import at.hannibal2.skyhanni.utils.NEUInternalName;
import at.hannibal2.skyhanni.utils.SimpleTimeMark;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerSpecificStorage {

    @Expose
    public Map<String, ProfileSpecificStorage> profiles = new HashMap<>(); // profile name

    @Expose
    public Boolean useRomanNumerals = true;

    @Expose
    public Boolean multipleProfiles = false;

    @Expose
    public Integer gardenCommunityUpgrade = -1;

    @Expose
    public SimpleTimeMark nextCityProjectParticipationTime = GenericWrapper.getSimpleTimeMark(SimpleTimeMark.farPast()).getIt();

    @Expose
    public UpgradeReminder.CommunityShopUpgrade communityShopAccountUpgrade = null;

    @Expose
    public List<String> guildMembers = new ArrayList<>();

    @Expose
    public WinterStorage winter = new WinterStorage();

    public static class WinterStorage {

        @Expose
        public Set<String> playersThatHaveBeenGifted = new HashSet<>();

        @Expose
        public int amountGifted = 0;

        @Expose
        public int cakeCollectedYear = 0;
    }

    @Expose
    public Map<Long, BingoSession> bingoSessions = new HashMap<>();

    public static class BingoSession {

        @Expose
        public Set<NEUInternalName> tierOneMinionsDone = new HashSet<>();

        @Expose
        public Map<Integer, BingoGoal> goals = new HashMap<>();
    }

    @Expose
    public LimboStats limbo = new LimboStats();

    public static class LimboStats {

        @Expose
        public int playtime = 0;

        @Expose
        public int personalBest = 0;

        @Expose
        public float userLuck = 0f;
    }
}
