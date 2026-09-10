package com.bettercontent.quests;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

public final class GameplayCriterionDetector {
    private static final java.util.Map<UUID, Set<String>> EATEN_FOODS = new java.util.HashMap<>();

    private GameplayCriterionDetector() {}

    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() instanceof ServerPlayer player && event.getParentA() instanceof Animal) {
            QuestCriteria.trigger(player, "animal_husbandry");
        }
    }

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getItem().isEdible()) return;
        Set<String> foods = EATEN_FOODS.computeIfAbsent(player.getUUID(), ignored -> new HashSet<>());
        foods.add(BuiltInRegistries.ITEM.getKey(event.getItem().getItem()).toString());
        if (foods.size() >= 8) QuestCriteria.trigger(player, "balanced_diet");
    }

    @SubscribeEvent
    public static void onCrank(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String id = BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).toString();
        if (id.equals("create:hand_crank") && ModList.get().isLoaded("create")
                && CreateManualWorkcellCriteria.isConnected(player, event.getPos())) {
            QuestCriteria.trigger(player, "manual_workcell_run");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 40 != 0) return;
        if (ModList.get().isLoaded("tconstruct") && TinkersSmelteryCriteria.hasFormedSmeltery(player)) {
            QuestCriteria.trigger(player, "formed_tcon_smeltery");
        }
        if (hasShelter(player)) QuestCriteria.trigger(player, "shelter_completed");
        if (hasFreshStoredFood(player)) QuestCriteria.trigger(player, "fresh_food_stored");
        if (hasPackedProvisions(player)) QuestCriteria.trigger(player, "provisions_packed");
    }

    static boolean hasShelter(ServerPlayer player) {
        BlockPos feet = player.blockPosition();
        if (player.level().canSeeSky(feet) || player.level().getMaxLocalRawBrightness(feet) < 8
                || player.isOnFire() || player.isFreezing()) return false;
        for (Direction direction : Direction.values()) {
            boolean closed = false;
            for (int distance = 1; distance <= (direction.getAxis().isVertical() ? 5 : 6); distance++) {
                BlockPos pos = feet.relative(direction, distance);
                if (player.level().getBlockState(pos).isSolidRender(player.level(), pos)) { closed = true; break; }
            }
            if (!closed) return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-5, -2, -5), feet.offset(5, 3, 5))) {
            var block = player.level().getBlockState(pos).getBlock();
            if (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock) return true;
        }
        return false;
    }

    static boolean hasFreshStoredFood(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -4, -8), center.offset(8, 4, 8))) {
            if (!(player.level().getBlockEntity(pos) instanceof Container container)) continue;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack.isEdible() && freshTemperature(stack)) return true;
            }
        }
        return false;
    }

    static boolean hasPackedProvisions(ServerPlayer player) {
        int food = 0, water = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEdible()) food += stack.getCount();
            if (NamedStackPredicates.test("water_purity_3", stack)) water += stack.getCount();
        }
        return food >= 4 && water >= 2;
    }

    private static boolean freshTemperature(ItemStack stack) {
        if (stack.getTag() == null || !stack.getTag().contains("heat_sync_food", 10)) return true;
        var food = stack.getTag().getCompound("heat_sync_food");
        return food.getDouble("decay") < 1.0 / 7.0 && food.getDouble("temperature_k") > 273.15;
    }

}
