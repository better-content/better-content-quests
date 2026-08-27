package com.bettercontent.quests;

import com.bettercontent.quests.BetterContentQuests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class QuestPredicateGameTests {
    private QuestPredicateGameTests() {
    }

    @GameTest(templateNamespace = BetterContentQuests.MOD_ID, template = "empty", timeoutTicks = 100)
    public static void enchantmentPredicatesInspectActualStackEnchantments(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 3);
        helper.assertTrue(NamedStackPredicates.test("enchantment_minecraft_sharpness", sword),
                "sharpness predicate should accept a sharpness-enchanted item");
        helper.assertTrue(!NamedStackPredicates.test("enchantment_minecraft_silk_touch", sword),
                "silk touch predicate must reject a sharpness-only item");
        helper.succeed();
    }
}
