package com.zen.fogman.entity.the_man;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.function.Predicate;

public class TheManPredicates {
    public static final Predicate<BlockState> BLOCK_STATE_PREDICATE = blockState -> {

        if (blockState.isAir() || blockState.getBlock() instanceof LeavesBlock || blockState.getBlock() instanceof PlantBlock) {
            return false;
        }

        return blockState.isOpaque();
    };

    public static final Predicate<Entity> TARGET_PREDICATE = entity -> {
        if (!entity.isPlayer()) {
            return false;
        }
        PlayerEntity player = (PlayerEntity) entity;
        return !player.isCreative() && !player.isSpectator();
    };

    public static final Predicate<Entity> VALID_MAN = entity ->
            entity instanceof TheManEntity theMan && !theMan.isHallucination() && theMan.isAlive();

    public static final Predicate<Entity> VALID_MAN_HALLUCINATION = entity ->
            entity instanceof TheManEntity theMan && theMan.isHallucination() && theMan.isAlive();

    public static final Predicate<BlockState> EXCEPT_AIR = blockState -> !blockState.isAir();
}
