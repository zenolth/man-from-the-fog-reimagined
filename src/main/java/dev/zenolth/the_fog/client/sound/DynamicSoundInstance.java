package dev.zenolth.the_fog.client.sound;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class DynamicSoundInstance extends PositionedSoundInstance implements TickableSoundInstance {

    private boolean done = false;
    private final boolean shouldRepeat;

    public DynamicSoundInstance(SoundEvent sound, SoundCategory category, float volume, float pitch, Random random, BlockPos pos) {
        super(sound, category, volume, pitch, random, pos);
        this.shouldRepeat = this.repeat;
    }

    public DynamicSoundInstance(SoundEvent sound, SoundCategory category, float volume, float pitch, Random random, double x, double y, double z) {
        super(sound, category, volume, pitch, random, x, y, z);
        this.shouldRepeat = this.repeat;
    }

    public DynamicSoundInstance(Identifier id, SoundCategory category, float volume, float pitch, Random random, boolean repeat, int repeatDelay, AttenuationType attenuationType, double x, double y, double z, boolean relative) {
        super(id, category, volume, pitch, random, repeat, repeatDelay, attenuationType, x, y, z, relative);
        this.shouldRepeat = this.repeat;
    }

    public static DynamicSoundInstance loop(SoundEvent sound,SoundCategory category,float volume,float pitch) {
        return new DynamicSoundInstance(sound.getId(), category, volume, pitch, SoundInstance.createRandom(), true, 0, SoundInstance.AttenuationType.NONE, 0.0, 0.0, 0.0, true);
    }

    public static DynamicSoundInstance loop(SoundEvent sound,float volume,float pitch) {
        return loop(sound,SoundCategory.MASTER,volume,pitch);
    }

    public static DynamicSoundInstance master(SoundEvent sound, float volume) {
        return master(sound, volume, 0.25F);
    }

    public static DynamicSoundInstance master(RegistryEntry<SoundEvent> sound, float volume) {
        return master(sound.value(), volume);
    }

    public static DynamicSoundInstance master(SoundEvent sound, float volume, float pitch) {
        return new DynamicSoundInstance(
                sound.getId(), SoundCategory.MASTER, volume, pitch, SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.NONE, 0.0, 0.0, 0.0, true
        );
    }

    public static DynamicSoundInstance music(SoundEvent sound) {
        return new DynamicSoundInstance(
                sound.getId(), SoundCategory.MUSIC, 1.0F, 1.0F, SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.NONE, 0.0, 0.0, 0.0, true
        );
    }

    public static DynamicSoundInstance ambient(SoundEvent sound, float volume, float pitch) {
        return new DynamicSoundInstance(
                sound.getId(), SoundCategory.AMBIENT, volume, pitch, SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.NONE, 0.0, 0.0, 0.0, true
        );
    }

    public static DynamicSoundInstance ambient(SoundEvent sound) {
        return ambient(sound, 1.0F, 1.0F);
    }

    @Override
    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float vol) {
        this.volume = vol;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pit) {
        this.pitch = pit;
    }

    public void finish() {
        this.done = true;
        if (this.shouldRepeat) {
            this.repeat = false;
        }
    }

    public void reset() {
        this.done = false;
        if (this.shouldRepeat) {
            this.repeat = true;
        }
    }

    @Override
    public boolean isDone() {
        return this.done;
    }

    @Override
    public void tick() {

    }
}
