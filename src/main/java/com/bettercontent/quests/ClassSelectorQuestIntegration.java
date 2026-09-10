package com.bettercontent.quests;

import com.bettercontent.classselector.integration.PlayerStartFinalizedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

/** Loaded only after Forge confirms the exact Class Selector API provider is present. */
final class ClassSelectorQuestIntegration {
    private ClassSelectorQuestIntegration() {
    }

    static void register() {
        MinecraftForge.EVENT_BUS.addListener(ClassSelectorQuestIntegration::onPlayerStartFinalized);
    }

    private static void onPlayerStartFinalized(final PlayerStartFinalizedEvent event) {
        QuestIntegration.completeCriterion((ServerPlayer) event.getEntity(), "class_selector_start_finalized");
    }
}
