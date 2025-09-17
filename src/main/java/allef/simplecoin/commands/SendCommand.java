package allef.simplecoin.commands;

import allef.simplecoin.data.BankingData;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SendCommand {

    private static final List<String> COIN_NAMES = Arrays.asList(
            "gold", "copper", "diamond", "emerald",
            "iron", "netherite", "continental", "credit", "all"
    );

    private static final List<String> AMOUNT_SUGGESTIONS = List.of("all");

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {


            dispatcher.register(
                    CommandManager.literal("send")
                            .requires(source -> true) // Allow everyone
                            .executes(context -> {
                                // Show usage when no arguments provided
                                context.getSource().sendMessage(Text.literal("Usage: /testtransfer <player> <coin> <amount>"));
                                context.getSource().sendMessage(Text.literal("Coins: " + String.join(", ", COIN_NAMES)));
                                return 1;
                            })
                            .then(CommandManager.argument("player", StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        // Suggest online players
                                        ServerCommandSource source = context.getSource();
                                        List<String> playerNames = source.getServer().getPlayerManager().getPlayerList()
                                                .stream()
                                                .map(player -> player.getName().getString())
                                                .collect(Collectors.toList());
                                        return CommandSource.suggestMatching(playerNames, builder);
                                    })
                                    .executes(context -> {
                                        ServerPlayerEntity executor = context.getSource().getPlayer();
                                        String playerName = StringArgumentType.getString(context, "player");

                                        if (executor != null) {
                                            executor.sendMessage(Text.literal("Player argument: " + playerName));
                                            executor.sendMessage(Text.literal("Missing coin and amount arguments"));
                                        }
                                        return 1;
                                    })
                                    .then(CommandManager.argument("coin", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                // Suggest coin types
                                                return CommandSource.suggestMatching(COIN_NAMES, builder);
                                            })
                                            .executes(context -> {
                                                ServerPlayerEntity executor = context.getSource().getPlayer();
                                                String playerName = StringArgumentType.getString(context, "player");
                                                String coin = StringArgumentType.getString(context, "coin");

                                                if (executor != null) {
                                                    executor.sendMessage(Text.literal("Player: " + playerName + ", Coin: " + coin));
                                                    executor.sendMessage(Text.literal("Missing amount argument"));
                                                }
                                                return 1;
                                            })
                                            .then(CommandManager.argument("amount", StringArgumentType.word())
                                                    .suggests((context, builder) -> {
                                                        // Suggest amounts including "all"
                                                        ServerPlayerEntity sender = context.getSource().getPlayer();
                                                        if (sender != null) {
                                                            String coinArg = StringArgumentType.getString(context, "coin");
                                                            if (!coinArg.equals("all")) {
                                                                int balance = BankingData.getBalance(sender, coinArg);
                                                                if (balance > 0) {
                                                                    // Suggest "all" plus common amounts and player's balance
                                                                    List<String> suggestions = AMOUNT_SUGGESTIONS.stream()
                                                                            .filter(amt -> !amt.equals("all") || balance > 0)
                                                                            .collect(Collectors.toList());

                                                                    return CommandSource.suggestMatching(suggestions, builder);
                                                                }
                                                            }
                                                        }
                                                        return CommandSource.suggestMatching(AMOUNT_SUGGESTIONS, builder);
                                                    })
                                                    .executes(SendCommand::executeTestTransfer)
                                            )
                                    )
                            )
            );


        });
    }

    private static int executeTestTransfer(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity sender = context.getSource().getPlayer();
        if (sender == null) return 0;

        String targetName = StringArgumentType.getString(context, "player");
        String coin = StringArgumentType.getString(context, "coin").toLowerCase();
        String amountArg = StringArgumentType.getString(context, "amount");

        // Find target player
        ServerPlayerEntity targetPlayer = context.getSource().getServer().getPlayerManager().getPlayer(targetName);
        if (targetPlayer == null) {
            sender.sendMessage(Text.literal("Player not found: " + targetName));
            return 0;
        }

        // Check if sender is trying to transfer to themselves
        if (sender.getUuid().equals(targetPlayer.getUuid())) {
            sender.sendMessage(Text.literal("You cannot transfer to yourself!"));
            return 0;
        }

        // Handle "all" for coin type
        if (coin.equals("all")) {
            // Transfer all coins
            Map<String, Integer> allBalances = BankingData.getAllBalances(sender);
            if (allBalances.isEmpty()) {
                sender.sendMessage(Text.literal("You don't have any coins to transfer."));
                return 0;
            }

            int totalTransferred = 0;
            for (Map.Entry<String, Integer> entry : allBalances.entrySet()) {
                String coinType = entry.getKey();
                int amount = entry.getValue();
                if (amount > 0) {
                    BankingData.withdrawBalance(sender, coinType, amount);
                    BankingData.addBalance(targetPlayer, coinType, amount);
                    totalTransferred += amount;


                }
            }

            sender.sendMessage(Text.literal("§aTransferred §6ALL coins (§e" + totalTransferred + " total§6) §ato §e" + targetPlayer.getName().getString()));
            targetPlayer.sendMessage(Text.literal("§aReceived §6ALL coins (§e" + totalTransferred + " total§6) §afrom §e" + sender.getName().getString()));
            return 1;
        }

        // Parse amount for specific coin
        int amount;
        if (amountArg.equalsIgnoreCase("all")) {
            amount = BankingData.getBalance(sender, coin);
            if (amount <= 0) {
                sender.sendMessage(Text.literal("You don't have any " + coin + " to transfer."));
                return 0;
            }
        } else {
            try {
                amount = Integer.parseInt(amountArg);
                if (amount <= 0) {
                    sender.sendMessage(Text.literal("Amount must be positive."));
                    return 0;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.literal("Invalid amount: " + amountArg));
                return 0;
            }
        }

        // Check if sender has enough balance
        int senderBalance = BankingData.getBalance(sender, coin);
        if (senderBalance < amount) {
            sender.sendMessage(Text.literal("Not enough " + coin + ". You have: " + senderBalance));
            return 0;
        }

        // Perform the transfer
        boolean withdrawSuccess = BankingData.withdrawBalance(sender, coin, amount);
        if (!withdrawSuccess) {
            sender.sendMessage(Text.literal("Failed to withdraw " + coin));
            return 0;
        }

        BankingData.addBalance(targetPlayer, coin, amount);

        // Send success messages
        sender.sendMessage(Text.literal("§aTransferred §6" + amount + " " + coin + " §ato §e" + targetPlayer.getName().getString()));
        targetPlayer.sendMessage(Text.literal("§aReceived §6" + amount + " " + coin + " §afrom §e" + sender.getName().getString()));

        // Log the transaction


        return 1;
    }
}