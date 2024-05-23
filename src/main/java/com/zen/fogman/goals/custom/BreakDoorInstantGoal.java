package com.zen.fogman.goals.custom;

import java.util.function.Predicate;

import com.zen.fogman.ManFromTheFog;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldEvents;

public class BreakDoorInstantGoal
        extends DoorInteractGoal {

    public BreakDoorInstantGoal(MobEntity mob) {
        super(mob);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.canStart()) {
            this.mob.getWorld().removeBlock(this.doorPos, false);
            this.mob.getWorld().syncWorldEvent(WorldEvents.ZOMBIE_BREAKS_WOODEN_DOOR, this.doorPos, 0);
            this.mob.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, this.doorPos, Block.getRawIdFromState(this.mob.getWorld().getBlockState(this.doorPos)));
        }
    }
}

