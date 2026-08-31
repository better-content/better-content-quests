package com.bettercontent.quests;

import java.util.Locale;
import java.util.Set;

final class GameplayCriterionNames {
    static final Set<String> SUPPORTED = Set.of(
            "class_selector_start_finalized", "ship_assembled", "font_aggregate_return",
            "rich_soil_tilled", "first_finished_ferment", "formed_tcon_smeltery",
            "manual_workcell_run", "shelter_completed", "fresh_food_stored",
            "provisions_packed", "animal_husbandry", "balanced_diet");
    private GameplayCriterionNames() {}

    static String forEventSimpleName(String eventName) {
        return switch (eventName.toLowerCase(Locale.ROOT)) {
            case "playerstartfinalizedevent" -> "class_selector_start_finalized";
            case "fontaggregatereturnevent", "fontsuccessfulreturnevent" -> "font_aggregate_return";
            case "shipassembledevent", "vesselassembledevent" -> "ship_assembled";
            case "finishedfermentevent", "kegrecipecompletedevent" -> "first_finished_ferment";
            case "richsoiltilledevent" -> "rich_soil_tilled";
            default -> "";
        };
    }
}
