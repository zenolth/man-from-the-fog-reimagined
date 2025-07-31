package dev.zenolth.the_fog.common.services;

import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public interface ForecastService {
    boolean isLunarEventPresent(World world,RegistryKey<LunarEvent> lunarEvent);
    boolean isBloodMoon(World world);
    boolean isSuperBloodMoon(World world);
}
