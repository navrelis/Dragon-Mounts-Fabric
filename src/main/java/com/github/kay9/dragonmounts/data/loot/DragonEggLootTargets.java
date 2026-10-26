package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.dragon.DragonBreed;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static com.github.kay9.dragonmounts.dragon.DragonBreed.BuiltIn.*;

/**
 * Fabric port: replaces the NeoForge {@code DragonEggLootMod.BUILT_IN_CHANCES}.
 * Holds the built-in chest loot tables that may contain a dragon egg, along with the default chance.
 */
public final class DragonEggLootTargets
{
    public record Target(ResourceKey<DragonBreed> breed, ResourceLocation table, double defaultChance)
    {
        /**
         * The config key for this target: {@code <breed path>_in_<last path segment of table>_chance}
         */
        public String configKey()
        {
            String tablePath = table.getPath();
            return String.format("%s_in_%s_chance", breed.location().getPath(), tablePath.substring(tablePath.lastIndexOf('/') + 1));
        }
    }

    public static final Target[] BUILT_IN = new Target[]{
            new Target(AETHER, ResourceLocation.withDefaultNamespace("chests/simple_dungeon"), 0.15),
            new Target(FIRE, ResourceLocation.withDefaultNamespace("chests/desert_pyramid"), 0.075),
            new Target(FOREST, ResourceLocation.withDefaultNamespace("chests/jungle_temple"), 0.3),
            new Target(GHOST, ResourceLocation.withDefaultNamespace("chests/woodland_mansion"), 0.2),
            new Target(GHOST, ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"), 0.095),
            new Target(ICE, ResourceLocation.withDefaultNamespace("chests/igloo_chest"), 0.2),
            new Target(NETHER, ResourceLocation.withDefaultNamespace("chests/bastion_treasure"), 0.35),
            new Target(WATER, ResourceLocation.withDefaultNamespace("chests/buried_treasure"), 0.175)
    };

    private DragonEggLootTargets() {}
}
