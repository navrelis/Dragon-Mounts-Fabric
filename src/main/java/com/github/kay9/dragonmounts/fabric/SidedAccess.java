package com.github.kay9.dragonmounts.fabric;

import com.github.kay9.dragonmounts.fabric.client.ClientAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Side-safe access to a {@link RegistryAccess} from common code (upstream #259: don't touch a null server or
 * the client class on a dedicated server).
 */
public final class SidedAccess
{
    private SidedAccess() {}

    /**
     * @return the registry access of the running server if there is one, otherwise (client only) the registry
     * access of the current client level. {@code null} if neither is available.
     */
    @Nullable
    public static RegistryAccess registryAccess()
    {
        MinecraftServer server = ServerHolder.get();
        if (server != null) return server.registryAccess();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            return ClientAccess.registryAccess();

        return null;
    }
}
