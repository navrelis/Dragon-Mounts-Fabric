package com.github.kay9.dragonmounts.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.Nullable;

/**
 * The only class on the common-side access path that references the client. Must only be loaded on the client.
 */
@Environment(EnvType.CLIENT)
public final class ClientAccess
{
    private ClientAccess() {}

    @Nullable
    public static RegistryAccess registryAccess()
    {
        var level = Minecraft.getInstance().level;
        return level == null ? null : level.registryAccess();
    }
}
