package com.natamus.collective.functions;

import net.minecraft.client.Options;

public class GameSettingsFunctions {
    public static void setGamma(Options options, double gamma) {
        options.gamma = gamma;
    }
    public static double getGamma(Options options) {
        return options.gamma;
    }
}
