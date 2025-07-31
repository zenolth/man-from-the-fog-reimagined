package dev.zenolth.the_fog.common.config;

import dev.zenolth.the_fog.common.FogMod;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import org.jetbrains.annotations.NotNull;

public class ModConfig extends Config {
    public static class SpawningSection extends ConfigSection {
        @ValidatedFloat.Restrict(min = 0f,max = 1f) public float spawnChance = 0.33f;
        @ValidatedFloat.Restrict(min = 0f,max = 1f) public float fakeSpawnChance = 0.6f;
        @ValidatedFloat.Restrict(min = 0f,max = 1f) public float mimicSpawnChance = 0.6f;
        public boolean spawnChanceScalesWithPlayerCount = false;
        @ValidatedFloat.Restrict(min = 0.1f,max = 120f) public float timeBetweenSpawnAttempts = 10f;
        public boolean spawnInDay = false;
        @ValidatedInt.Restrict(min = 20,max = 120) public int minSpawnRange = 20;
        @ValidatedInt.Restrict(min = 20,max = 120) public int maxSpawnRange = 60;
        @ValidatedInt.Restrict(min = 1,max = 10) public int maxKillCount = 5;
        @ValidatedInt.Restrict(min = 1,max = 50) public int dayAmountToStopSpawn = 4;
    }

    public static class BehaviorSection extends ConfigSection {
        @ValidatedFloat.Restrict(min = 1f,max = 10f) public float speedMultiplier = 1f;
        @ValidatedInt.Restrict(min = 1,max = 120) public int forgetTime = 5;
    }

    public static class StatusEffectsSection extends ConfigSection {
        public boolean giveStatusEffects = true;
        public boolean giveSpeed = true;
        public boolean giveDarkness = true;
    }

    public static class MiscellaneousSection extends ConfigSection {
        @ValidatedInt.Restrict(min = 1,max = 20) public int chaseHungerCap = 8;
        public boolean summonCosmeticLightning = true;
        @ValidatedInt.Restrict(min = 1,max = 60) public int chatLogLifetime = 30;
        @ValidatedFloat.Restrict(min = 0.1f,max = 10f) public float fogDensityMultiplier = 1f;
    }

    public ModConfig() {
        super(FogMod.id("server_config"));
    }

    public SpawningSection spawning = new SpawningSection();
    public BehaviorSection behavior = new BehaviorSection();
    public StatusEffectsSection statusEffects = new StatusEffectsSection();
    public MiscellaneousSection miscellaneous = new MiscellaneousSection();

    @Override
    public int defaultPermLevel() {
        return 4;
    }

    @Override
    public @NotNull FileType fileType() {
        return FileType.JSON5;
    }
}