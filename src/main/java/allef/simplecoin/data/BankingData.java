package allef.simplecoin.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankingData {

    private static final Map<UUID, Map<String, Integer>> balances = new HashMap<>();
    private static final Gson gson = new Gson();
    private static File file;

    public static void init(MinecraftServer server) {
        // Save file in server world directory
        file = new File(server.getSavePath(WorldSavePath.PLAYERDATA).toFile(), "simplecoin_bank.json");
        load();
    }

    // Add balance
    public static void addBalance(ServerPlayerEntity player, String coin, int amount) {
        balances.computeIfAbsent(player.getUuid(), k -> new HashMap<>());
        Map<String, Integer> playerBalance = balances.get(player.getUuid());
        playerBalance.put(coin, playerBalance.getOrDefault(coin, 0) + amount);
        save();
    }

    // Withdraw balance
    public static boolean withdrawBalance(ServerPlayerEntity player, String coin, int amount) {
        Map<String, Integer> playerBalance = balances.get(player.getUuid());
        if (playerBalance == null || playerBalance.getOrDefault(coin, 0) < amount) return false;
        playerBalance.put(coin, playerBalance.get(coin) - amount);
        save();
        return true;
    }

    // Get balance
    public static int getBalance(ServerPlayerEntity player, String coin) {
        return balances.getOrDefault(player.getUuid(), new HashMap<>()).getOrDefault(coin, 0);
    }

    // Get all balances
    public static Map<String, Integer> getAllBalances(ServerPlayerEntity player) {
        return new HashMap<>(balances.getOrDefault(player.getUuid(), new HashMap<>()));
    }

    // Withdraw all coins
    public static Map<String, Integer> withdrawAll(ServerPlayerEntity player) {
        Map<String, Integer> all = balances.getOrDefault(player.getUuid(), new HashMap<>());
        Map<String, Integer> withdrawn = new HashMap<>(all);
        all.clear();
        save();
        return withdrawn;
    }

    // --- Persistence ---
    private static void save() {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(balances, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<UUID, Map<String, Integer>>>() {}.getType();
            Map<UUID, Map<String, Integer>> loaded = gson.fromJson(reader, type);
            if (loaded != null) balances.putAll(loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
