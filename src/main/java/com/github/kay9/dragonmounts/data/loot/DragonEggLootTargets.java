package com.github.kay9.dragonmounts.data.loot;

import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static com.github.kay9.dragonmounts.dragon.DragonBreed.BuiltIn.*;

/**
 * Fabric port: replaces the NeoForge {@code DragonEggLootMod.BUILT_IN_CHANCES}.
 * Holds the built-in chest loot tables that may contain a dragon egg, along with the default chance.
 * <br>
 * Each target has a base table (which is what the config key is derived from, same as upstream) and optionally a list
 * of extra tables that share the same chance. These are mostly the replacements of mods that overhaul vanilla structures.
 * Tables of mods that are not installed simply never match.
 */
public final class DragonEggLootTargets
{
    public record Target(ResourceKey<DragonBreed> breed, ResourceLocation table, double defaultChance, List<ResourceLocation> extraTables)
    {
        /**
         * The config key for this target: {@code <breed path>_in_<last path segment of base table>_chance}
         */
        public String configKey()
        {
            String tablePath = table.getPath();
            return String.format("%s_in_%s_chance", breed.location().getPath(), tablePath.substring(tablePath.lastIndexOf('/') + 1));
        }

        /**
         * @return true if the given loot table id is the base table or one of the extra tables of this target
         */
        public boolean matches(ResourceLocation tableId)
        {
            return table.equals(tableId) || extraTables.contains(tableId);
        }
    }

    public static final Target[] BUILT_IN = new Target[]{
            new Target(AETHER, vanilla("chests/simple_dungeon"), 0.15, ids(
                    "betterdungeons:skeleton_dungeon/chests/common",
                    "betterdungeons:zombie_dungeon/chests/common",
                    "betterdungeons:small_dungeon/chests/loot_piles",
                    "repurposed_structures:chests/dungeons/badlands",
                    "repurposed_structures:chests/dungeons/dark_forest",
                    "repurposed_structures:chests/dungeons/deep",
                    "repurposed_structures:chests/dungeons/desert",
                    "repurposed_structures:chests/dungeons/jungle",
                    "repurposed_structures:chests/dungeons/mushroom",
                    "repurposed_structures:chests/dungeons/ocean",
                    "repurposed_structures:chests/dungeons/swamp")),
            new Target(FIRE, vanilla("chests/desert_pyramid"), 0.075, ids(
                    "betterdeserttemples:chests/tomb_pharaoh",
                    "betterdeserttemples:chests/pharaoh_hidden",
                    "betterdeserttemples:chests/tomb")),
            new Target(FOREST, vanilla("chests/jungle_temple"), 0.3, ids(
                    "betterjungletemples:chests/treasure",
                    "repurposed_structures:chests/pyramids/jungle")),
            new Target(GHOST, vanilla("chests/woodland_mansion"), 0.2, ids(
                    "minecraft:chests/illager_mansion/ancient_city_raid_chest",
                    "minecraft:chests/illager_mansion/evoker_chest",
                    "minecraft:chests/illager_mansion/generic",
                    "minecraft:chests/illager_mansion/library_chest",
                    "minecraft:chests/illager_mansion/map_chest",
                    "minecraft:chests/illager_mansion/pillager_chest",
                    "minecraft:chests/illager_mansion/ravager_chest",
                    "minecraft:chests/illager_mansion/secret_room",
                    "minecraft:chests/illager_mansion/smithing_room",
                    "minecraft:chests/illager_mansion/vindicator_chest",
                    "minecraft:chests/illager_mansion/witch_chest",
                    "repurposed_structures:chests/mansions/birch",
                    "repurposed_structures:chests/mansions/desert",
                    "repurposed_structures:chests/mansions/jungle",
                    "repurposed_structures:chests/mansions/mangrove",
                    "repurposed_structures:chests/mansions/oak",
                    "repurposed_structures:chests/mansions/savanna",
                    "repurposed_structures:chests/mansions/snowy",
                    "repurposed_structures:chests/mansions/taiga")),
            new Target(GHOST, vanilla("chests/abandoned_mineshaft"), 0.095, ids()),
            new Target(ICE, vanilla("chests/igloo_chest"), 0.2, ids(
                    "repurposed_structures:chests/igloos/grassy",
                    "repurposed_structures:chests/igloos/mangrove",
                    "repurposed_structures:chests/igloos/mushroom",
                    "repurposed_structures:chests/igloos/stone")),
            new Target(NETHER, vanilla("chests/bastion_treasure"), 0.35, ids(
                    "repurposed_structures:chests/bastions/underground/treasure",
                    "mns:chests/dragon_arena/epic")),
            new Target(WATER, vanilla("chests/buried_treasure"), 0.175, ids(
                    "beachparty:chests/buried_treasure",
                    "betteroceanmonuments:chests/upper_side_chamber"))
    };

    private static ResourceLocation vanilla(String path)
    {
        return ResourceLocation.withDefaultNamespace(path);
    }

    private static List<ResourceLocation> ids(String... ids)
    {
        var builder = ImmutableList.<ResourceLocation>builder();
        for (String id : ids) builder.add(ResourceLocation.parse(id));
        return builder.build();
    }

    private DragonEggLootTargets() {}
}
