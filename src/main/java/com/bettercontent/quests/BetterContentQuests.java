package com.bettercontent.quests;

import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BetterContentQuests.MOD_ID)
public final class BetterContentQuests {
    public static final String MOD_ID = "better_content_quests";

    public BetterContentQuests() {
        QuestIntegration.initialize();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerGameTests);
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(QuestPredicateGameTests.class);
    }
}
