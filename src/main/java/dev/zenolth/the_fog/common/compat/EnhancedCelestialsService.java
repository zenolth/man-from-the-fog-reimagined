package dev.zenolth.the_fog.common.compat;

import dev.corgitaco.enhancedcelestials.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials.api.EnhancedCelestialsRegistry;
import dev.corgitaco.enhancedcelestials.api.lunarevent.DefaultLunarEvents;
import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import dev.corgitaco.enhancedcelestials.fabric.EnhancedCelestialsFabric;
import dev.corgitaco.enhancedcelestials.lunarevent.EnhancedCelestialsLunarForecastWorldData;
import dev.zenolth.the_fog.common.services.ForecastService;
import dev.zenolth.the_fog.common.util.Console;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;

public class EnhancedCelestialsService implements ForecastService {
    @Override
    public boolean isLunarEventPresent(World world, RegistryKey<LunarEvent> lunarEventToCheck) {
        var worldData = EnhancedCelestials.lunarForecastWorldData(world);
        if (worldData.isEmpty()) return false;
        var lunarEvent = worldData.get().currentLunarEventHolder();
        var lunarEventKey = lunarEvent.getKey();

        return lunarEventKey.isPresent() && lunarEventKey.get() == lunarEventToCheck;
    }

    public boolean isBloodMoon(World world) {
        return isLunarEventPresent(world,DefaultLunarEvents.BLOOD_MOON);
    }

    public boolean isSuperBloodMoon(World world) {
        return isLunarEventPresent(world,DefaultLunarEvents.SUPER_BLOOD_MOON);
    }
}
