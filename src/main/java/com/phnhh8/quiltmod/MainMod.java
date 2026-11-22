package com.phnhh8.quiltmod;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainMod implements ModInitializer {
    public static final String MOD_ID = "phnhh8_quiltmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize(ModContainer mod) {
        LOGGER.info("Mod {} cua Phnhh_8 da khoi dong tren Quilt 1.21.1!", mod.metadata().name());
    }
}
