package dev.zenolth.the_fog.common.data_tracker;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;

public class TrackingData<E extends Entity,T> {
    private final E entity;
    private final TrackedData<T> trackedData;

    public TrackingData(E entity,TrackedData<T> trackedData,T initialValue) {
        this.entity = entity;
        this.trackedData = trackedData;
        this.entity.getDataTracker().startTracking(this.trackedData,initialValue);
    }

    public static <S> TrackedData<S> register(Class<? extends Entity> entityClass,TrackedDataHandler<S> dataHandler) {
        return DataTracker.registerData(entityClass,dataHandler);
    }

    public void set(T value) {
        this.entity.getDataTracker().set(this.trackedData,value);
    }

    public T get() {
        return this.entity.getDataTracker().get(this.trackedData);
    }
}
