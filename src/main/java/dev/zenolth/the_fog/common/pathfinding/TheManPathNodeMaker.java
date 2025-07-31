package dev.zenolth.the_fog.common.pathfinding;

import dev.zenolth.the_fog.common.entity.the_man.TheManEntity;
import dev.zenolth.the_fog.common.predicate.TheManPredicates;
import dev.zenolth.the_fog.common.util.WorldHelper;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class TheManPathNodeMaker extends LandPathNodeMaker {
    @Override
    public PathNodeType getNodeType(BlockView blockView, int x, int y, int z, MobEntity mob) {
        if (mob instanceof TheManEntity theMan && theMan.isReal() && theMan.getTarget() != null) {
            //var targetEntity = theMan.getTarget();
            var pos = new BlockPos(x,y,z);
            var world = theMan.getWorld();

            /*if (y > theMan.getBlockY() && Math.abs(y - theMan.getBlockY()) > 1) {
                var climbPos = theMan.getPillarPos(pos);
                if (climbPos != null) {
                    int height = theMan.getPillarHeight(climbPos);
                    if (height > 1 && theMan.isClimbable(pos,targetEntity.getBlockPos(),height)) return PathNodeType.OPEN;
                }
            }*/

            if (!WorldHelper.isSuperBloodMoon(world)) {
                if (WorldHelper.isInLightSource(world,pos, TheManPredicates.LANTERN_PREDICATE)) {
                    return PathNodeType.DANGER_OTHER;
                }
            }
        }

        var solid = WorldHelper.isSolid(blockView,x,y,z);

        if (solid) return PathNodeType.BLOCKED;

        var floorSolid = WorldHelper.isSolid(blockView,x,y - 1,z);

        var chestSolid = WorldHelper.isSolid(blockView, x, y + 1, z);
        var headSolid = WorldHelper.isSolid(blockView,x,y + 2,z);

        if (y > mob.getBlockY()) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;
                    if (WorldHelper.isSolid(blockView,x + i,y,z + j))
                        return ModPathNodeTypes.WALL_HUGGING;
                }
            }
        }

        if (floorSolid) {
            if (!chestSolid && headSolid) return ModPathNodeTypes.CROUCHABLE;
            if (chestSolid && headSolid) return ModPathNodeTypes.CRAWLABLE;
            return PathNodeType.WALKABLE;
        }

        //var nodeType = super.getNodeType(blockView, x, y, z, mob);
        return PathNodeType.OPEN;
    }
}
