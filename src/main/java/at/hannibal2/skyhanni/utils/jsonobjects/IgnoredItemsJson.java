package at.hannibal2.skyhanni.utils.jsonobjects;

import com.google.gson.annotations.Expose;

import java.util.List;

public class IgnoredItemsJson {
    @Expose
    public List<String> exact;

    @Expose
    public List<String> contains;
}
