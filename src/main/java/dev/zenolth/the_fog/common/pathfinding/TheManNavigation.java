package dev.zenolth.the_fog.common.pathfinding;

import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class TheManNavigation extends MobNavigation {
    public TheManNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    @Override
    protected boolean isAtValidPosition() {
        if (this.entity instanceof TheManEntity theMan) {
            if (theMan.climbing.get() || theMan.crouching.get() || theMan.crawling.get()) {
                return true;
            }
        }

        return super.isAtValidPosition();
    }

    @Override
    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new TheManPathNodeMaker();
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(this.nodeMaker,TheManEntity.CHASE_DISTANCE * 16);
    }
}
