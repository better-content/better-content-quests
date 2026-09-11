package com.bettercontent.quests;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Real FTB objects in a synchronous, in-memory fixture; never loads or saves pack quest files. */
@PrefixGameTestTemplate(false)
public final class QuestCriterionGameTests {
    private QuestCriterionGameTests() {}

    @GameTest(templateNamespace = BetterContentQuests.MOD_ID, template = "empty", timeoutTicks = 100)
    public static void criterionCompletesOnlyMatchingTaskAndTeam(GameTestHelper helper) {
        ServerQuestFile originalFile = ServerQuestFile.INSTANCE;
        TeamManagerImpl originalTeams = TeamManagerImpl.INSTANCE;
        try {
            ServerQuestFile file = new ServerQuestFile(helper.getLevel().getServer());
            ServerQuestFile.INSTANCE = file;
            TeamManagerImpl teams = new TeamManagerImpl(helper.getLevel().getServer());
            TeamManagerImpl.INSTANCE = teams;
            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "criterion_actor"));
            UUID otherPlayer = UUID.randomUUID();
            // FTB's supported offline login path creates real personal teams without a client connection.
            teams.playerLoggedIn(null, player.getUUID(), player.getGameProfile().getName());
            teams.playerLoggedIn(null, otherPlayer, "criterion_other");
            Quest quest = quest(file);
            CriterionTask matching = task(file.newID(), quest, "manual_workcell_run");
            CriterionTask unrelated = task(file.newID(), quest, "shelter_completed");
            CriterionTask unsupported = task(file.newID(), quest, "test_unknown_criterion");
            quest.addTask(matching);
            quest.addTask(unrelated);
            quest.addTask(unsupported);
            file.refreshIDMap();
            TeamData actorData = file.getOrCreateTeamData(player);
            TeamData otherData = file.getOrCreateTeamData(teams.getTeamForPlayerID(otherPlayer).orElseThrow());
            helper.assertTrue(actorData != null && !actorData.getTeamId().equals(otherData.getTeamId()),
                    "fixture must resolve two distinct real FTB teams");

            QuestCriteria.trigger(player, "test_unknown_criterion");
            helper.assertTrue(actorData.getProgress(unsupported) == 0, "unsupported criteria must be rejected");
            QuestCriteria.trigger(player, "manual_workcell_run");
            helper.assertTrue(actorData.getProgress(matching) == 1 && actorData.isCompleted(matching),
                    "supported gameplay criterion must complete the actual FTB task");
            helper.assertTrue(actorData.getProgress(unrelated) == 0 && !actorData.isCompleted(unrelated),
                    "a different criterion must remain incomplete");
            helper.assertTrue(otherData.getProgress(matching) == 0 && !otherData.isCompleted(matching),
                    "criterion progress must stay on the triggering player's team");

            CompoundTag completed = actorData.serializeNBT().copy();
            QuestCriteria.trigger(player, "manual_workcell_run");
            helper.assertTrue(completed.equals(actorData.serializeNBT()),
                    "duplicate delivery must not change progress, completion timestamps, or reward state");
            helper.succeed();
        } finally {
            ServerQuestFile.INSTANCE = originalFile;
            TeamManagerImpl.INSTANCE = originalTeams;
        }
    }

    @GameTest(templateNamespace = BetterContentQuests.MOD_ID, template = "empty", timeoutTicks = 100)
    public static void criterionConfigurationRoundTripsThroughNbtAndNetwork(GameTestHelper helper) {
        Quest quest = quest(new ServerQuestFile(helper.getLevel().getServer()));
        CriterionTask original = task(101, quest, "manual_workcell_run");
        CompoundTag stored = new CompoundTag();
        original.writeData(stored);
        CriterionTask restored = task(102, quest, "shelter_completed");
        restored.readData(stored);
        helper.assertTrue(original.criterion().equals(restored.criterion()),
                "NBT load must overwrite an existing different criterion with the saved value");
        CompoundTag writtenAgain = new CompoundTag();
        restored.writeData(writtenAgain);
        helper.assertTrue(stored.equals(writtenAgain), "task configuration NBT must round-trip without loss");

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            original.writeNetData(buffer);
            CriterionTask received = task(103, quest, "shelter_completed");
            received.readNetData(buffer);
            helper.assertTrue(original.criterion().equals(received.criterion()),
                    "network decoding must preserve the configured criterion");
            helper.assertTrue(buffer.readableBytes() == 0, "task network reader must consume the entire payload");
            CompoundTag receivedTag = new CompoundTag();
            received.writeData(receivedTag);
            helper.assertTrue(stored.equals(receivedTag), "network round-trip must preserve all task configuration");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    private static Quest quest(ServerQuestFile file) {
        Chapter chapter = new Chapter(file.newID(), file, file.getDefaultChapterGroup());
        chapter.onCreated();
        Quest quest = new Quest(file.newID(), chapter);
        quest.onCreated();
        return quest;
    }

    private static CriterionTask task(long id, Quest quest, String criterion) {
        CriterionTask task = new CriterionTask(id, quest);
        CompoundTag config = new CompoundTag();
        config.putString("criterion", criterion);
        task.readData(config);
        return task;
    }
}
