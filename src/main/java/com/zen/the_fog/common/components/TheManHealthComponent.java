package com.zen.the_fog.common.components;

import com.zen.the_fog.common.entity.the_man.TheManEntity;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;

public class TheManHealthComponent implements FloatComponent, AutoSyncedComponent {

    public static final String NBT_TAG = "theManHealth";

    public float value = (float) TheManEntity.createManAttributes().build().getValue(EntityAttributes.GENERIC_MAX_HEALTH);

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getFloat(NBT_TAG);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putFloat(NBT_TAG,this.value);
    }

    @Override
    public float getValue() {
        return this.value;
    }

    @Override
    public void setValue(float value) {
        this.value = value;
    }
}
