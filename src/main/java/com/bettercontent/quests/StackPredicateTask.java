package com.bettercontent.quests;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class StackPredicateTask extends AbstractBooleanTask {
    private String predicate = "";

    public StackPredicateTask(long id, Quest quest) { super(id, quest); }
    @Override public TaskType getType() { return QuestTaskTypes.STACK_PREDICATE; }
    @Override public int autoSubmitOnPlayerTick() { return 20; }
    public String predicate() { return predicate; }

    @Override public boolean canSubmit(TeamData data, ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) if (NamedStackPredicates.test(predicate, stack)) return true;
        for (ItemStack stack : player.getInventory().armor) if (NamedStackPredicates.test(predicate, stack)) return true;
        for (ItemStack stack : player.getInventory().offhand) if (NamedStackPredicates.test(predicate, stack)) return true;
        return false;
    }

    @Override public void writeData(CompoundTag tag) { super.writeData(tag); tag.putString("predicate", predicate); }
    @Override public void readData(CompoundTag tag) { super.readData(tag); predicate = tag.getString("predicate"); }
    @Override public void writeNetData(FriendlyByteBuf buffer) { super.writeNetData(buffer); buffer.writeUtf(predicate); }
    @Override public void readNetData(FriendlyByteBuf buffer) { super.readNetData(buffer); predicate = buffer.readUtf(); }
}
