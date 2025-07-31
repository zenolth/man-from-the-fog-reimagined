package dev.zenolth.the_fog.common.sounds;

import dev.zenolth.the_fog.common.FogMod;
import dev.zenolth.the_fog.common.util.Console;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static SoundEvent NIGHT_AMBIENCE = registerSound("night_ambience");
    public static SoundEvent CAVE_AMBIENCE = registerSound("cave_ambience");

    public static SoundEvent HORROR = registerSound("horror");
    public static SoundEvent SHIELD_BREAK = registerSound("shield_break");

    public static SoundEvent MAN_ALARM = registerSound("man_alarm");
    public static SoundEvent MAN_PAIN = registerSound("man_pain");
    public static SoundEvent MAN_ATTACK = registerSound("man_attack");
    public static SoundEvent MAN_SPIT = registerSound("man_spit");
    public static SoundEvent MAN_SLASH = registerSound("man_slash");
    public static SoundEvent MAN_CHASE = registerSound("man_chase");
    public static SoundEvent MAN_DEATH = registerSound("man_death");
    public static SoundEvent MAN_LUNGE = registerSound("man_lunge");
    public static SoundEvent MAN_LUNGE_ATTACK = registerSound("man_lungeattack");
    public static SoundEvent MAN_CREEPY = registerSound("man_creepy");

    public static SoundEvent MIMIC_REVEAL = registerSound("mimic_reveal");
    public static SoundEvent MIMIC_REVEAL_JUMPSCARE = registerSound("mimic_reveal_jumpscare");

    public static Identifier getIdentifier(String id) {
        return FogMod.id(id);
    }

    public static SoundEvent registerSound(String id) {
        Identifier soundIdentifier = getIdentifier(id);
        return Registry.register(Registries.SOUND_EVENT,soundIdentifier,SoundEvent.of(soundIdentifier));
    }

    public static void register() {
        Console.writeln("Registering SoundEvents");
        Console.writeln("Registered SoundEvents");
    }
}
