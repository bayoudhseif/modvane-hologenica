package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.menu.DNACentrifugeMenu;
import com.modvane.hologenica.menu.HologramProjectorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Registry for all menu types (GUIs)
public class HologenicaMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, HologenicaMod.MODID);

    // Hologram projector menu type
    public static final DeferredHolder<MenuType<?>, MenuType<HologramProjectorMenu>> HOLOGRAM_PROJECTOR =
        MENUS.register("hologram_projector", () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> 
            new HologramProjectorMenu(containerId, inventory, null)));

    // DNA centrifuge menu type
    public static final DeferredHolder<MenuType<?>, MenuType<DNACentrifugeMenu>> DNA_CENTRIFUGE =
        MENUS.register("dna_centrifuge", () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> 
            new DNACentrifugeMenu(containerId, inventory, new SimpleContainer(1))));

    public static void init(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
