package com.github.kay9.dragonmounts.compat.emi;

import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.DragonSpawnEgg;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * EMI integration. Only loaded by Fabric Loader through the {@code emi} entrypoint, so EMI being absent is safe.
 * Eggs and spawn eggs share one item per type and differ only by their breed component, so EMI needs a
 * comparison to keep them apart.
 */
@EmiEntrypoint
public class DragonMountsEmiPlugin implements EmiPlugin
{
    @Override
    public void register(EmiRegistry registry)
    {
        Item eggItem = DMLRegistry.EGG_BLOCK_ITEM.get();
        Item spawnEggItem = DMLRegistry.SPAWN_EGG.get();
        DataComponentType<Holder<DragonBreed>> breedType = DMLRegistry.DRAGON_BREED_COMPONENT.get();

        Comparison byBreed = Comparison.compareData(stack ->
        {
            Holder<DragonBreed> breed = stack.get(breedType);
            return breed == null? null : breed.getRegisteredName();
        });
        registry.setDefaultComparison(eggItem, byBreed);
        registry.setDefaultComparison(spawnEggItem, byBreed);

        List<EmiStack> eggs = new ArrayList<>();
        List<EmiStack> spawnEggs = new ArrayList<>();
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null)
        {
            HatchableEggBlock.populateTab(level.registryAccess(), stack -> eggs.add(EmiStack.of(stack)));
            DragonSpawnEgg.populateTab(level.registryAccess(), stack -> spawnEggs.add(EmiStack.of(stack)));
        }

        // Make sure every breed is indexed (the creative tab usually did that already), then drop the component-less
        // default stacks EMI creates for every registered item plus any duplicate of an already indexed breed.
        // Stacks are baked in index order, so the first stack of a breed wins; ours are appended last.
        eggs.forEach(registry::addEmiStack);
        spawnEggs.forEach(registry::addEmiStack);

        Set<String> seenEggs = new HashSet<>();
        Set<String> seenSpawnEggs = new HashSet<>();
        registry.removeEmiStacks(stack ->
        {
            Item item = stack.getKeyOfType(Item.class);
            if (item != eggItem && item != spawnEggItem) return false;

            Holder<DragonBreed> breed = stack.get(breedType);
            if (breed == null) return true;
            return !(item == eggItem? seenEggs : seenSpawnEggs).add(breed.getRegisteredName());
        });

        if (!eggs.isEmpty())
        {
            List<EmiIngredient> ingredients = new ArrayList<>(eggs);
            ingredients.add(EmiStack.of(Items.DRAGON_EGG));
            registry.addRecipe(new EmiInfoRecipe(ingredients, List.of(
                    Component.translatable("emi.info.dragonmounts.dragon_egg"),
                    Component.empty(),
                    Component.translatable("emi.info.dragonmounts.dragon_egg.hatch"),
                    Component.empty(),
                    Component.translatable("emi.info.dragonmounts.dragon_egg.habitat"),
                    Component.empty(),
                    Component.translatable("emi.info.dragonmounts.dragon_egg.vanilla")
            ), DragonMountsLegacy.id("/dragon_egg_info")));
        }

        if (!spawnEggs.isEmpty())
            registry.addRecipe(new EmiInfoRecipe(new ArrayList<EmiIngredient>(spawnEggs),
                    List.of(Component.translatable("emi.info.dragonmounts.spawn_egg")),
                    DragonMountsLegacy.id("/dragon_spawn_egg_info")));
    }
}
