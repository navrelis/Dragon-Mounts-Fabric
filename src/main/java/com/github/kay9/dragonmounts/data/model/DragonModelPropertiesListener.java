package com.github.kay9.dragonmounts.data.model;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.client.DragonModel;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Fabric port: this listener no longer registers a model layer per breed (there is no equivalent of NeoForge's
 * {@code ClientHooks#registerLayerDefinition} for dynamic layers). It only stores the decoded {@link DragonModel.Properties}
 * per breed id; {@link com.github.kay9.dragonmounts.client.DragonRenderer} bakes the per-breed models lazily from them
 * and drops its cache whenever {@link #getVersion()} changes.
 */
public class DragonModelPropertiesListener extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener
{
    public static final DragonModelPropertiesListener INSTANCE = new DragonModelPropertiesListener();

    private static final ResourceLocation ID = DragonMountsLegacy.id("dragon_model_properties");
    private static final String FOLDER = "models/entity/dragon/breed/properties";

    private volatile Map<ResourceLocation, DragonModel.Properties> properties = Map.of();
    private volatile int version = 0;

    public DragonModelPropertiesListener()
    {
        super(new GsonBuilder().create(), FOLDER);
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return ID;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        var parsed = new HashMap<ResourceLocation, DragonModel.Properties>(map.size());

        for (var entry : map.entrySet())
        {
            var properties = DragonModel.Properties.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(JsonParseException::new);
            parsed.put(entry.getKey(), properties);
        }

        this.properties = Map.copyOf(parsed);
        ++version; // tells renderers to throw away models baked from the previous properties
    }

    /**
     * @return the model properties for the given breed, or null if the breed has no properties file (use the standard model).
     */
    @Nullable
    public DragonModel.Properties getProperties(ResourceLocation breedId)
    {
        return properties.get(breedId);
    }

    /**
     * @return a counter that is incremented every time the properties are reloaded.
     */
    public int getVersion()
    {
        return version;
    }
}
