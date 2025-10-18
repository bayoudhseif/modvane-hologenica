package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.menu.HologramPodMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Registry for all menu types (GUIs)
public class HologenicaMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, HologenicaMod.MODID);

    // Hologram pod menu type
    public static final DeferredHolder<MenuType<?>, MenuType<HologramPodMenu>> HOLOGRAM_POD =
        MENUS.register("hologram_pod", () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> 
            new HologramPodMenu(containerId, inventory, null)));

    public static void init(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
