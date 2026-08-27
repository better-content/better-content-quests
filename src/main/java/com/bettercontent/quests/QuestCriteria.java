package com.bettercontent.quests;

import net.minecraft.server.level.ServerPlayer;

/** Public, dependency-free entry point for Better Content gameplay integrations. */
public final class QuestCriteria {
    private QuestCriteria() {}

    public static void trigger(ServerPlayer player, String criterion) {
        if (player != null && criterion != null && !criterion.isBlank()) {
            QuestIntegration.completeCriterion(player, criterion);
        }
    }
}
