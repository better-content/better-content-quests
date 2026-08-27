package com.bettercontent.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class QuestInfrastructureTest {
    @Test
    void supportedForeignEventsHaveStableCriterionNames() {
        assertEquals("class_selector_start_finalized", GameplayCriterionNames.forEventSimpleName("PlayerStartFinalizedEvent"));
        assertEquals("font_enter", GameplayCriterionNames.forEventSimpleName("FontEnterEvent"));
        assertEquals("font_aggregate_return", GameplayCriterionNames.forEventSimpleName("FontAggregateReturnEvent"));
        assertEquals("ship_assembled", GameplayCriterionNames.forEventSimpleName("ShipAssembledEvent"));
        assertEquals("", GameplayCriterionNames.forEventSimpleName("UnrelatedEvent"));
        assertEquals(Set.of("class_selector_start_finalized", "tcon_station_tool_repaired", "ship_assembled", "font_enter",
                "font_aggregate_return", "water_curio_drink", "regolith_crop_harvest", "rich_soil_tilled",
                "starcatcher_edible_catch", "first_finished_ferment", "frame_camouflaged", "frame_reshaped",
                "frame_custom_surface", "functional_frame_used", "formed_tcon_smeltery", "manual_workcell_run",
                "shelter_completed", "fresh_food_stored", "provisions_packed", "animal_husbandry",
                "planted_harvest", "transplanted_food_harvest", "offseason_growing", "balanced_diet",
                "ventilation_network"),
                GameplayCriterionNames.SUPPORTED);
        Set<String> basePredicates = Set.of("food_temperature_changed", "water_purity_3",
                "armor_with_inserted_insulation", "tempered_waterskin", "any_tcon_sand_cast",
                "any_tcon_permanent_cast", "tcon_functional_metal_part", "tcon_tool_with_metal_functional_part");
        assertTrue(NamedStackPredicates.SUPPORTED.containsAll(basePredicates));
        assertTrue(NamedStackPredicates.SUPPORTED.contains("enchantment_ars_nouveau_reactive"));
        assertTrue(NamedStackPredicates.SUPPORTED.contains("enchantment_minecraft_silk_touch"));
        assertEquals(38, NamedStackPredicates.SUPPORTED.size());
    }

    @Test
    void authoredTaskFieldsRemainPresent() throws IOException {
        String criterion = Files.readString(Path.of("src/main/java/com/bettercontent/quests/CriterionTask.java"));
        String predicate = Files.readString(Path.of("src/main/java/com/bettercontent/quests/StackPredicateTask.java"));
        assertTrue(criterion.contains("putString(\"criterion\"") && criterion.contains("getString(\"criterion\"") );
        assertTrue(predicate.contains("putString(\"predicate\"") && predicate.contains("getString(\"predicate\"") );
    }
}
