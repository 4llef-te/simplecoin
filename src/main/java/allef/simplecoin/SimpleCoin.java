package allef.simplecoin;

import allef.simplecoin.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCoin implements ModInitializer {

    public static final String MOD_ID = "simplecoin"; // Changed from "simple-coin"
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.initialize();
        LOGGER.info("SimpleCoin mod initialized!");
    }
}