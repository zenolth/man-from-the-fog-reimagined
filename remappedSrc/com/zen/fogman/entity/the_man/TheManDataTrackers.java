package com.zen.fogman.entity.the_man;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public class TheManDataTrackers {
    public static final TrackedData<Boolean> CLIMBING = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Integer> STATE = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Float> TARGET_FOV = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.FLOAT);
}
