package com.zen.fogman.sounds;

import com.zen.fogman.ManFromTheFog;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final Identifier MAN_ALARM_ID = new Identifier(parseName("man_alarm"));
    public static SoundEvent MAN_ALARM = SoundEvent.of(MAN_ALARM_ID);

    public static final Identifier MAN_PAIN_ID = new Identifier(parseName("man_pain"));
    public static SoundEvent MAN_PAIN = SoundEvent.of(MAN_PAIN_ID);

    public static final Identifier MAN_ATTACK_ID = new Identifier(parseName("man_attack"));
    public static SoundEvent MAN_ATTACK = SoundEvent.of(MAN_ATTACK_ID);

    public static final Identifier MAN_SLASH_ID = new Identifier(parseName("man_slash"));
    public static SoundEvent MAN_SLASH = SoundEvent.of(MAN_SLASH_ID);

    public static final Identifier MAN_SPOT_ID = new Identifier(parseName("man_spot"));
    public static SoundEvent MAN_SPOT = SoundEvent.of(MAN_SPOT_ID);

    public static final Identifier MAN_IDLECALM_ID = new Identifier(parseName("man_idlecalm"));
    public static SoundEvent MAN_IDLECALM = SoundEvent.of(MAN_IDLECALM_ID);

    public static final Identifier MAN_CHASE_ID = new Identifier(parseName("man_chase"));
    public static SoundEvent MAN_CHASE = SoundEvent.of(MAN_CHASE_ID);

    public static final Identifier MAN_DEATH_ID = new Identifier(parseName("man_death"));
    public static SoundEvent MAN_DEATH = SoundEvent.of(MAN_DEATH_ID);

    public static final Identifier MAN_LUNGE_ID = new Identifier(parseName("man_lunge"));
    public static SoundEvent MAN_LUNGE = SoundEvent.of(MAN_LUNGE_ID);

    public static final Identifier MAN_CREEPY_ID = new Identifier(parseName("man_creepy"));
    public static SoundEvent MAN_CREEPY = SoundEvent.of(MAN_CREEPY_ID);

    public static String parseName(String name) {
        return ManFromTheFog.MOD_ID + ":" + name;
    }

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT,MAN_ALARM_ID,MAN_ALARM);
        Registry.register(Registries.SOUND_EVENT,MAN_PAIN_ID,MAN_PAIN);
        Registry.register(Registries.SOUND_EVENT,MAN_ATTACK_ID,MAN_ATTACK);
        Registry.register(Registries.SOUND_EVENT,MAN_SLASH_ID,MAN_SLASH);
        Registry.register(Registries.SOUND_EVENT,MAN_SPOT_ID,MAN_SPOT);
        Registry.register(Registries.SOUND_EVENT,MAN_IDLECALM_ID,MAN_IDLECALM);
        Registry.register(Registries.SOUND_EVENT,MAN_CHASE_ID,MAN_CHASE);
        Registry.register(Registries.SOUND_EVENT,MAN_DEATH_ID,MAN_DEATH);
        Registry.register(Registries.SOUND_EVENT,MAN_LUNGE_ID,MAN_LUNGE);
        Registry.register(Registries.SOUND_EVENT,MAN_CREEPY_ID,MAN_CREEPY);
    }
}
