package com.zen.the_fog.client.config;

import com.zen.the_fog.common.config.Config;
import net.minecraft.client.gui.screen.Screen;

public class ConfigGUI {
    public static Screen getModConfigScreenFactory(Screen parent) {

        Config.HANDLER.configClass();

        return Config.HANDLER.generateGui().generateScreen(parent);
    }
}
