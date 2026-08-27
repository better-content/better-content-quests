package com.bettercontent.quests;

import com.bettercontent.quests.BetterContentQuests;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import net.minecraft.resources.ResourceLocation;

final class QuestTaskTypes {
    static final TaskType CRITERION = TaskTypes.register(
            new ResourceLocation(BetterContentQuests.MOD_ID, "criterion"), CriterionTask::new,
            () -> Icon.getIcon("minecraft:item/filled_map"));
    static final TaskType STACK_PREDICATE = TaskTypes.register(
            new ResourceLocation(BetterContentQuests.MOD_ID, "stack_predicate"), StackPredicateTask::new,
            () -> Icon.getIcon("minecraft:item/spyglass"));

    private QuestTaskTypes() {}

    static void register() {
        // Class initialization performs FTB's supported task-type registration.
    }
}
