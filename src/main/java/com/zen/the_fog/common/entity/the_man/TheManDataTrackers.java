package com.zen.the_fog.common.entity.the_man;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public class TheManDataTrackers {
    public static final TrackedData<Float> SHIELD_HEALTH = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Boolean> CLIMBING = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> CROUCHING = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> CRAWLING = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> CROUCHING_COLLIDES_HEAD = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> CRAWLING_COLLIDES_HEAD = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Integer> STATE = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Boolean> IS_LUNGING = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
}
