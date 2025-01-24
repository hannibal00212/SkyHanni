package at.hannibal2.skyhanni.data.jsonobjects.local;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnownFeaturesJson {
    @Expose
    public Map<String, List<String>> knownFeatures = new HashMap<>();
}
