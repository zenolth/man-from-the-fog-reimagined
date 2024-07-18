package com.zen.fogman.common.gamerules;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.minecraft.world.GameRules;

public class ModGamerules {

    public static final GameRules.Key<GameRules.BooleanRule> MAN_SPAWN_CHANCE_SCALES = createRule("manSpawnChanceScales", GameRules.Category.SPAWNING, true);
    public static final GameRules.Key<DoubleRule> MAN_SPAWN_CHANCE = createRule("manSpawnChance", GameRules.Category.SPAWNING, 0.23, 0, 1);
    public static final GameRules.Key<DoubleRule> MAN_AMBIENT_SOUND_CHANCE = createRule("manAmbientSoundChance", GameRules.Category.SPAWNING, 0.55, 0, 1);
    public static final GameRules.Key<DoubleRule> MAN_SPAWN_INTERVAL = createRule("manSpawnInterval", GameRules.Category.SPAWNING, 15.0, 5.0, 60.0);

    //  HELPER FUNCTIONS
    // Double rules
    public static GameRules.Key<DoubleRule> createRule(String name,GameRules.Category category,double value) {
        return GameRuleRegistry.register(name,category,GameRuleFactory.createDoubleRule(value));
    }

    public static GameRules.Key<DoubleRule> createRule(String name,GameRules.Category category,double value,double minValue) {
        return GameRuleRegistry.register(name,category,GameRuleFactory.createDoubleRule(value,minValue));
    }

    public static GameRules.Key<DoubleRule> createRule(String name,GameRules.Category category,double value,double minValue,double maxValue) {
        return GameRuleRegistry.register(name,category,GameRuleFactory.createDoubleRule(value,minValue,maxValue));
    }

    // Int rules
    public static GameRules.Key<GameRules.IntRule> createRule(String name, GameRules.Category category, int value) {
        return GameRuleRegistry.register(name,category,GameRuleFactory.createIntRule(value));
    }

    public static GameRules.Key<GameRules.IntRule> createRule(String name,GameRules.Category category,int value,int minValue) {
        return GameRuleRegistry.register(name,category,GameRuleFactory.createIntRule(value,minValue));
    }

    public static GameRules.Key<GameRules.IntRule> createRule(String name,GameRules.Category category,int value,int minValue,int maxValue) {
        return GameRuleRegistry.register(name,category,GameRuleFactory.createIntRule(value,minValue,maxValue));
    }

    // Boolean rules
    public static GameRules.Key<GameRules.BooleanRule> createRule(String name,GameRules.Category category,boolean value) {
        return GameRuleRegistry.register(name,category,GameRuleFactory.createBooleanRule(value));
    }

    public static void registerGamerules() {

    }
}
