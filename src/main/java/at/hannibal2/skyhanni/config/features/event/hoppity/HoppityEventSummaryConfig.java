package at.hannibal2.skyhanni.config.features.event.hoppity;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HoppityEventSummaryConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a summary of your Hoppity's Hunt stats when the event is over.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean enabled = true;

    @Expose
    @Accordion
    @ConfigOption(name = "Live Display", desc = "")
    public HoppityEventSummaryLiveDisplayConfig liveDisplay = new HoppityEventSummaryLiveDisplayConfig();

    @Expose
    @ConfigLink(owner = HoppityEventSummaryConfig.class, field = "liveDisplay")
    public Position liveDisplayPosition = new Position(0, 0);

    @Expose
    @ConfigOption(
        name = "Stats List",
        desc = "Drag text to change what displays in the summary card."
    )
    @ConfigEditorDraggableList
    public List<HoppityStat> statDisplayList = new ArrayList<>(Arrays.asList(
        HoppityStat.MEAL_EGGS_FOUND,
        HoppityStat.HOPPITY_RABBITS_BOUGHT,
        HoppityStat.SIDE_DISH_EGGS,
        HoppityStat.MILESTONE_RABBITS,
        HoppityStat.EMPTY_1,
        HoppityStat.NEW_RABBITS,
        HoppityStat.EMPTY_2,
        HoppityStat.DUPLICATE_RABBITS,
        HoppityStat.EMPTY_3,
        HoppityStat.TIME_IN_CF,
        HoppityStat.RABBIT_THE_FISH_FINDS,
        HoppityStat.LEADERBOARD_CHANGE
    ));

    public enum HoppityStat {
        MEAL_EGGS_FOUND("§7You found §b45§7/§a47 §6Chocolate Meal Eggs§7."),
        HOPPITY_RABBITS_BOUGHT("§7You bought §b7 §fRabbits §7from §aHoppity§7."),
        SIDE_DISH_EGGS("§7You found §b4 §6§lSide Dish Eggs §r§7in the §6Chocolate Factory§7."),
        MILESTONE_RABBITS("§7You claimed §b2 §6§lMilestone Rabbits§7."),
        EMPTY_1(""),
        NEW_RABBITS("§7Unique Rabbits: §b7\n §f1 §7- §a1 §7- §91 §7- §51 §7- §61 §7- §d1 §7- §b1"),
        EMPTY_2(""),
        DUPLICATE_RABBITS("§7Duplicate Rabbits: §c10\n §f4 §7- §a3 §7- §92 §7- §51 §7- §60 §7- §d0 §7- §b0\n §6+250,000,000 Chocolate"),
        EMPTY_3(""),
        STRAY_RABBITS("§7Stray Rabbits: §f20\n §f10 §7- §a6 §7- §93 §7- §51 §7- §60 §7- §d0 §7- §b0\n §6+8,000,000 Chocolate\n  §4* §c§oRequires Stray Tracker being enabled to work§4§o."),
        EMPTY_4(""),
        TIME_IN_CF("§7You spent §b4h 36m §7in the §6Chocolate Factory§7."),
        RABBIT_THE_FISH_FINDS("§7You found §cRabbit the Fish §7in Meal Eggs §b2 §7times."),
        LEADERBOARD_CHANGE("§6Leaderboard Change§7: §b#107 §c-> §b#99\n §7(§a+8 Spots§7) Top §a0.003% §c-> §7Top §a0.002%\n§4* §c§oSyncs on first §4§o/cf §c§orun per server swap§4§o.")
        ;

        private final String display;

        HoppityStat(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
