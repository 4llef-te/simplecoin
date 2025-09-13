package allef.simplecoin.data;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankingData {
    // Player UUID -> (coin type -> amount)
    private static final Map<UUID, Map<String, Integer>> balances = new HashMap<>();

    // Get balance for a specific coin
    public static int getBalance(ServerPlayerEntity player, String coin) {
        return balances
                .getOrDefault(player.getUuid(), new HashMap<>())
                .getOrDefault(coin, 0);
    }

    // Add coins to a specific coin type
    public static void addBalance(ServerPlayerEntity player, String coin, int amount) {
        Map<String, Integer> playerBalances = balances.computeIfAbsent(player.getUuid(), k -> new HashMap<>());
        playerBalances.put(coin, playerBalances.getOrDefault(coin, 0) + amount);
    }

    // Withdraw coins of a specific type
    public static boolean withdrawBalance(ServerPlayerEntity player, String coin, int amount) {
        Map<String, Integer> playerBalances = balances.get(player.getUuid());
        if (playerBalances == null) return false;

        int current = playerBalances.getOrDefault(coin, 0);
        if (current >= amount) {
            playerBalances.put(coin, current - amount);
            return true;
        }
        return false;
    }

    // Withdraw all coins and return map of coin type -> amount
    public static Map<String, Integer> withdrawAll(ServerPlayerEntity player) {
        Map<String, Integer> playerBalances = balances.get(player.getUuid());
        if (playerBalances == null) return new HashMap<>();

        Map<String, Integer> withdrawn = new HashMap<>(playerBalances);
        // Clear all balances
        playerBalances.clear();
        return withdrawn;
    }

    // Get all balances for display
    public static Map<String, Integer> getAllBalances(ServerPlayerEntity player) {
        return balances.getOrDefault(player.getUuid(), new HashMap<>());
    }
}
