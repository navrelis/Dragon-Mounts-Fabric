package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.data.loot.conditions.RandomChanceByConfig;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.abilities.*;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlockEntity;
import com.github.kay9.dragonmounts.dragon.egg.habitats.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.function.Supplier;


/**
 * Fabric port: registration happens immediately (class initialization of this class performs it) instead of via
 * NeoForge's DeferredRegister. Field order matters, since later entries depend on earlier ones.
 * The custom registries (ability + habitat types) must exist before this class is initialized,
 * see {@link com.github.kay9.dragonmounts.fabric.DMLCustomRegistries}.
 */
public class DMLRegistry
{
    /**
     * Forces class initialization, which registers everything.
     */
    public static void init()
    {
        DragonMountsLegacy.LOG.debug("Registered Dragon Mounts content");
    }

    public static final Supplier<SoundEvent> DRAGON_AMBIENT_SOUND = sound("entity.dragon.ambient");
    public static final Supplier<SoundEvent> DRAGON_STEP_SOUND = sound("entity.dragon.step");
    public static final Supplier<SoundEvent> DRAGON_DEATH_SOUND = sound("entity.dragon.death");
    public static final Supplier<SoundEvent> GHOST_DRAGON_AMBIENT = sound("entity.dragon.ambient.ghost");

    public static final Supplier<Block> EGG_BLOCK = register("dragon_egg", Registries.BLOCK, HatchableEggBlock::new);

    public static final Supplier<Item> EGG_BLOCK_ITEM = register("dragon_egg", Registries.ITEM, HatchableEggBlock.Item::new);

    public static final Supplier<EntityType<TameableDragon>> DRAGON = register("dragon", Registries.ENTITY_TYPE, () -> EntityType.Builder.of(TameableDragon::new, MobCategory.CREATURE).sized(TameableDragon.BASE_WIDTH, TameableDragon.BASE_HEIGHT).eyeHeight(3.375f).clientTrackingRange(10).updateInterval(3).build(DragonMountsLegacy.MOD_ID + ":dragon"));

    // needs the entity type to be registered first
    public static final Supplier<Item> SPAWN_EGG = register("spawn_egg", Registries.ITEM, DragonSpawnEgg::new);

    public static final Supplier<BlockEntityType<HatchableEggBlockEntity>> EGG_BLOCK_ENTITY = register("dragon_egg", Registries.BLOCK_ENTITY_TYPE, () -> BlockEntityType.Builder.of(HatchableEggBlockEntity::new, EGG_BLOCK.get()).build(null));

    public static final Supplier<LootItemConditionType> RANDOM_CHANCE_CONFIG_CONDITION = register("random_chance_by_config", Registries.LOOT_CONDITION_TYPE, () -> new LootItemConditionType(RandomChanceByConfig.CODEC));

    public static final Supplier<DataComponentType<Holder<DragonBreed>>> DRAGON_BREED_COMPONENT = register("dragon_breed", Registries.DATA_COMPONENT_TYPE, () -> DataComponentType.<Holder<DragonBreed>>builder().persistent(DragonBreed.CODEC).networkSynchronized(DragonBreed.STREAM_CODEC).build());

    // Dragon ability types
    // unnecessary to store these
    static
    {
        register("frost_walker", Ability.REGISTRY_KEY, () -> FrostWalkerAbility.CODEC);
        register("green_toes", Ability.REGISTRY_KEY, () -> GreenToesAbility.CODEC);
        register("snow_stepper", Ability.REGISTRY_KEY, () -> SnowStepperAbility.CODEC);
        register("hot_feet", Ability.REGISTRY_KEY, () -> HotFeetAbility.CODEC);
        register("reaper_step", Ability.REGISTRY_KEY, () -> ReaperStepAbility.CODEC);
        register("hydro_step", Ability.REGISTRY_KEY, () -> HydroStepAbility.CODEC);
    }

    // Dragon egg habitat types
    // unnecessary to store these
    static
    {
        register("picky", Habitat.REGISTRY_KEY, () -> PickyHabitat.CODEC);
        register("biome", Habitat.REGISTRY_KEY, () -> BiomeHabitat.CODEC);
        register("in_fluid", Habitat.REGISTRY_KEY, () -> FluidHabitat.CODEC);
        register("world_height", Habitat.REGISTRY_KEY, () -> HeightHabitat.CODEC);
        register("light", Habitat.REGISTRY_KEY, () -> LightHabitat.CODEC);
        register("nearby_blocks", Habitat.REGISTRY_KEY, () -> NearbyBlocksHabitat.CODEC);
        register("dragon_breath", Habitat.REGISTRY_KEY, () -> DragonBreathHabitat.CODEC);

    }

    private static Supplier<SoundEvent> sound(String name)
    {
        return register(name, Registries.SOUND_EVENT, () -> SoundEvent.createVariableRangeEvent(DragonMountsLegacy.id(name)));
    }

    @SuppressWarnings("unchecked")
    private static <T, I extends T> Supplier<I> register(String name, ResourceKey<Registry<T>> forType, Supplier<I> sup)
    {
        var registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(forType.location());
        if (registry == null)
            throw new IllegalStateException("Registry " + forType.location() + " does not exist (yet). Custom registries must be created first.");
        I obj = Registry.register(registry, DragonMountsLegacy.id(name), sup.get());
        return () -> obj;
    }
}
