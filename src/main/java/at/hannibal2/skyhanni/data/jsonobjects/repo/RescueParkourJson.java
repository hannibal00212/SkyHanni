package at.hannibal2.skyhanni.data.jsonobjects.repo;

import at.hannibal2.skyhanni.utils.LorenzVec;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Map;

public class RescueParkourJson {

    @Expose
    public Map<String, List<LorenzVec>> mage;

    @Expose
    public Map<String, List<LorenzVec>> barb;

}
