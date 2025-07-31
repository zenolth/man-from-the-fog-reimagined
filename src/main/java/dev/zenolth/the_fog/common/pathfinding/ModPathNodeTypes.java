package dev.zenolth.the_fog.common.pathfinding;

import dev.zenolth.the_fog.common.EarlyRiser;
import net.minecraft.entity.ai.pathing.PathNodeType;

public class ModPathNodeTypes {
    public static PathNodeType CROUCHABLE = EarlyRiser.getNodeTypeEnum("CROUCHABLE");
    public static PathNodeType CRAWLABLE = EarlyRiser.getNodeTypeEnum("CRAWLABLE");
    public static PathNodeType WALL_HUGGING = EarlyRiser.getNodeTypeEnum("WALL_HUGGING");
}
