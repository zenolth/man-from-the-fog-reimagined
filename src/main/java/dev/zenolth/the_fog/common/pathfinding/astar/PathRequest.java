package dev.zenolth.the_fog.common.pathfinding.astar;

import net.minecraft.util.math.BlockPos;

public record PathRequest(BlockPos startPos, BlockPos targetPos) {
}
