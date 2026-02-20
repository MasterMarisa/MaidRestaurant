package com.mastermarisa.maid_restaurant.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class RestaurantConfig {
    public static boolean SIT_WHILE_COOKING() { return Server.SIT_WHILE_COOKING.getAsBoolean(); }

    public static void register(ModContainer modContainer) {
        Server.register(modContainer);
    }

    private static class Server {
        private static final ModConfigSpec.Builder BUILDER;
        private static final ModConfigSpec SPEC;
        public static final ModConfigSpec.BooleanValue SIT_WHILE_COOKING;

        public static void register(ModContainer modContainer) {
            modContainer.registerConfig(ModConfig.Type.SERVER, SPEC);
        }

        static {
            BUILDER = new ModConfigSpec.Builder();

            SIT_WHILE_COOKING = BUILDER.
                    translation("config.maid_restaurant.server.sit_while_cooking").
                    define("sit_while_cooking", true);

            SPEC = BUILDER.build();
        }
    }
}
