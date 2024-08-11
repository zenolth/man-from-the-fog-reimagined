package com.zen.fogman.client.mixin_interfaces;

public interface ClientPlayerEntityInterface {

    default void the_fog_is_coming$setGlitchMultiplier(float value) {

    }

    default float the_fog_is_coming$getGlitchMultiplier() {
        return 0f;
    }
}
