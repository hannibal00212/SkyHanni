package at.hannibal2.skyhanni.config;

import at.hannibal2.skyhanni.data.SackItem;
import at.hannibal2.skyhanni.utils.NEUInternalName;
import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SackData {

    @Expose
    public Map<UUID, PlayerSpecific> players = new HashMap<>();

    public static class PlayerSpecific {
        @Expose
        public Map<String, ProfileSpecific> profiles = new HashMap<>();
    }

    public static class ProfileSpecific {

        @Expose
        public Map<NEUInternalName, SackItem> sackContents = new HashMap<>();
    }
}
