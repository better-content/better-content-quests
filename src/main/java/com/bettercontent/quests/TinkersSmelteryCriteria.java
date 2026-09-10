package com.bettercontent.quests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import slimeknights.tconstruct.smeltery.block.entity.controller.SmelteryBlockEntity;

/** Loaded only after Forge confirms the pinned TConstruct API is present. */
final class TinkersSmelteryCriteria {
    private TinkersSmelteryCriteria() {
    }

    static boolean hasFormedSmeltery(final ServerPlayer player) {
        final BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -5, -8), center.offset(8, 5, 8))) {
            final String id = BuiltInRegistries.BLOCK.getKey(
                    player.level().getBlockState(pos).getBlock()).toString();
            if (!id.equals("tconstruct:smeltery_controller")) {
                continue;
            }
            if (player.level().getBlockEntity(pos) instanceof SmelteryBlockEntity smeltery
                    && smeltery.getStructureResult().isSuccess()) {
                return true;
            }
        }
        return false;
    }
}
