package com.zen.fogman.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final Identifier MAN_PAIN_ID = new Identifier("man:man_pain");
    public static SoundEvent MAN_PAIN = SoundEvent.of(MAN_PAIN_ID);

    public static final Identifier MAN_ATTACK_ID = new Identifier("man:man_attack");
    public static SoundEvent MAN_ATTACK = SoundEvent.of(MAN_ATTACK_ID);

    public static final Identifier MAN_SPOT_ID = new Identifier("man:man_spot");
    public static SoundEvent MAN_SPOT = SoundEvent.of(MAN_SPOT_ID);

    public static final Identifier MAN_IDLECALM_ID = new Identifier("man:man_idlecalm");
    public static SoundEvent MAN_IDLECALM = SoundEvent.of(MAN_IDLECALM_ID);

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT,MAN_PAIN_ID,MAN_PAIN);
        Registry.register(Registries.SOUND_EVENT,MAN_ATTACK_ID,MAN_ATTACK);
        Registry.register(Registries.SOUND_EVENT,MAN_SPOT_ID,MAN_SPOT);
        Registry.register(Registries.SOUND_EVENT,MAN_IDLECALM_ID,MAN_IDLECALM);
    }
}
