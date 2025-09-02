package allef.simplecoin.item;

import allef.simplecoin.SimpleCoin;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {


    public static final Item GOLD_COIN = register("gold_coin", Item::new, new Item.Settings());
    public static final Item COPPER_COIN = register("copper_coin", Item::new, new Item.Settings());

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(SimpleCoin.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void registerModItems() {

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.GOLD_COIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.COPPER_COIN));;

    }

    public static void initialize() {
    }
}
