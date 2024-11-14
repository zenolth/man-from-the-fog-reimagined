package com.zen.the_fog.common.entity.the_man;

import com.zen.the_fog.common.item.ModItems;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class TheManPredicates {
    public static final List<Class<? extends Block>> LOOK_BLACKLIST = Arrays.asList(
            LeavesBlock.class,
            PlantBlock.class,
            FenceBlock.class,
            FenceGateBlock.class,
            SlabBlock.class,
            FlowerPotBlock.class,
            AbstractPressurePlateBlock.class,
            FluidBlock.class,
            TorchBlock.class,
            DoorBlock.class,
            AbstractRailBlock.class,
            BarrierBlock.class,
            VineBlock.class,
            LadderBlock.class,
            NetherPortalBlock.class,
            TripwireBlock.class,
            AbstractFireBlock.class,
            TransparentBlock.class,
            StairsBlock.class
    );

    public static final List<Class<? extends Block>> BLOCK_PREDICATE_BLACKLIST = Arrays.asList(
            PlantBlock.class,
            FlowerPotBlock.class,
            AbstractPressurePlateBlock.class,
            FluidBlock.class,
            TorchBlock.class,
            DoorBlock.class,
            AbstractRailBlock.class,
            VineBlock.class,
            LadderBlock.class,
            NetherPortalBlock.class,
            TripwireBlock.class,
            AbstractFireBlock.class
    );

    public static final Predicate<BlockState> LOOK_BLOCK_STATE_PREDICATE = blockState -> !blockState.isAir() && blockState.isOpaque() && !LOOK_BLACKLIST.contains(blockState.getBlock().getClass());
    public static final Predicate<BlockState> BLOCK_STATE_PREDICATE = blockState -> !blockState.isAir() && blockState.isOpaque() && !BLOCK_PREDICATE_BLACKLIST.contains(blockState.getBlock().getClass());

    public static final Predicate<Entity> TARGET_PREDICATE = entity -> {
        if (!entity.isPlayer()) {
            return false;
        }
        PlayerEntity player = (PlayerEntity) entity;

        if (!TheManEntity.canAttack(player,player.getWorld())) {
            return false;
        }

        return !player.isCreative() && !player.isSpectator() && player.getHealth() > 1;
    };

    public static final Predicate<Entity> VALID_MAN = entity ->
            entity instanceof TheManEntity theMan && !theMan.isHallucination() && !theMan.isParanoia() && theMan.isAlive();

    public static final Predicate<Entity> VALID_MAN_HALLUCINATION = entity ->
            entity instanceof TheManEntity theMan && theMan.isHallucination() && !theMan.isParanoia() && theMan.isAlive();

    public static final Predicate<BlockState> EXCEPT_AIR = blockState -> !blockState.isAir();

    public static final BiPredicate<ServerWorld, BlockPos> CLIMBABLE_BLOCK_PREDICATE = (serverWorld,blockPos) -> {
        BlockState blockState = serverWorld.getBlockState(blockPos);
        Block block = blockState.getBlock();

        if (blockState.contains(Properties.OPEN)) {
            return false;
        }

        if (block instanceof LadderBlock) {
            return false;
        }

        if (block instanceof FenceBlock || block instanceof FenceGateBlock || block instanceof SlabBlock || block instanceof StairsBlock) {
            return true;
        }

        return !blockState.isAir() && blockState.isFullCube(serverWorld,blockPos);
    };
}
