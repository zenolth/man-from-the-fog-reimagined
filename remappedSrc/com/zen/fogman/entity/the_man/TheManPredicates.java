package com.zen.fogman.common.entity.the_man;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

public class TheManPredicates {
    public static final Predicate<BlockState> BLOCK_STATE_PREDICATE = blockState -> {

        if (blockState.isAir() || blockState.getBlock() instanceof LeavesBlock) {
            return false;
        }

        return blockState.isOpaque();
    };

    public static final Predicate<Entity> TARGET_PREDICATE = entity -> {
        if (!entity.isPlayer()) {
            return false;
        }
        PlayerEntity player = (PlayerEntity) entity;
        return player.isAlive() && !player.isCreative() && !player.isSpectator();
    };
}
