package dev.zenolth.the_fog.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.TypeFilter;

public class Util {
    public static TypeFilter<Entity,LivingEntity> LIVING_ENTITY_TYPE_FILTER = TypeFilter.instanceOf(LivingEntity.class);

}
