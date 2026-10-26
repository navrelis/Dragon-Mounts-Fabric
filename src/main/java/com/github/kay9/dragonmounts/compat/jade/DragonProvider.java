package com.github.kay9.dragonmounts.compat.jade;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Shows breed, life stage and saddle status of a dragon.
 */
public enum DragonProvider implements IEntityComponentProvider
{
    INSTANCE;

    public static final ResourceLocation UID = DragonMountsLegacy.id("dragon");
    public static final ResourceLocation SHOW_SADDLE = DragonMountsLegacy.id("dragon.show_saddle");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config)
    {
        if (!(accessor.getEntity() instanceof TameableDragon dragon)) return;

        var breed = dragon.getBreedHolder();
        if (breed != null)
            tooltip.add(Component.translatable("jade.dragonmounts.breed", DragonBreed.getTranslation(breed)));

        String stage = dragon.isAdult()? "adult" : dragon.isJuvenile()? "juvenile" : "hatchling";
        tooltip.add(Component.translatable("jade.dragonmounts.stage", Component.translatable("jade.dragonmounts.stage." + stage)).withStyle(ChatFormatting.GRAY));

        if (config.get(SHOW_SADDLE))
            tooltip.add(Component.translatable("jade.dragonmounts.saddle", Component.translatable("jade.dragonmounts.saddle." + (dragon.isSaddled()? "yes" : "no"))).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ResourceLocation getUid()
    {
        return UID;
    }
}
