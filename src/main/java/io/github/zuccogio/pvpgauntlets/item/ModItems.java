package io.github.zuccogio.pvpgauntlets.item;

import io.github.zuccogio.pvpgauntlets.PvPGauntlets;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {
    public static Item register(Item item, String id) {
        // Create the identifier for the item.
        ResourceLocation itemID = ResourceLocation.fromNamespaceAndPath(PvPGauntlets.MOD_ID, id);

        // Register the item.
        Item registeredItem = Registry.register(BuiltInRegistries.ITEM, itemID, item);

        // Return the registered item!
        return registeredItem;
    }

    public static void initialize() {
        // Get the event for modifying entries in the ingredients group.
        // And register an event handler that adds our suspicious item to the ingredients group.
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
            .register((itemGroup) -> itemGroup.accept(ModItems.GAUNTLET));
    }

    public static final Item GAUNTLET = register(
            new Item(new Item.Properties()),
            "gauntlet"
    );
}
