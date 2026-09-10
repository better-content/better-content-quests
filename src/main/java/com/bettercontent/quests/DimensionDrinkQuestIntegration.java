package com.bettercontent.quests;

import com.bettercontent.dimensiondrink.api.event.FontAggregateReturnEvent;
import net.minecraftforge.common.MinecraftForge;

/** Loaded only after Forge confirms the exact Dimension Drink API provider is present. */
final class DimensionDrinkQuestIntegration {
    private DimensionDrinkQuestIntegration() {
    }

    static void register() {
        MinecraftForge.EVENT_BUS.addListener(DimensionDrinkQuestIntegration::onFontAggregateReturn);
    }

    private static void onFontAggregateReturn(final FontAggregateReturnEvent event) {
        QuestIntegration.completeCriterion(event.getPlayer(), "font_aggregate_return");
    }
}
