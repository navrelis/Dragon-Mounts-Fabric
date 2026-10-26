package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Per-breed dragon egg block/item model (Fabric port of the upstream NeoForge {@code IGeometryLoader} /
 * {@code IDynamicBakedModel}).
 * <p>
 * Every {@code assets/<ns>/models/block/dragon_eggs/<breed>_dragon_egg.json} (all namespaces, so addons contribute) is
 * loaded and baked as a normal vanilla model. The baked models of the egg block's block states and of the egg item
 * (all {@link ModelResourceLocation}s with the egg's registry id) are then wrapped in {@link Baked}, which picks the
 * per-breed model from the block entity's render data (chunk meshing, {@link FabricBakedModel}) or from the stack's
 * breed component (items). The wrapped vanilla-format fallback ({@code dragonmounts:block/dragon_egg}, parent
 * {@code minecraft:block/dragon_egg}) is used whenever no breed is known.
 */
@Environment(EnvType.CLIENT)
public final class DragonEggModel
{
    public static final String EGG_MODELS_DIR = "models/block/dragon_eggs";
    private static final String MODEL_PREFIX = "models/";
    private static final String JSON_SUFFIX = ".json";
    private static final String EGG_SUFFIX = "_dragon_egg";

    private DragonEggModel() {}

    /**
     * Registers the model loading plugin. Called from the client entrypoint.
     */
    public static void init()
    {
        PreparableModelLoadingPlugin.register(DragonEggModel::loadEggModelIds, DragonEggModel::onInitializeModelLoader);
    }

    /**
     * Async prepare step: maps breed id (registered name, e.g. {@code dragonmounts:fire}) to the model id
     * (e.g. {@code dragonmounts:block/dragon_eggs/fire_dragon_egg}).
     */
    private static CompletableFuture<Map<String, ResourceLocation>> loadEggModelIds(ResourceManager resourceManager, Executor executor)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            var ids = ImmutableMap.<String, ResourceLocation>builder();
            for (var file : resourceManager.listResources(EGG_MODELS_DIR, rl -> rl.getPath().endsWith(JSON_SUFFIX)).keySet())
            {
                var path = file.getPath();
                var modelPath = path.substring(MODEL_PREFIX.length(), path.length() - JSON_SUFFIX.length()); // block/dragon_eggs/fire_dragon_egg
                var name = modelPath.substring((EGG_MODELS_DIR.substring(MODEL_PREFIX.length()) + "/").length()); // fire_dragon_egg
                var breedName = name.endsWith(EGG_SUFFIX) ? name.substring(0, name.length() - EGG_SUFFIX.length()) : name;

                var breedId = ResourceLocation.tryBuild(file.getNamespace(), breedName);
                var modelId = ResourceLocation.tryBuild(file.getNamespace(), modelPath);
                if (breedId == null || modelId == null)
                {
                    DragonMountsLegacy.LOG.warn("Ignoring dragon egg model with invalid name: {}", file);
                    continue;
                }

                ids.put(breedId.toString(), modelId);
            }
            return ids.buildKeepingLast();
        }, executor);
    }

    private static void onInitializeModelLoader(Map<String, ResourceLocation> eggModelIds, ModelLoadingPlugin.Context context)
    {
        context.addModels(eggModelIds.values());

        // One instance per model reload; holds the per-breed baked models of this reload only.
        var bakedBreeds = new Object()
        {
            private Map<String, BakedModel> models;

            synchronized Map<String, BakedModel> get(ModelModifier.AfterBake.Context ctx)
            {
                if (models == null)
                {
                    var baked = ImmutableMap.<String, BakedModel>builder();
                    for (var entry : eggModelIds.entrySet())
                    {
                        var model = ctx.baker().bake(entry.getValue(), ctx.settings());
                        if (model != null) baked.put(entry.getKey(), model);
                    }
                    models = baked.buildKeepingLast();
                }
                return models;
            }
        };

        context.modifyModelAfterBake().register((model, ctx) ->
        {
            var topLevelId = ctx.topLevelId();
            if (model == null || topLevelId == null || model instanceof Baked || !isEggModel(topLevelId))
                return model;

            return new Baked(model, bakedBreeds.get(ctx));
        });
    }

    private static boolean isEggModel(ModelResourceLocation id)
    {
        if (ModelResourceLocation.INVENTORY_VARIANT.equals(id.variant()))
            return id.id().equals(BuiltInRegistries.ITEM.getKey(DMLRegistry.EGG_BLOCK_ITEM.get()));
        return id.id().equals(BuiltInRegistries.BLOCK.getKey(DMLRegistry.EGG_BLOCK.get()));
    }

    /**
     * Wraps the vanilla-format fallback egg model and selects the per-breed model at render time.
     */
    public static class Baked extends ForwardingBakedModel implements FabricBakedModel
    {
        private final Map<String, BakedModel> models;
        private final ItemOverrides overrides;

        public Baked(BakedModel fallback, Map<String, BakedModel> models)
        {
            super(fallback);
            this.models = models;
            this.overrides = new ItemModelResolver(this, fallback.getOverrides());
        }

        @Nullable
        public BakedModel getBreedModel(@Nullable Object breedId)
        {
            return breedId instanceof String s ? models.get(s) : null;
        }

        @Override
        public boolean isVanillaAdapter()
        {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context)
        {
            var model = getBreedModel(blockView.getBlockEntityRenderData(pos));
            if (model != null) model.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            else wrapped.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context)
        {
            var breed = stack.get(DMLRegistry.DRAGON_BREED_COMPONENT.get());
            var model = breed == null ? null : getBreedModel(breed.getRegisteredName());
            if (model != null) model.emitItemQuads(stack, randomSupplier, context);
            else wrapped.emitItemQuads(stack, randomSupplier, context);
        }

        // getQuads, getParticleIcon and getTransforms are forwarded to the fallback (vanilla dragon egg look).

        @Override
        public boolean useAmbientOcclusion()
        {
            return true;
        }

        @Override
        public boolean isGui3d()
        {
            return true;
        }

        @Override
        public boolean usesBlockLight()
        {
            return true;
        }

        @Override
        public boolean isCustomRenderer()
        {
            return false;
        }

        @Override
        public ItemOverrides getOverrides()
        {
            return overrides;
        }
    }

    /**
     * Resolves the item model per stack breed (vanilla item rendering path, also used before FRAPI item emission).
     * Uses the no-arg {@link ItemOverrides} constructor (access widened).
     */
    public static class ItemModelResolver extends ItemOverrides
    {
        private final Baked owner;
        private final ItemOverrides nested;

        public ItemModelResolver(Baked owner, ItemOverrides nested)
        {
            this.owner = owner;
            this.nested = nested;
        }

        @Nullable
        @Override
        public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed)
        {
            var override = nested.resolve(original, stack, level, entity, seed);
            if (override != original) return override;

            var breed = stack.get(DMLRegistry.DRAGON_BREED_COMPONENT.get());
            if (breed != null)
            {
                var model = owner.getBreedModel(breed.getRegisteredName());
                if (model != null) return model;
            }

            return original;
        }
    }
}
