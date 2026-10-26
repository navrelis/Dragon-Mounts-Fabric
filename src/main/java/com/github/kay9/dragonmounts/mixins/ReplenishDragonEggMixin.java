package com.github.kay9.dragonmounts.mixins;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderDragon.class)
public class ReplenishDragonEggMixin
{
    /**
     * Purpose: To implement a way to replenish dragon eggs after death of the ender dragon
     * <br>
     * Fabric port: the original runs first, so vanilla (and mods like YUNG's Better End Island, which cancel it and place
     * their own egg) do their thing. Afterward, if there is no dragon egg on the exit portal, we place one.
     */
    @WrapOperation(method = "tickDeath()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;setDragonKilled(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;)V"))
    private void dragonmounts_replenishDragonEgg(EndDragonFight fight, EnderDragon dragon, Operation<Void> original)
    {
        original.call(fight, dragon);

        if (!DMLConfig.replenishEggs() || !(dragon.level() instanceof ServerLevel level)) return;

        BlockPos portal = fight.portalLocation;
        if (portal == null) return;

        // the same position that vanilla places the egg at.
        BlockPos eggPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.getLocation(fight.origin));

        if (dragonmounts_hasEggInColumn(level, portal, portal) || dragonmounts_hasEggInColumn(level, portal, eggPos)) return;

        level.setBlockAndUpdate(eggPos, Blocks.DRAGON_EGG.defaultBlockState());
    }

    private static boolean dragonmounts_hasEggInColumn(ServerLevel level, BlockPos portal, BlockPos column)
    {
        int top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column).getY() + 4;
        var cursor = new BlockPos.MutableBlockPos(column.getX(), portal.getY(), column.getZ());
        for (int y = portal.getY(); y <= top; y++)
        {
            cursor.setY(y);
            var state = level.getBlockState(cursor);
            if (state.is(Blocks.DRAGON_EGG) || state.is(DMLRegistry.EGG_BLOCK.get())) return true;
        }
        return false;
    }
}
