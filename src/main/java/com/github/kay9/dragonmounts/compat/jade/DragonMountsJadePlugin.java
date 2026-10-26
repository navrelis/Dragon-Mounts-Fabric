package com.github.kay9.dragonmounts.compat.jade;

import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade integration. Only loaded by Fabric Loader through the {@code jade} entrypoint, so Jade being absent is safe.
 * All providers are client side: the egg's breed/hatch stage live in the synced block entity/state and the
 * dragon's breed/age/saddle live in synced entity data.
 */
@WailaPlugin
public class DragonMountsJadePlugin implements IWailaPlugin
{
    @Override
    public void registerClient(IWailaClientRegistration registration)
    {
        registration.addConfig(DragonProvider.SHOW_SADDLE, true);

        registration.registerBlockComponent(DragonEggProvider.INSTANCE, HatchableEggBlock.class);
        registration.registerEntityComponent(DragonProvider.INSTANCE, TameableDragon.class);
    }
}
