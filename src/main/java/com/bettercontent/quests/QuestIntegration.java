package com.bettercontent.quests;

import com.bettercontent.quests.BetterContentQuests;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.lang.reflect.Method;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public final class QuestIntegration {
    private QuestIntegration() {}

    public static void initialize() {
        if (!ModList.get().isLoaded("ftbquests")) return;
        QuestTaskTypes.register();
        MinecraftForge.EVENT_BUS.register(QuestIntegration.class);
        MinecraftForge.EVENT_BUS.register(GameplayCriterionDetector.class);
    }

    public static void completeCriterion(ServerPlayer player, String name) {
        if (!GameplayCriterionNames.SUPPORTED.contains(name) || !ModList.get().isLoaded("ftbquests") || ServerQuestFile.INSTANCE == null) return;
        TeamData data = ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
        for (Task task : ServerQuestFile.INSTANCE.getAllTasks()) {
            if (task instanceof CriterionTask criterion && criterion.criterion().equals(name)) data.setProgress(task, 1L);
        }
    }

    @SubscribeEvent
    public static void onForeignEvent(Event event) {
        String criterion = GameplayCriterionNames.forEventSimpleName(event.getClass().getSimpleName());
        if (criterion.isEmpty()) return;
        ServerPlayer player = reflectedPlayer(event);
        if (player != null) completeCriterion(player, criterion);
    }

    private static ServerPlayer reflectedPlayer(Object event) {
        for (String name : new String[]{"getPlayer", "player"}) {
            try {
                Method method = event.getClass().getMethod(name);
                Object value = method.invoke(event);
                if (value instanceof ServerPlayer player) return player;
            } catch (ReflectiveOperationException ignored) {}
        }
        return null;
    }

}
