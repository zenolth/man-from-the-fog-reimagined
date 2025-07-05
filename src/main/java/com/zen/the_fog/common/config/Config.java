package com.zen.the_fog.common.config;

import com.google.gson.GsonBuilder;
import com.zen.the_fog.common.ManFromTheFog;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.ValueFormatters;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.List;

public class Config {
    public static ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(new Identifier(ManFromTheFog.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("man_from_the_fog_r_config.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    // SPAWNING CATEGORY

    @AutoGen(category = "default",group = "spawning")
    @FloatSlider(min=0,max=1,step=0.01f)
    @CustomName("Spawn Chance")
    @CustomDescription("Chance that The Man's spawn attempt will succeed")
    @CustomFormat(ValueFormatters.PercentFormatter.class)
    @SerialEntry(comment = "Value between 0 and 1 (0 being 0% and 1 being 100%)\nChance that The Man's spawn attempt will succeed")
    public float spawnChance = 0.33f;

    @AutoGen(category = "default",group = "spawning")
    @FloatSlider(min=0,max=1,step=0.01f)
    @CustomName("Spawn attempt fail chance")
    @CustomDescription("Chance that The Man's spawn attempt will fail and only his spawn sound will play at the location where he would've spawned")
    @CustomFormat(ValueFormatters.PercentFormatter.class)
    @SerialEntry(comment = "Value between 0 and 1 (0 being 0% and 1 being 100%)\nChance that The Man's spawn attempt will fail and only his spawn sound will play at the location where he would've spawned")
    public float fakeSpawnChance = 0.6f;

    @AutoGen(category = "default",group = "spawning")
    @CustomName("Multiply spawn chance with player count?")
    @CustomDescription("If enabled, The Man's spawn chance will be multiplied by the amount of players")
    @Boolean(formatter = Boolean.Formatter.YES_NO)
    @SerialEntry(comment = "If true, The Man's spawn chance will be multiplied by the amount of players")
    public boolean spawnChanceScalesWithPlayerCount = false;

    @AutoGen(category = "default",group = "spawning")
    @DoubleField(min = 1)
    @CustomName("Time between spawn attempts in seconds")
    @SerialEntry(comment = "Time between spawn attempts in seconds")
    public double timeBetweenSpawnAttempts = 10;

    @AutoGen(category = "default",group = "spawning")
    @CustomName("Spawn in day?")
    @CustomDescription("Should The Man spawn when it's day? (Spawn chance is halved when it's day)")
    @Boolean(formatter = Boolean.Formatter.YES_NO)
    @SerialEntry(comment = "If true, The Man will spawn in day\n(Spawn chance is halved when it's day)")
    public boolean spawnInDay = false;

    @AutoGen(category = "default",group = "spawning")
    @CustomName("Min Spawning Range (in blocks)")
    @IntSlider(min = 20,max = 120,step = 1)
    @SerialEntry(comment = "Min Spawning Range (in blocks)")
    public int minSpawnRange = 20;

    @AutoGen(category = "default",group = "spawning")
    @CustomName("Max Spawning Range (in blocks)")
    @IntSlider(min = 20,max = 120,step = 1)
    @SerialEntry(comment = "Max Spawning Range (in blocks)")
    public int maxSpawnRange = 60;

    @AutoGen(category = "default")
    @CustomName("Allowed dimensions")
    @CustomDescription("List of dimensions where The Man is allowed to spawn in\nValue should follow identifier format (namespace:dimension_id)")
    @ListGroup(valueFactory = StringListFactory.class,controllerFactory = StringListFactory.class)
    @SerialEntry(comment = "List of dimensions where The Man is allowed to spawn in\nValue should follow identifier format (namespace:dimension_id)")
    public List<String> allowedDimensions = List.of(DimensionTypes.OVERWORLD_ID.toString());

    // STATUS EFFECTS CATEGORY

    @AutoGen(category = "default",group = "status_effects")
    @MasterTickBox({"giveSpeed","giveDarkness"})
    @CustomName("Give status effects?")
    @CustomDescription("Should The Man give status effects to nearby players?")
    @SerialEntry(comment = "Should The Man give status effects to nearby players?")
    public boolean giveStatusEffects = true;

    @AutoGen(category = "default",group = "status_effects")
    @CustomName("Give speed?")
    @Boolean(formatter = Boolean.Formatter.YES_NO)
    @SerialEntry()
    public boolean giveSpeed = true;

    @AutoGen(category = "default",group = "status_effects")
    @CustomName("Give darkness?")
    @Boolean(formatter = Boolean.Formatter.YES_NO)
    @SerialEntry()
    public boolean giveDarkness = true;

    // GRIEFING CATEGORY

    @AutoGen(category = "default",group = "griefing")
    @MasterTickBox({"breakGlass","breakLightSources"})
    @CustomName("Allow griefing?")
    @SerialEntry()
    public boolean allowGriefing = true;

    @AutoGen(category = "default",group = "griefing")
    @CustomName("Break glass?")
    @Boolean(formatter = Boolean.Formatter.YES_NO)
    @SerialEntry()
    public boolean breakGlass = true;

    @AutoGen(category = "default",group = "griefing")
    @CustomName("Break light sources?")
    @Boolean(formatter = Boolean.Formatter.YES_NO)
    @SerialEntry()
    public boolean breakLightSources = true;

    // MISC CATEGORY

    @AutoGen(category = "default",group = "misc")
    @CustomName("Chase hunger cap")
    @CustomDescription("When chasing, The Man caps nearby players' hunger to this value (it wont go under the value)")
    @IntSlider(min = 1,max = 20,step = 1)
    @SerialEntry(comment = "When chasing, The Man caps nearby players' hunger to this value (it wont go under the value)")
    public int chaseHungerCap = 8;

    @AutoGen(category = "default",group = "misc")
    @CustomName("Summon cosmetic lightning?")
    @Boolean(formatter = Boolean.Formatter.YES_NO)
    @SerialEntry()
    public boolean summonCosmeticLightning = true;

    // WHITELIST CATEGORY
    @AutoGen(category = "default", group = "whitelist")
    @CustomName("Terror Player List")
    @CustomDescription("Map of player UUIDs to their terror mechanics preference (JOINED or DECLINED).")
    // YACL3 doesn't have a built-in Map controller, so we'll store it as a list of custom objects or rely on Gson's default map serialization.
    // For simplicity with YACL's structure and to avoid creating custom object serializers for now,
    // we'll let Gson handle it directly. This might not be editable in YACL's GUI screen without more complex setup.
    @SerialEntry(comment = "Map of player UUIDs to their terror mechanics preference (JOINED or DECLINED). Absence means undecided.")
    public java.util.Map<String, String> terrorPlayerList = new java.util.HashMap<>();

    public static Config get() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();
    }

    public static class StringListFactory implements ListGroup.ValueFactory<String>, ListGroup.ControllerFactory<String> {
        @Override
        public String provideNewValue() {
            return "";
        }

        @Override
        public ControllerBuilder<String> createController(ListGroup annotation, ConfigField<List<String>> field, OptionAccess storage, Option<String> option) {
            return StringControllerBuilder.create(option);
        }
    }
}