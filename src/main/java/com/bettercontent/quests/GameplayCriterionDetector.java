package com.bettercontent.quests;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class GameplayCriterionDetector {
    private static final Map<UUID, Set<Long>> PLANTED_CROPS = new HashMap<>();
    private static final Map<UUID, Set<Long>> TRANSPLANTED_FOOD = new HashMap<>();
    private static final Map<UUID, Set<String>> EATEN_FOODS = new HashMap<>();

    private GameplayCriterionDetector() {}

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String id = blockId(event.getPlacedBlock());
        if (event.getPlacedBlock().getBlock() instanceof CropBlock) remember(PLANTED_CROPS, player, event.getPos());
        if (id.contains("sapling") || id.contains("berry_bush") || id.contains("fruit") || id.contains("orchard")) {
            remember(TRANSPLANTED_FOOD, player, event.getPos());
        }
    }

    @SubscribeEvent
    public static void onCropBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (isRemembered(TRANSPLANTED_FOOD, player, event.getPos())) QuestCriteria.trigger(player, "transplanted_food_harvest");
        if (!(event.getState().getBlock() instanceof CropBlock crop) || !crop.isMaxAge(event.getState())) return;
        if (isRemembered(PLANTED_CROPS, player, event.getPos())) QuestCriteria.trigger(player, "planted_harvest");
        if (event.getPos().getY() < 48 && !player.level().canSeeSky(event.getPos())) QuestCriteria.trigger(player, "offseason_growing");
        String below = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(event.getPos().below()).getBlock()).toString();
        if (below.contains("regolith") && below.contains("farmland")) QuestCriteria.trigger(player, "regolith_crop_harvest");
    }

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
        if (id.equals("create:hand_crank") && isConnectedManualWorkcell(player, event.getPos())) {
            QuestCriteria.trigger(player, "manual_workcell_run");
        }
        if (isRemembered(TRANSPLANTED_FOOD, player, event.getPos())) QuestCriteria.trigger(player, "transplanted_food_harvest");
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 40 != 0) return;
        if (hasFormedSmeltery(player)) QuestCriteria.trigger(player, "formed_tcon_smeltery");
        if (hasShelter(player)) QuestCriteria.trigger(player, "shelter_completed");
        if (hasFreshStoredFood(player)) QuestCriteria.trigger(player, "fresh_food_stored");
        if (hasPackedProvisions(player)) QuestCriteria.trigger(player, "provisions_packed");
        if (hasVentilationNetwork(player)) QuestCriteria.trigger(player, "ventilation_network");
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

    static boolean hasVentilationNetwork(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        Set<BlockPos> pieces = new HashSet<>();
        boolean chimney = false, mover = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-12, -8, -12), center.offset(12, 8, 12))) {
            String id = blockId(player.level().getBlockState(pos));
            if (!(id.contains("chimney") || id.contains("vent") || id.contains("duct") || id.contains("pipe") || id.contains("pump"))) continue;
            BlockPos immutable = pos.immutable(); pieces.add(immutable);
            chimney |= id.contains("chimney"); mover |= id.contains("vent") || id.contains("pump");
        }
        if (!chimney || !mover || pieces.size() < 3) return false;
        Set<BlockPos> visited = new HashSet<>(); ArrayDeque<BlockPos> open = new ArrayDeque<>();
        open.add(pieces.iterator().next());
        while (!open.isEmpty()) { BlockPos pos = open.remove(); if (!visited.add(pos)) continue;
            for (Direction direction : Direction.values()) { BlockPos next = pos.relative(direction); if (pieces.contains(next)) open.add(next); }
        }
        return visited.size() == pieces.size();
    }

    private static boolean freshTemperature(ItemStack stack) {
        if (stack.getTag() == null || !stack.getTag().contains("heat_sync_food", 10)) return true;
        var food = stack.getTag().getCompound("heat_sync_food");
        return food.getDouble("decay") < 1.0 / 7.0 && food.getDouble("temperature_k") > 273.15;
    }

    private static void remember(Map<UUID, Set<Long>> map, ServerPlayer player, BlockPos pos) {
        Set<Long> values = map.computeIfAbsent(player.getUUID(), ignored -> new HashSet<>()); values.add(pos.asLong());
        if (values.size() > 512) values.remove(values.iterator().next());
    }

    private static boolean isRemembered(Map<UUID, Set<Long>> map, ServerPlayer player, BlockPos pos) {
        return map.getOrDefault(player.getUUID(), Set.of()).contains(pos.asLong());
    }

    private static String blockId(BlockState state) { return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(); }

    static boolean hasFormedSmeltery(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -5, -8), center.offset(8, 5, 8))) {
            String id = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(pos).getBlock()).toString();
            if (!id.equals("tconstruct:smeltery_controller")) continue;
            BlockEntity entity = player.level().getBlockEntity(pos);
            if (reflectBoolean(entity, "isFormed", "isStructureValid", "isActive")) return true;
        }
        return false;
    }

    static boolean isConnectedManualWorkcell(ServerPlayer player, BlockPos crank) {
        EnumSet<Part> found = EnumSet.noneOf(Part.class);
        Set<Object> networks = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(crank.offset(-16, -16, -16), crank.offset(16, 16, 16))) {
            BlockState state = player.level().getBlockState(pos);
            String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            Part part = Part.from(id);
            if (part == null) continue;
            found.add(part);
            if (part.kinetic) {
                Object network = reflectNetwork(player.level().getBlockEntity(pos));
                if (network != null) networks.add(network);
            }
        }
        return found.containsAll(EnumSet.allOf(Part.class)) && networks.size() == 1;
    }

    private static boolean reflectBoolean(Object target, String... methods) {
        if (target == null) return false;
        for (String methodName : methods) {
            try {
                Method method = target.getClass().getMethod(methodName);
                if (method.invoke(target) instanceof Boolean result && result) return true;
            } catch (ReflectiveOperationException ignored) {}
        }
        return false;
    }

    private static Object reflectNetwork(Object target) {
        if (target == null) return null;
        for (String methodName : new String[]{"getNetwork", "getNetworkId"}) {
            try {
                Method method = target.getClass().getMethod(methodName);
                Object value = method.invoke(target);
                if (value != null) return value;
            } catch (ReflectiveOperationException ignored) {}
        }
        Class<?> type = target.getClass();
        while (type != null) {
            for (String fieldName : new String[]{"network", "networkId"}) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(target);
                    if (value != null) return value;
                } catch (ReflectiveOperationException ignored) {}
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private enum Part {
        SHAFT(true), COG(true), BELT(true), PRESS(true), MIXER(true), DEPLOYER(true), DEPOT(false), BASIN(false), CASING(false);
        final boolean kinetic;
        Part(boolean kinetic) { this.kinetic = kinetic; }

        static Part from(String id) {
            if (id.equals("create:shaft")) return SHAFT;
            if (id.contains("cogwheel")) return COG;
            if (id.equals("create:belt")) return BELT;
            if (id.equals("create:mechanical_press")) return PRESS;
            if (id.equals("create:mechanical_mixer")) return MIXER;
            if (id.equals("create:deployer")) return DEPLOYER;
            if (id.equals("create:depot")) return DEPOT;
            if (id.equals("create:basin")) return BASIN;
            if (id.equals("create:andesite_casing")) return CASING;
            return null;
        }
    }
}
