package com.zen.fogman.goals.custom;

import net.minecraft.block.*;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

public class BreakDoorInstantGoal
        extends DoorInteractGoal {

    public BreakDoorInstantGoal(MobEntity mob) {
        super(mob);
    }

    @Override
    public boolean canStart() {
        if (!NavigationConditions.hasMobNavigation(this.mob)) {
            return false;
        }
        if (!this.mob.horizontalCollision) {
            return false;
        }
        MobNavigation mobNavigation = (MobNavigation)this.mob.getNavigation();
        Path path = mobNavigation.getCurrentPath();
        if (path == null || path.isFinished() || !mobNavigation.canEnterOpenDoors()) {
            return false;
        }
        for (int i = 0; i < Math.min(path.getCurrentNodeIndex() + 2, path.getLength()); ++i) {
            PathNode pathNode = path.getNode(i);
            this.doorPos = new BlockPos(pathNode.x, pathNode.y + 1, pathNode.z);
            if (this.mob.squaredDistanceTo(this.doorPos.getX(), this.mob.getY(), this.doorPos.getZ()) > 2.25) continue;
            this.doorValid = DoorBlock.canOpenByHand(this.mob.method_48926(), this.doorPos);

            if (!this.doorValid) continue;
            return true;
        }
        this.doorPos = this.mob.getBlockPos().up();
        this.doorValid = DoorBlock.canOpenByHand(this.mob.method_48926(), this.doorPos);
        return this.doorValid;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.canStart()) {
            this.mob.method_48926().removeBlock(this.doorPos, false);
            this.mob.method_48926().syncWorldEvent(WorldEvents.ZOMBIE_BREAKS_WOODEN_DOOR, this.doorPos, 0);
            this.mob.method_48926().syncWorldEvent(WorldEvents.BLOCK_BROKEN, this.doorPos, Block.getRawIdFromState(this.mob.method_48926().getBlockState(this.doorPos)));
        }
    }
}

