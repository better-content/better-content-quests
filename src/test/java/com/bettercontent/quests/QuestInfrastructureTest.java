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
        assertEquals("font_aggregate_return", GameplayCriterionNames.forEventSimpleName("FontAggregateReturnEvent"));
        assertEquals("ship_assembled", GameplayCriterionNames.forEventSimpleName("ShipAssembledEvent"));
        assertEquals("", GameplayCriterionNames.forEventSimpleName("UnrelatedEvent"));
        assertEquals(Set.of("class_selector_start_finalized", "ship_assembled", "font_aggregate_return",
                "rich_soil_tilled", "first_finished_ferment", "formed_tcon_smeltery", "manual_workcell_run",
                "shelter_completed", "fresh_food_stored", "provisions_packed", "animal_husbandry",
                "balanced_diet"),
                GameplayCriterionNames.SUPPORTED);
        assertTrue(NamedStackPredicates.SUPPORTED.contains("water_purity_3"));
        assertTrue(NamedStackPredicates.SUPPORTED.contains("enchantment_ars_nouveau_reactive"));
        assertTrue(NamedStackPredicates.SUPPORTED.contains("enchantment_minecraft_silk_touch"));
        assertEquals(31, NamedStackPredicates.SUPPORTED.size());
    }

    @Test
    void authoredTaskFieldsRemainPresent() throws IOException {
        String criterion = Files.readString(Path.of("src/main/java/com/bettercontent/quests/CriterionTask.java"));
        String predicate = Files.readString(Path.of("src/main/java/com/bettercontent/quests/StackPredicateTask.java"));
        assertTrue(criterion.contains("putString(\"criterion\"") && criterion.contains("getString(\"criterion\"") );
        assertTrue(predicate.contains("putString(\"predicate\"") && predicate.contains("getString(\"predicate\"") );
    }
}
