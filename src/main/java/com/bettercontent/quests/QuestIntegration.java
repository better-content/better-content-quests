package com.bettercontent.quests;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;

public final class QuestIntegration {
    private QuestIntegration() {}

    public static void initialize() {
        if (!ModList.get().isLoaded("ftbquests")) return;
        QuestTaskTypes.register();
        if (ModList.get().isLoaded("class_selector")) {
            ClassSelectorQuestIntegration.register();
        }
        if (ModList.get().isLoaded("dimension_drink")) {
            DimensionDrinkQuestIntegration.register();
        }
        MinecraftForge.EVENT_BUS.register(GameplayCriterionDetector.class);
    }

    public static void completeCriterion(ServerPlayer player, String name) {
        if (!GameplayCriterionNames.SUPPORTED.contains(name) || !ModList.get().isLoaded("ftbquests") || ServerQuestFile.INSTANCE == null) return;
        TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
        for (Task task : ServerQuestFile.INSTANCE.getAllTasks()) {
            if (task instanceof CriterionTask criterion && criterion.criterion().equals(name)) data.setProgress(task, 1L);
        }
    }
}
