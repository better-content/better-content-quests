package com.bettercontent.quests;

import java.util.Locale;
import java.util.Set;

final class GameplayCriterionNames {
    static final Set<String> SUPPORTED = Set.of(
            "class_selector_start_finalized", "tcon_station_tool_repaired", "ship_assembled", "font_enter",
            "font_aggregate_return", "water_curio_drink", "regolith_crop_harvest", "rich_soil_tilled",
            "starcatcher_edible_catch", "first_finished_ferment", "frame_camouflaged", "frame_reshaped",
            "frame_custom_surface", "functional_frame_used", "formed_tcon_smeltery", "manual_workcell_run",
            "shelter_completed", "fresh_food_stored", "provisions_packed", "animal_husbandry",
            "planted_harvest", "transplanted_food_harvest", "offseason_growing", "balanced_diet",
            "ventilation_network");
    private GameplayCriterionNames() {}

    static String forEventSimpleName(String eventName) {
        return switch (eventName.toLowerCase(Locale.ROOT)) {
            case "playerstartfinalizedevent" -> "class_selector_start_finalized";
            case "fontenterevent" -> "font_enter";
            case "fontaggregatereturnevent", "fontsuccessfulreturnevent" -> "font_aggregate_return";
            case "shipassembledevent", "vesselassembledevent" -> "ship_assembled";
            case "finishedfermentevent", "kegrecipecompletedevent" -> "first_finished_ferment";
            case "framecamouflageevent" -> "frame_camouflaged";
            case "framereshapedevent" -> "frame_reshaped";
            case "framecustomsurfaceevent" -> "frame_custom_surface";
            case "functionalframeusedevent" -> "functional_frame_used";
            case "tconstructtoolrepairedevent", "stationtoolrepairedevent" -> "tcon_station_tool_repaired";
            case "richsoiltilledevent" -> "rich_soil_tilled";
            case "starcatcherediblecatchevent", "starcatchercatchevent" -> "starcatcher_edible_catch";
            default -> "";
        };
    }
}
