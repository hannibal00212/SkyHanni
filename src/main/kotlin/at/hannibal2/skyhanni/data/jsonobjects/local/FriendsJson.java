package at.hannibal2.skyhanni.data.jsonobjects.local;

import com.google.gson.annotations.Expose;

import java.util.Map;
import java.util.UUID;

public class FriendsJson {
    @Expose
    public Map<UUID, PlayerFriends> players;

    public static class PlayerFriends {

        @Expose
        public Map<UUID, Friend> friends;

        public static class Friend {
            @Expose
            public String name;
            @Expose
            public boolean bestFriend;
        }
    }
}
