package allef.simplecoin.commands;

import allef.simplecoin.data.BankingData;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static allef.simplecoin.commands.DepositCommand.getCoinByName;

public class WithdrawCommand {
    private static final List<String> COIN_NAMES = Arrays.asList(
            "gold", "copper", "diamond", "emerald",
            "iron", "netherite", "continental", "credit", "all"
    );

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("withdraw")
                .then(CommandManager.argument("coin", StringArgumentType.word())
                        .suggests(WithdrawCommand::suggestCoins)
                        .then(CommandManager.argument("amount", StringArgumentType.word())
                                .suggests(WithdrawCommand::suggestAmounts)
                                .executes(WithdrawCommand::executeWithdraw)))
                .executes(WithdrawCommand::executeWithdrawAll)));
    }

    private static int executeWithdraw(CommandContext<?> ctx) {
        ServerPlayerEntity player = ((net.minecraft.server.command.ServerCommandSource) ctx.getSource()).getPlayer();
        String coinArg = StringArgumentType.getString(ctx, "coin").toLowerCase();
        String amountArg = StringArgumentType.getString(ctx, "amount").toLowerCase();

        if (coinArg.equals("all")) return executeWithdrawAll(ctx);

        Item coinItem = getCoinByName(coinArg);
        if (coinItem == null) {
            assert player != null;
            player.sendMessage(Text.literal("Unknown coin type: " + coinArg), false);
            return 0;
        }

        int amount;
        if (amountArg.equals("all")) {
            assert player != null;
            amount = BankingData.getBalance(player, coinArg);
        } else {
            try {
                amount = Integer.parseInt(amountArg);
            } catch (NumberFormatException e) {
                assert player != null;
                player.sendMessage(Text.literal("Invalid amount: " + amountArg), false);
                return 0;
            }
        }

        if (amount <= 0 || !BankingData.withdrawBalance(Objects.requireNonNull(player), coinArg, amount)) {
            assert player != null;
            player.sendMessage(Text.literal("Not enough balance!"), false);
            return 0;
        }

        giveCoins(player, coinItem, amount);
        player.sendMessage(Text.literal("Withdrew " + amount + " " + coinArg +
                ". Balance: " + BankingData.getBalance(player, coinArg)), false);
        return 1;
    }

    private static int executeWithdrawAll(CommandContext<?> ctx) {
        ServerPlayerEntity player = ((net.minecraft.server.command.ServerCommandSource) ctx.getSource()).getPlayer();
        assert player != null;
        Map<String, Integer> withdrawn = BankingData.withdrawAll(player);
        int total = 0;

        for (String coin : withdrawn.keySet()) {
            int amount = withdrawn.get(coin);
            if (amount > 0) {
                giveCoins(player, getCoinByName(coin), amount);
                total += amount;
            }
        }

        player.sendMessage(Text.literal("Withdrew all coins. Total: " + total), false);
        return total > 0 ? 1 : 0;
    }

    private static void giveCoins(ServerPlayerEntity player, Item coin, int amount) {
        while (amount > 0) {
            int stack = Math.min(64, amount);
            player.giveItemStack(new ItemStack(coin, stack));
            amount -= stack;
        }
    }

    private static CompletableFuture<Suggestions> suggestCoins(CommandContext<?> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(COIN_NAMES, builder);
    }

    private static CompletableFuture<Suggestions> suggestAmounts(CommandContext<?> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(List.of("all"), builder);
    }

}
