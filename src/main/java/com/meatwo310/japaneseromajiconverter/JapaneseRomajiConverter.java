package com.meatwo310.japaneseromajiconverter;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;


public class JapaneseRomajiConverter implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Hello from Initializer");
    }
}
