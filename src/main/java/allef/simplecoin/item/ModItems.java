package allef.simplecoin.item;

import allef.simplecoin.SimpleCoin;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
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

    public static final RegistryKey<ItemGroup> SIMPLE_COIN = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(SimpleCoin.MOD_ID, "simple_coin"));

    public static final ItemGroup CUSTOM_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.GOLD_COIN))
            .displayName(Text.translatable("itemGroup.simple_coin"))
            .build();

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(SimpleCoin.MOD_ID, name));

        Item item = itemFactory.apply(settings.registryKey(itemKey));

        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, SIMPLE_COIN, CUSTOM_ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(SIMPLE_COIN).register(itemGroup -> {
            itemGroup.add(ModItems.GOLD_COIN);
            itemGroup.add(ModItems.COPPER_COIN);
            itemGroup.add(ModItems.DIAMOND_COIN);
            itemGroup.add(ModItems.EMERALD_COIN);
            itemGroup.add(ModItems.IRON_COIN);
            itemGroup.add(ModItems.NETHERITE_COIN);
            itemGroup.add(ModItems.CONTINENTAL_COIN);
            itemGroup.add(ModItems.GALACTIC_CREDIT);

        });
    }
}
