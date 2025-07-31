package dev.zenolth.the_fog.common.animation;

import software.bernie.geckolib.core.animation.RawAnimation;

public class TheManAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    public static final RawAnimation CROUCH_RUN = RawAnimation.begin().thenLoop("runcrouch");
    public static final RawAnimation CRAWL_RUN = RawAnimation.begin().thenLoop("runcrawl");
    public static final RawAnimation SNEAK_RUN = RawAnimation.begin().thenLoop("sneakrun");

    public static final RawAnimation CLIMB = RawAnimation.begin().thenLoop("climb");
}
