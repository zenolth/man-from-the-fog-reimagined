package dev.zenolth.the_fog.common.predicate;

import dev.zenolth.the_fog.common.block.ModBlocks;
import dev.zenolth.the_fog.common.entity.mimic.MimicEntity;
import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

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
            StairsBlock.class,
            FernBlock.class
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
            AbstractFireBlock.class,
            FernBlock.class
    );

    public static final List<TagKey<Block>> CLIMBABLE_TAGS = Arrays.asList(
        BlockTags.FENCES,
        BlockTags.WOODEN_FENCES,
        BlockTags.FENCE_GATES,
        BlockTags.SLABS,
        BlockTags.WOODEN_SLABS,
        BlockTags.STAIRS,
        BlockTags.WOODEN_STAIRS
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

    public static final Predicate<Entity> THE_MAN_ENTITY_PREDICATE = entity ->
            entity instanceof TheManEntity theMan && theMan.isReal() && theMan.isAlive() ||
            entity instanceof MimicEntity mimic && mimic.isAlive();

    public static final Predicate<Entity> THE_MAN_HALLUCINATION_ENTITY_PREDICATE = entity ->
            entity instanceof TheManEntity theMan && theMan.isHallucination() && !theMan.isParanoia() && theMan.isAlive();

    public static final Predicate<BlockState> EXCEPT_AIR = blockState -> !blockState.isAir();

    public static final BiPredicate<World, BlockPos> CLIMBABLE_PREDICATE = (world, blockPos) -> {
        var blockState = world.getBlockState(blockPos);
        if (blockState.isAir()) return false;
        if (blockState.getCollisionShape(world,blockPos) == VoxelShapes.empty()) return false;
        if (blockState.contains(Properties.OPEN)) return false;

        var block = blockState.getBlock();
        if (block instanceof LadderBlock) return false;

        var tag = blockState.streamTags().findAny();
        if (tag.isPresent() && CLIMBABLE_TAGS.contains(tag.get())) return true;

        return blockState.isFullCube(world,blockPos);
    };

    public static final Predicate<BlockState> LANTERN_PREDICATE = state -> state.isOf(ModBlocks.EREBUS_LANTERN);
}
