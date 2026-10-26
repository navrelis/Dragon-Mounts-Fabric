package com.github.kay9.dragonmounts.fabric;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Static access to the currently running server (replaces NeoForge's {@code ServerLifecycleHooks}).
 * Set on {@code SERVER_STARTING} and cleared on {@code SERVER_STOPPED}.
 */
public final class ServerHolder
{
    @Nullable
    private static volatile MinecraftServer server;

    private ServerHolder() {}

    @Nullable
    public static MinecraftServer get()
    {
        return server;
    }

    static void set(@Nullable MinecraftServer server)
    {
        ServerHolder.server = server;
    }
}
