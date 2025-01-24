package at.hannibal2.skyhanni.data.jsonobjects.local;

import at.hannibal2.skyhanni.features.garden.CropType;
import at.hannibal2.skyhanni.utils.SimpleTimeMark;
import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacobContestsJson {
    @Expose
    public Map<SimpleTimeMark, List<CropType>> contestTimes = new HashMap<>();
}
