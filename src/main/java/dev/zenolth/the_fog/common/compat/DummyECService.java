package dev.zenolth.the_fog.common.compat;

import dev.corgitaco.enhancedcelestials.api.lunarevent.LunarEvent;
import dev.zenolth.the_fog.common.services.ForecastService;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class DummyECService implements ForecastService {


    @Override
    public boolean isLunarEventPresent(World world, RegistryKey<LunarEvent> lunarEvent) {
        return false;
    }

    @Override
    public boolean isBloodMoon(World world) {
        return false;
    }

    @Override
    public boolean isSuperBloodMoon(World world) {
        return false;
    }
}
