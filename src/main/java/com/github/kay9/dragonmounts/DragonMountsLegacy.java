package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dragon Mounts Legacy
 * <br>
 * Main mod information like the ID and logger is found here.
 * Game logic that is shared between loader hooks and mixins is also present here.
 * Everything in the mod is a network with this at the core of it all.
 * <br>
 * Fabric port: load events (registries, reload listeners, attributes, config, ...) are wired up by the
 * Fabric entrypoints ({@code fabric.DragonMountsFabric} for common, {@code fabric.DragonMountsFabricClient} for client).
 * Game events are the way the mod interacts with the game's behavior.
 * Event methods can be triggered by:
 *  - Fabric API event callbacks registered in the entrypoints
 *  - Mixins that inject callbacks to here
 */
public class DragonMountsLegacy
{
    public static final String MOD_ID = "dragonmounts";
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    public static ResourceLocation id(String path)
    {
        return ResourceLocation.tryBuild(MOD_ID, path);
    }

    // ========================
    //       Game Events
    // ========================

    public static boolean overrideVanillaDragonEgg(Level level, BlockPos pos, Player player)
    {
        if (DMLConfig.allowEggOverride() && level.getBlockState(pos).is(Blocks.DRAGON_EGG))
        {
            var end = DragonBreed.registry(level.registryAccess()).getHolder(DragonBreed.BuiltIn.END);
            if (end.isPresent())
            {
                if (level.isClientSide) player.swing(InteractionHand.MAIN_HAND);
                else
                {
                    var state = DMLRegistry.EGG_BLOCK.get().defaultBlockState().setValue(HatchableEggBlock.HATCHING, true);
                    HatchableEggBlock.place((ServerLevel) level, pos, state, end.get());
                }
                return true;
            }
        }
        return false;
    }
}
