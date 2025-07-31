package dev.zenolth.the_fog.common;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.ai.pathing.PathNodeType;

public class EarlyRiser implements Runnable {
    @Override
    public void run() {
        var remapper = FabricLoader.getInstance().getMappingResolver();
        var pathNodeType = remapper.mapClassName("intermediary","net.minecraft.class_7");
        ClassTinkerers.enumBuilder(pathNodeType, float.class).addEnum("CROUCHABLE",0.5f).build();
        ClassTinkerers.enumBuilder(pathNodeType, float.class).addEnum("CRAWLABLE",1f).build();
        ClassTinkerers.enumBuilder(pathNodeType, float.class).addEnum("WALL_HUGGING",0f).build();
    }

    public static PathNodeType getNodeTypeEnum(String name) {
        return ClassTinkerers.getEnum(PathNodeType.class,name);
    }
}
