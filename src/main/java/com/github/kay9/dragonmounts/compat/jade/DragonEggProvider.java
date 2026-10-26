package com.github.kay9.dragonmounts.compat.jade;

import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Shows the breed and hatch stage of a dragon egg block.
 */
public enum DragonEggProvider implements IBlockComponentProvider
{
    INSTANCE;

    public static final ResourceLocation UID = DragonMountsLegacy.id("dragon_egg");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
    {
        if (accessor.getBlockEntity() instanceof HatchableEggBlockEntity egg && egg.hasBreed())
            tooltip.add(Component.translatable("jade.dragonmounts.breed", DragonBreed.getTranslation(egg.getBreedHolder())));

        BlockState state = accessor.getBlockState();
        if (!state.hasProperty(HatchableEggBlock.HATCHING) || !state.hasProperty(HatchableEggBlock.HATCH_STAGE)) return;

        if (state.getValue(HatchableEggBlock.HATCHING))
        {
            int stage = state.getValue(HatchableEggBlock.HATCH_STAGE);
            int maxStage = HatchableEggBlock.HATCH_STAGE.getPossibleValues().size();
            tooltip.add(Component.translatable("jade.dragonmounts.egg.hatching", stage + 1, maxStage).withStyle(ChatFormatting.GRAY));
        }
        else tooltip.add(Component.translatable("jade.dragonmounts.egg.dormant").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ResourceLocation getUid()
    {
        return UID;
    }
}
