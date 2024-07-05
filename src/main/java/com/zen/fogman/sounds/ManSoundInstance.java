package com.zen.fogman.sounds;

import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class ManSoundInstance extends EntityTrackingSoundInstance {

    private float volumeModifier = 1.0f;
    private final float defaultVolume;

    public ManSoundInstance(SoundEvent sound, SoundCategory category, float volume, float pitch, Entity entity, long seed) {
        super(sound, category, volume, pitch, entity, seed);
        this.defaultVolume = volume;

        this.volume = this.defaultVolume * this.volumeModifier;
        this.pitch = pitch;
    }

    public void setVolumeModifier(float newVolume) {
        this.volumeModifier = newVolume;
        if (this.volumeModifier < 0) {
            this.volumeModifier = 0;
        }
    }

    public float getVolumeModifier() {
        return this.volumeModifier;
    }

    @Override
    public void tick() {
        super.tick();
        this.volume = this.defaultVolume * this.volumeModifier;
    }
}
