package at.hannibal2.skyhanni.data.jsonobjects.local;

import at.hannibal2.skyhanni.features.misc.visualwords.VisualWord;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class VisualWordsJson {
    @Expose
    public List<VisualWord> modifiedWords = new ArrayList<>();
}
