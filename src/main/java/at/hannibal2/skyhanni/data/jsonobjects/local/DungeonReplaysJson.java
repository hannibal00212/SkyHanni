package at.hannibal2.skyhanni.data.jsonobjects.local;

import at.hannibal2.skyhanni.features.dungeon.floor7.DungeonGhostData;
import com.google.gson.annotations.Expose;

public class DungeonReplaysJson {
    @Expose
    public DungeonGhostData manual = new DungeonGhostData();

    @Expose
    public DungeonGhostData floor3 = new DungeonGhostData();

    @Expose
    public DungeonGhostData floor7 = new DungeonGhostData();

    @Expose
    public DungeonGhostData floorMaster7 = new DungeonGhostData();
}
