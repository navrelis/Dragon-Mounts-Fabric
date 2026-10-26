package com.github.kay9.dragonmounts.fabric;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.data.CrossBreedingManager;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.neoforged.fml.config.ModConfig;

/**
 * Common (both sides) Fabric entrypoint. Replaces the NeoForge {@code NeoModImpl} dispatcher.
 */
public class DragonMountsFabric implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        // 1. custom registries must exist before DMLRegistry's static initializer registers entries into them
        DMLCustomRegistries.init();

        // 2. content
        DMLRegistry.init();

        // 3. entity data serializer (ids are assigned in order of registration)
        EntityDataSerializers.registerSerializer(TameableDragon.DRAGON_BREED_SERIALIZER);

        // 4. datapack registry for breeds, synced to clients using the network codec
        DynamicRegistries.registerSynced(DragonBreed.REGISTRY_KEY, DragonBreed.DIRECT_CODEC, DragonBreed.NETWORK_CODEC);

        // 5. default attributes
        FabricDefaultAttributeRegistry.register(DMLRegistry.DRAGON.get(), TameableDragon.createAttributes());

        // 6. config
        NeoForgeConfigRegistry.INSTANCE.register(DragonMountsLegacy.MOD_ID, ModConfig.Type.COMMON, DMLConfig.COMMON_SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(DragonMountsLegacy.MOD_ID, ModConfig.Type.CLIENT, DMLConfig.CLIENT_SPEC);

        // 7. events
        registerEvents();
        registerLoot();
    }

    private void registerEvents()
    {
        // Replaces NeoForge's PlayerInteractEvent.RightClickBlock handler
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
        {
            if (player.isSpectator()) return InteractionResult.PASS;
            if (DragonMountsLegacy.overrideVanillaDragonEgg(level, hit.getBlockPos(), player))
                return InteractionResult.sidedSuccess(level.isClientSide);
            return InteractionResult.PASS;
        });

        // Replaces NeoForge's AddReloadListenerEvent
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(CrossBreedingManager.INSTANCE);

        // Replaces NeoForge's ServerLifecycleHooks.getCurrentServer()
        ServerLifecycleEvents.SERVER_STARTING.register(ServerHolder::set);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerHolder.set(null));

        // Replaces HatchableEggBlock#onDestroyedByPlayer: the end egg must not be destroyed by punching/mining
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
                !HatchableEggBlock.shouldPreventDestruction(state, level, pos, player));
    }

    private void registerLoot()
    {
        // T5: register the LootTableEvents.MODIFY callback for DragonEggLootTargets.BUILT_IN here.
    }
}
