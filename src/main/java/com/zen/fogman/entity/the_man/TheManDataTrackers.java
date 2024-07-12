package com.zen.fogman.entity.the_man;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

import javax.sound.midi.Track;

public class TheManDataTrackers {
    public static final TrackedData<Boolean> CLIMBING = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Integer> STATE = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Boolean> IS_LUNGING = DataTracker.registerData(TheManEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
}
