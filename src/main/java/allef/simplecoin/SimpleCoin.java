package allef.simplecoin;

import allef.simplecoin.commands.BalanceCommand;
import allef.simplecoin.commands.DepositCommand;
import allef.simplecoin.commands.TransferCommand;
import allef.simplecoin.commands.WithdrawCommand;
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
        DepositCommand.register();
        BalanceCommand.register();
        WithdrawCommand.register();
        TransferCommand.register();
        LOGGER.info("SimpleCoin mod initialized!");
    }
}