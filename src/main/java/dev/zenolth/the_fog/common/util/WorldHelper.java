package dev.zenolth.the_fog.common.util;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.block.ErebusLanternBlock;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class WorldHelper {
    public static final Function<Integer,Integer> RADIUS_FUNCTION_DEFAULT = i -> i;
    public static Predicate<BlockPos> ANY_BLOCK = (pos) -> true;

    public static boolean isDay(World world) {
        world.calculateAmbientDarkness();
        return world.getAmbientDarkness() < 4;
    }

    public static boolean isNight(World world) {
        return !isDay(world);
    }

    public static boolean isEnhancedCelestialsPresent() {
        return FabricLoader.getInstance().isModLoaded("enhancedcelestials");
    }

    public static boolean isBloodMoon(World world) {
        return FogMod.FORECAST.isBloodMoon(world);
    }

    public static boolean isSuperBloodMoon(World world) {
        return FogMod.FORECAST.isSuperBloodMoon(world);
    }

    public static boolean isSolid(BlockView world, BlockState blockState, BlockPos pos) {
        if (blockState.isAir()) return false;
        if (blockState.isIn(BlockTags.CLIMBABLE)) return false;
        if (blockState.isIn(BlockTags.DOORS) || blockState.isIn(BlockTags.WOODEN_DOORS)) return false;
        if (blockState.isIn(BlockTags.TRAPDOORS) || blockState.isIn(BlockTags.WOODEN_TRAPDOORS)) return false;
        return blockState.getCollisionShape(world,pos) != VoxelShapes.empty();
    }

    public static boolean isSolid(BlockView world, BlockPos pos) {
       return isSolid(world,world.getBlockState(pos),pos);
    }

    public static boolean isSolid(BlockView world, int x, int y, int z) {
        return isSolid(world,new BlockPos(x,y,z));
    }

    /**
     * Generates a random pos around pos
     * @param world The World
     * @param origin Position to generate around
     * @param direction Direction the "player" is looking into
     * @param minRange Minimum range to generate
     * @param maxRange Maximum range to generate
     * @return The generated pos
     */
    public static Vec3d getRandomSpawnBehindDirection(WorldView world, Vec3d origin, Vec3d direction, int minRange, int maxRange) {
        direction = direction.multiply(-1);
        direction = direction.rotateY((float) Math.toRadians((RandomNum.next(-60,60))));

        Vec3d normalizedDirection = direction.normalize();
        int range;

        if (minRange == maxRange) {
            range = minRange;
        } else {
            range = maxRange > minRange ? RandomNum.next(minRange,maxRange) : RandomNum.next(maxRange,minRange);
        }

        int initialRange = range;

        Vec3d spawnDirection = normalizedDirection.multiply(initialRange);

        BlockPos blockPos = getTopPosition(world,BlockPos.ofFloored(origin.add(spawnDirection)));

        while (isInLightSource(world,blockPos, TheManPredicates.LANTERN_PREDICATE)) {
            initialRange += 15;
            spawnDirection = normalizedDirection.multiply(initialRange);
            blockPos = getTopPosition(world,BlockPos.ofFloored(origin.add(spawnDirection)));
        }

        return blockPos.toCenterPos();
    }

    /**
     * Generates a random pos around pos
     * @param serverWorld The World
     * @param origin Position to generate around
     * @param direction Direction the "player" is looking into
     * @return The generated pos
     */
    public static Vec3d getRandomSpawnBehindDirection(ServerWorld serverWorld, Vec3d origin, Vec3d direction) {
        return getRandomSpawnBehindDirection(
                serverWorld,
                origin,
                direction,
                FogMod.CONFIG.spawning.minSpawnRange,
                FogMod.CONFIG.spawning.maxSpawnRange
        );
    }

    /**
     *
     * @param world The world to check for
     * @param pos {@link BlockPos} to manipulate
     * @return A {@link BlockPos} that's not in any block and the block under it is solid/not air
     */
    public static BlockPos getTopPosition(WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);

        while (!blockState.isAir()) {
            pos = pos.up();
            blockState = world.getBlockState(pos);
            if (pos.getY() >= world.getTopY()) {
                break;
            }
        }

        BlockState blockStateDown = world.getBlockState(pos.down());

        while (blockStateDown.isAir()) {
            pos = pos.down();
            blockStateDown = world.getBlockState(pos.down());
            if (pos.getY() < world.getBottomY()) {
                break;
            }
        }

        return pos;
    }

    @Nullable
    public static BlockPos getClosestBlockPos(BlockPos center, Iterable<BlockPos> blockPosesToCheck, Predicate<BlockPos> blockPosPredicate) {
        double closestDistance = -1.0;
        BlockPos target = null;

        for (var blockPos : blockPosesToCheck) {
            if (!blockPosPredicate.test(blockPos)) continue;
            double distance = blockPos.toCenterPos().distanceTo(center.toCenterPos());

            if (closestDistance == -1.0 || distance < closestDistance) {
                closestDistance = distance;
                target = blockPos;
            }
        }

        return target;
    }

    public static BlockPos getClosestBlockPos(BlockPos center, Iterable<BlockPos> blockPosesToCheck) {
        return getClosestBlockPos(center,blockPosesToCheck,blockPos -> true);
    }

    public static boolean isBlockPresent(WorldView world, BlockPos pos) {
        return isSolid(world,pos);
    }

    public static boolean areBlocksAround(WorldView world, BlockPos pos, int rangeX, int rangeY, int rangeZ) {
        for (var blockPos : BlockPos.iterateOutwards(pos,rangeX,rangeY,rangeZ)) {
            if (blockPos.equals(pos)) continue;
            if (isSolid(world,blockPos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean areBlocksAround(WorldView world, BlockPos pos, int rangeY) {
        for (int y = 1; y <= rangeY; y++) {
            var blockPos = pos.up(y);
            if (isSolid(world,blockPos)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<BlockPos> blockCast(WorldView world, BlockPos start, BlockPos end, Predicate<BlockPos> blockPredicate) {
        if (start.equals(end)) return Optional.empty();
        if (!world.getBlockState(start).isAir() && blockPredicate.test(start)) return Optional.of(start);

        for (var pos : BlockPos.iterate(start,end)) {
            if (pos.equals(start) || pos.equals(end)) continue;
            var blockState = world.getBlockState(pos);
            if (blockState.isAir() || blockState.getCollisionShape(world,pos) == VoxelShapes.empty()) continue;
            if (blockPredicate.test(pos)) return Optional.of(pos);
        }

        return Optional.empty();
    }

    public static Optional<BlockPos> blockCast(WorldView world, BlockPos start, BlockPos end) {
        return blockCast(world,start,end, ANY_BLOCK);
    }

    public static boolean isBlockInChunk(ChunkPos chunkPos, BlockPos pos) {
        var startX = chunkPos.getStartX();
        var startZ = chunkPos.getStartZ();
        var endX = chunkPos.getEndX();
        var endZ = chunkPos.getEndZ();
        return (startX >= pos.getX() && startZ >= pos.getZ()) || (endX <= pos.getX() && endZ <= pos.getZ());
    }

    public static boolean isOnSurface(WorldView world, BlockPos pos) {
        if (world.isSkyVisible(pos)) return true;

        var biome = world.getBiome(pos);

        if (biome.isIn(ConventionalBiomeTags.CAVES)) {
            return false;
        }

        var baseSkyLight = world.getLightLevel(LightType.SKY,pos) - world.getAmbientDarkness();

        return baseSkyLight > 0;
    }

    public static boolean isOnSurface(WorldView world, Entity entity) {
        return isOnSurface(world, entity.getBlockPos());
    }

    public static boolean isInLightSource(WorldView world, BlockPos pos, Predicate<BlockState> lightSourcePredicate, Function<Integer,Integer> radiusFunction) {
        var lightLevel = world.getLightLevel(pos);
        if (lightLevel <= 15 - world.getAmbientDarkness()) {
            return false;
        }

        var cubeRadius = radiusFunction.apply(Math.max(1, 15 - lightLevel));

        for (var blockPos : BlockPos.iterateOutwards(pos,cubeRadius,cubeRadius,cubeRadius)) {
            var state = world.getBlockState(blockPos);
            if (lightSourcePredicate.test(state)) {
                if (state.contains(ErebusLanternBlock.POWER)) {
                    var power = state.get(ErebusLanternBlock.POWER);
                    if (pos.getManhattanDistance(blockPos) <= power) {
                        return true;
                    }
                    continue;
                }
                return true;
            }
        }

        return false;
    }

    public static boolean isInLightSource(WorldView world, BlockPos pos, Predicate<BlockState> lightSourcePredicate) {
        return isInLightSource(world,pos,lightSourcePredicate, RADIUS_FUNCTION_DEFAULT);
    }

    @Nullable
    public static BlockPos getLightSource(WorldView world, BlockPos pos, Predicate<BlockState> lightSourcePredicate, Function<Integer,Integer> radiusFunction) {
        var lightLevel = world.getLightLevel(pos);
        if (lightLevel <= 15 - world.getAmbientDarkness()) {
            return null;
        }

        var cubeRadius = radiusFunction.apply(Math.max(1, 15 - lightLevel));

        for (var blockPos : BlockPos.iterateOutwards(pos,cubeRadius,cubeRadius,cubeRadius)) {
            if (lightSourcePredicate.test(world.getBlockState(blockPos))) {
                return blockPos;
            }
        }

        return null;
    }

    @Nullable
    public static BlockPos getLightSource(WorldView world, BlockPos pos, Predicate<BlockState> lightSourcePredicate) {
        return getLightSource(world,pos,lightSourcePredicate, RADIUS_FUNCTION_DEFAULT);
    }
}
