package allef.simplecoin.commands;

import allef.simplecoin.data.BankingData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class BalanceCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("balance")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    assert player != null;
                    Map<String, Integer> balances = BankingData.getAllBalances(player);

                    if (balances.isEmpty()) {
                        player.sendMessage(Text.literal("You have no coins in your bank."), false);
                    } else {
                        player.sendMessage(Text.literal("Your bank balances:"), false);
                        for (String coin : balances.keySet()) {
                            player.sendMessage(Text.literal(coin + ": " + balances.get(coin)), false);
                        }
                    }
                    return 1;
                })));
    }
}
