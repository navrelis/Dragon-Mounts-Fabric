package com.github.kay9.dragonmounts.fabric;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.client.DragonEggModel;
import com.github.kay9.dragonmounts.client.DragonModel;
import com.github.kay9.dragonmounts.client.DragonRenderer;
import com.github.kay9.dragonmounts.client.KeyMappings;
import com.github.kay9.dragonmounts.client.MountControlsMessenger;
import com.github.kay9.dragonmounts.data.model.DragonModelPropertiesListener;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTabs;

/**
 * Client-side entry point. Collects what NeoForge did through the client-only mod bus events
 * (renderers, layer definitions, item colors, key mappings, creative tab contents, reload listeners).
 */
public class DragonMountsFabricClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        EntityRendererRegistry.register(DMLRegistry.DRAGON.get(), DragonRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(DragonRenderer.MODEL_LOCATION, () -> DragonModel.createBodyLayer(DragonModel.Properties.STANDARD));

        ColorProviderRegistry.ITEM.register(DragonSpawnEgg::getColor, DMLRegistry.SPAWN_EGG.get());

        KeyMappings.registerKeybinds(KeyBindingHelper::registerKeyBinding);

        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            MountControlsMessenger.tick();
            KeyMappings.handleCameraControlsKey();
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> DragonSpawnEgg.populateTab(entries.getContext().holders(), entries::accept));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> HatchableEggBlock.populateTab(entries.getContext().holders(), entries::accept));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(DragonModelPropertiesListener.INSTANCE);

        DragonEggModel.init();
    }
}
