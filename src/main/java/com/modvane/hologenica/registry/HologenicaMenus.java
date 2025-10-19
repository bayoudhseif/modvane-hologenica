package com.modvane.hologenica.registry;

import com.modvane.hologenica.HologenicaMod;
import com.modvane.hologenica.menu.CloningPodMenu;
import com.modvane.hologenica.menu.HologramProjectorMenu;
import com.modvane.hologenica.menu.SteveNPCMenu;
import com.modvane.hologenica.menu.TelepadMenu;
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

    // Cloning Pod menu type
    public static final DeferredHolder<MenuType<?>, MenuType<CloningPodMenu>> CLONING_POD =
        MENUS.register("cloning_pod", () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> 
            new CloningPodMenu(containerId, inventory, new SimpleContainer(1))));

    // Steve NPC menu type
    public static final DeferredHolder<MenuType<?>, MenuType<SteveNPCMenu>> STEVE_NPC =
        MENUS.register("steve_npc", () -> IMenuTypeExtension.create((containerId, inventory, buffer) ->
            new SteveNPCMenu(containerId, inventory, null)));

    // Telepad menu type
    public static final DeferredHolder<MenuType<?>, MenuType<TelepadMenu>> TELEPAD =
        MENUS.register("telepad", () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> {
            net.minecraft.core.BlockPos pos = buffer.readBlockPos();
            return new TelepadMenu(containerId, inventory, pos);
        }));

    public static void init(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
