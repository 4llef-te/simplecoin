package allef.simplecoin.commands;

import allef.simplecoin.item.ModItems;
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
import java.util.concurrent.CompletableFuture;

public class DepositCommand {
    private static final List<String> COIN_NAMES = Arrays.asList(
            "gold", "copper", "diamond", "emerald",
            "iron", "netherite", "continental", "credit", "all"
    );

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("deposit")
                .then(CommandManager.argument("coin", StringArgumentType.word())
                        .suggests(DepositCommand::suggestCoins)
                        .then(CommandManager.argument("amount", StringArgumentType.word())
                                .suggests(DepositCommand::suggestAmounts)
                                .executes(DepositCommand::executeDeposit)))
                .executes(DepositCommand::executeDepositAll)));
    }

    private static int executeDeposit(CommandContext<?> ctx) {
        ServerPlayerEntity player = ((net.minecraft.server.command.ServerCommandSource) ctx.getSource()).getPlayer();
        String coinArg = StringArgumentType.getString(ctx, "coin").toLowerCase();
        String amountArg = StringArgumentType.getString(ctx, "amount").toLowerCase();

        if (coinArg.equals("all")) {
            return depositAllCoins(player);
        }

        Item coinItem = getCoinByName(coinArg);
        if (coinItem == null) {
            assert player != null;
            player.sendMessage(Text.literal("Unknown coin type: " + coinArg), false);
            return 0;
        }

        int amount;
        if (amountArg.equals("all")) {
            assert player != null;
            amount = countCoins(player, coinItem);
        } else {
            try {
                amount = Integer.parseInt(amountArg);
            } catch (NumberFormatException e) {
                assert player != null;
                player.sendMessage(Text.literal("Invalid amount: " + amountArg), false);
                return 0;
            }
        }

        return depositCoin(player, coinItem, coinArg, amount);
    }

    private static int executeDepositAll(CommandContext<?> ctx) {
        ServerPlayerEntity player = ((net.minecraft.server.command.ServerCommandSource) ctx.getSource()).getPlayer();
        return depositAllCoins(player);
    }

    private static int depositCoin(ServerPlayerEntity player, Item coinItem, String coinName, int amount) {
        int removed = removeCoins(player, coinItem, amount);
        if (removed > 0) {
            BankingData.addBalance(player, coinName, removed);
            player.sendMessage(Text.literal(
                    "Deposited " + removed + " " + coinName +
                            ". Balance: " + BankingData.getBalance(player, coinName)
            ), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("You don’t have enough " + coinName + "!"), false);
            return 0;
        }
    }

    private static int depositAllCoins(ServerPlayerEntity player) {
        int totalDeposited = 0;
        for (String name : COIN_NAMES) {
            if (name.equals("all")) continue;
            Item coin = getCoinByName(name);
            if (coin != null) {
                int count = countCoins(player, coin);
                if (count > 0) {
                    removeCoins(player, coin, count);
                    BankingData.addBalance(player, name, count);
                    totalDeposited += count;
                }
            }
        }
        if (totalDeposited > 0) {
            player.sendMessage(Text.literal("Deposited ALL coins. Total: " + totalDeposited), false);
            return 1;
        } else {
            player.sendMessage(Text.literal("You don’t have any coins to deposit."), false);
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggestCoins(CommandContext<?> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(COIN_NAMES, builder);
    }

    private static CompletableFuture<Suggestions> suggestAmounts(CommandContext<?> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(List.of("all"), builder);
    }

    private static int removeCoins(ServerPlayerEntity player, Item coin, int amount) {
        int toRemove = amount;
        int removed = 0;

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == coin) {
                int take = Math.min(stack.getCount(), toRemove);
                stack.decrement(take);
                removed += take;
                toRemove -= take;
                if (toRemove <= 0) break;
            }
        }
        return removed;
    }

    private static int countCoins(ServerPlayerEntity player, Item coin) {
        int total = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == coin) total += stack.getCount();
        }
        return total;
    }

    static Item getCoinByName(String name) {
        return switch (name) {
            case "gold" -> ModItems.GOLD_COIN;
            case "copper" -> ModItems.COPPER_COIN;
            case "diamond" -> ModItems.DIAMOND_COIN;
            case "emerald" -> ModItems.EMERALD_COIN;
            case "iron" -> ModItems.IRON_COIN;
            case "netherite" -> ModItems.NETHERITE_COIN;
            case "continental" -> ModItems.CONTINENTAL_COIN;
            case "credit" -> ModItems.GALACTIC_CREDIT;
            default -> null;
        };
    }
}
