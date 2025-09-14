package allef.simplecoin.commands;

import allef.simplecoin.data.BankingData;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static allef.simplecoin.commands.DepositCommand.getCoinByName;

public class  TransferCommand {

    private static final List<String> COIN_NAMES = Arrays.asList(
            "gold", "copper", "diamond", "emerald",
            "iron", "netherite", "continental", "credit", "all"
    );

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("transfer")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.argument("target", StringArgumentType.word())
                        .suggests(TransferCommand::suggestPlayers)
                        .then(CommandManager.argument("coin", StringArgumentType.word())
                                .suggests(TransferCommand::suggestCoins)
                                .then(CommandManager.argument("amount", StringArgumentType.word())
                                        .suggests(TransferCommand::suggestAmounts)
                                        .executes(TransferCommand::executeTransfer))))));
    }

    private static int executeTransfer(CommandContext<?> ctx) {
        ServerPlayerEntity sender = ((net.minecraft.server.command.ServerCommandSource) ctx.getSource()).getPlayer();
        String targetName = StringArgumentType.getString(ctx, "target");
        String coinArg = StringArgumentType.getString(ctx, "coin").toLowerCase();
        String amountArg = StringArgumentType.getString(ctx, "amount").toLowerCase();

        // Find target player
        assert sender != null;
        ServerPlayerEntity target = Objects.requireNonNull(sender.getServer()).getPlayerManager().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(Text.literal("Player not found: " + targetName), false);
            return 0;
        }

        // Transfer all coins
        if (coinArg.equals("all")) {
            Map<String, Integer> allBalances = BankingData.getAllBalances(sender);
            if (allBalances.isEmpty()) {
                sender.sendMessage(Text.literal("You have no coins to transfer."), false);
                return 0;
            }

            for (String coin : allBalances.keySet()) {
                int amount = allBalances.get(coin);
                if (amount > 0) {
                    BankingData.withdrawBalance(sender, coin, amount);
                    BankingData.addBalance(target, coin, amount);
                }
            }

            sender.sendMessage(Text.literal("Transferred ALL coins to " + target.getName().getString()), false);
            target.sendMessage(Text.literal("You received ALL coins from " + sender.getName().getString()), false);
            return 1;
        }

        // Transfer specific coin
        Item coinItem = getCoinByName(coinArg);
        if (coinItem == null) {
            sender.sendMessage(Text.literal("Unknown coin type: " + coinArg), false);
            return 0;
        }

        int amount;
        if (amountArg.equals("all")) {
            amount = BankingData.getBalance(sender, coinArg);
        } else {
            try {
                amount = Integer.parseInt(amountArg);
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.literal("Invalid amount: " + amountArg), false);
                return 0;
            }
        }

        if (amount <= 0 || !BankingData.withdrawBalance(sender, coinArg, amount)) {
            sender.sendMessage(Text.literal("Not enough " + coinArg + " to transfer."), false);
            return 0;
        }

        BankingData.addBalance(target, coinArg, amount);
        sender.sendMessage(Text.literal("Transferred " + amount + " " + coinArg + " to " + target.getName().getString()), false);
        target.sendMessage(Text.literal("You received " + amount + " " + coinArg + " from " + sender.getName().getString()), false);
        return 1;
    }

    // --- Suggestions ---
    private static CompletableFuture<Suggestions> suggestPlayers(CommandContext<?> context, SuggestionsBuilder builder) {
        ServerPlayerEntity sender = ((net.minecraft.server.command.ServerCommandSource) context.getSource()).getPlayer();
        assert sender != null;
        MinecraftServer server = sender.getServer();
        if (server == null) return Suggestions.empty();
        return CommandSource.suggestMatching(
                server.getPlayerManager().getPlayerList().stream().map(p -> p.getName().getString()),
                builder
        );
    }

    private static CompletableFuture<Suggestions> suggestCoins(CommandContext<?> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(COIN_NAMES, builder);
    }

    private static CompletableFuture<Suggestions> suggestAmounts(CommandContext<?> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(List.of("all"), builder);
    }



}
