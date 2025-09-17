package allef.simplecoin;

import allef.simplecoin.commands.*;
import allef.simplecoin.data.BankingData;
import allef.simplecoin.item.ModItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCoin implements ModInitializer {

    public static final String MOD_ID = "simplecoin"; // Changed from "simple-coin"
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.initialize();
        DepositCommand.register();
        BalanceCommand.register();
        WithdrawCommand.register();
        SendCommand.register();

        // Initialize banking data
        ServerLifecycleEvents.SERVER_STARTED.register(BankingData::init);
        LOGGER.info("SimpleCoin mod initialized!");
    }
}