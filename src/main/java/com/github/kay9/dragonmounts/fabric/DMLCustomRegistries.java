package com.github.kay9.dragonmounts.fabric;

import com.github.kay9.dragonmounts.dragon.abilities.Ability;
import com.github.kay9.dragonmounts.dragon.egg.habitats.Habitat;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;

/**
 * Creates the mod's static custom registries. Must run before {@code DMLRegistry} is initialized,
 * since its static initializer registers entries into these. This class intentionally never touches DMLRegistry.
 */
public final class DMLCustomRegistries
{
    private DMLCustomRegistries() {}

    public static void init()
    {
        // Ability types are synced to clients (a dragon breed's abilities reference them by id).
        FabricRegistryBuilder.createSimple(Ability.REGISTRY_KEY)
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister();

        FabricRegistryBuilder.createSimple(Habitat.REGISTRY_KEY)
                .buildAndRegister();
    }
}
