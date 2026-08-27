package com.bettercontent.quests;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class CriterionTask extends AbstractBooleanTask {
    private String criterion = "";

    public CriterionTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override public TaskType getType() { return QuestTaskTypes.CRITERION; }
    @Override public boolean canSubmit(TeamData data, ServerPlayer player) { return false; }
    @Override public boolean checkOnLogin() { return false; }
    public String criterion() { return criterion; }

    @Override public void writeData(CompoundTag tag) {
        super.writeData(tag);
        tag.putString("criterion", criterion);
    }

    @Override public void readData(CompoundTag tag) {
        super.readData(tag);
        criterion = tag.getString("criterion");
    }

    @Override public void writeNetData(FriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(criterion);
    }

    @Override public void readNetData(FriendlyByteBuf buffer) {
        super.readNetData(buffer);
        criterion = buffer.readUtf();
    }
}
