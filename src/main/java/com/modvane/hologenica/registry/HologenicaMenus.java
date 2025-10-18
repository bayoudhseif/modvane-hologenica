package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.menu.HolographicMapMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Registry for all menu types (GUIs)
public class HologenicaMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, HologenicaMod.MODID);

    // Holographic map menu type
    public static final DeferredHolder<MenuType<?>, MenuType<HolographicMapMenu>> HOLOGRAPHIC_MAP =
        MENUS.register("holographic_map", () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> 
            new HolographicMapMenu(containerId, inventory, null)));

    public static void init(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
