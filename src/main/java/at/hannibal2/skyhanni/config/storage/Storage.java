package at.hannibal2.skyhanni.config.storage;

import at.hannibal2.skyhanni.features.misc.reminders.Reminder;
import at.hannibal2.skyhanni.features.misc.visualwords.VisualWord;
import at.hannibal2.skyhanni.utils.LorenzVec;
import at.hannibal2.skyhanni.utils.tracker.SkyHanniTracker;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Storage {

    @Expose
    public boolean hasPlayedBefore = false;

    @Expose
    public Float savedMouselockedSensitivity = .5f;

    @Expose
    public Float savedMouseloweredSensitivity = .5f;

    @Deprecated
    @Expose
    public Map<String, List<String>> knownFeatureToggles = new HashMap<>();

    @Deprecated
    @Expose
    public List<VisualWord> modifiedWords = new ArrayList<>();

    @Expose
    public boolean visualWordsImported = false;

    @Expose
    public Boolean contestSendingAsked = false;

    @Expose
    public Map<String, SkyHanniTracker.DisplayMode> trackerDisplayModes = new HashMap<>();

    @Expose
    public List<LorenzVec> foundDianaBurrowLocations = new ArrayList<>();

    @Expose
    public Map<UUID, PlayerSpecificStorage> players = new HashMap<>();

    // TODO this should get moved into player specific
    @Expose
    public String currentFameRank = "New player";

    @Expose
    public List<String> blacklistedUsers = new ArrayList<>();

    @Expose
    public Map<String, Reminder> reminders = new HashMap<>();
}
