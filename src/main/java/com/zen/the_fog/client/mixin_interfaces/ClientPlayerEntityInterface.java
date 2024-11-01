package com.zen.the_fog.client.mixin_interfaces;

public interface ClientPlayerEntityInterface {

    default void the_fog_is_coming$setGlitchMultiplier(float value) { }

    default float the_fog_is_coming$getGlitchMultiplier() {
        return 0f;
    }

    default void the_fog_is_coming$setFogDensity(double value) { }

    default double the_fog_is_coming$getFogDensity() { return 1.0; };
}
