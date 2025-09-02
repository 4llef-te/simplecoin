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
    public static final Item DIAMOND_COIN = register("diamond_coin", Item::new, new Item.Settings());
    public static final Item EMERALD_COIN = register("emerald_coin", Item::new, new Item.Settings());
    public static final Item IRON_COIN = register("iron_coin", Item::new, new Item.Settings());
    public static final Item NETHERITE_COIN = register("netherite_coin", Item::new, new Item.Settings());
    public static final Item CONTINENTAL_COIN = register("continental_coin", Item::new, new Item.Settings());
    public static final Item GALACTIC_CREDIT = register("galactic_credit", Item::new, new Item.Settings());

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(SimpleCoin.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void registerModItems() {



    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.GOLD_COIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.COPPER_COIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.DIAMOND_COIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.EMERALD_COIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.IRON_COIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.NETHERITE_COIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.CONTINENTAL_COIN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ModItems.GALACTIC_CREDIT));
    }
}
