package com.bettercontent.quests;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/** Loaded only after Forge confirms the pinned Create API is present. */
final class CreateManualWorkcellCriteria {
    private CreateManualWorkcellCriteria() {
    }

    static boolean isConnected(final ServerPlayer player, final BlockPos crank) {
        final EnumSet<Part> found = EnumSet.noneOf(Part.class);
        final Set<Long> networks = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                crank.offset(-16, -16, -16), crank.offset(16, 16, 16))) {
            final BlockState state = player.level().getBlockState(pos);
            final String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            final Part part = Part.from(id);
            if (part == null) {
                continue;
            }
            found.add(part);
            if (part.kinetic
                    && player.level().getBlockEntity(pos) instanceof KineticBlockEntity kinetic
                    && kinetic.network != null) {
                networks.add(kinetic.network);
            }
        }
        return found.containsAll(EnumSet.allOf(Part.class)) && networks.size() == 1;
    }

    private enum Part {
        SHAFT(true), COG(true), BELT(true), PRESS(true), MIXER(true), DEPLOYER(true),
        DEPOT(false), BASIN(false), CASING(false);

        private final boolean kinetic;

        Part(final boolean kinetic) {
            this.kinetic = kinetic;
        }

        static Part from(final String id) {
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
