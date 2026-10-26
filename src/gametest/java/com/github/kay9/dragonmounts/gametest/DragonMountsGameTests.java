package com.github.kay9.dragonmounts.gametest;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DMLRegistry;
import com.github.kay9.dragonmounts.data.loot.DragonEggLootTargets;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlock;
import com.github.kay9.dragonmounts.dragon.egg.HatchableEggBlockEntity;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Game tests for the Fabric port of Dragon Mounts: Legacy. Run with {@code ./gradlew runGametest}.
 */
public class DragonMountsGameTests implements FabricGameTest
{
    private static final List<ResourceKey<DragonBreed>> ALL_BREEDS = List.of(
            DragonBreed.BuiltIn.AETHER, DragonBreed.BuiltIn.END, DragonBreed.BuiltIn.FIRE, DragonBreed.BuiltIn.FOREST,
            DragonBreed.BuiltIn.GHOST, DragonBreed.BuiltIn.ICE, DragonBreed.BuiltIn.NETHER, DragonBreed.BuiltIn.WATER);

    /** floor block (relative), the thing eggs and dragons stand on */
    private static final BlockPos FLOOR = new BlockPos(3, 1, 3);
    private static final BlockPos ABOVE_FLOOR = FLOOR.above();

    // ------------------------------------------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------------------------------------------

    private static void check(boolean condition, String message)
    {
        if (!condition) throw new GameTestAssertException(message);
    }

    private static Registry<DragonBreed> breeds(GameTestHelper helper)
    {
        return DragonBreed.registry(helper.getLevel().registryAccess());
    }

    private static Holder<DragonBreed> breed(GameTestHelper helper, ResourceKey<DragonBreed> key)
    {
        return breeds(helper).getHolder(key).orElseThrow(() -> new GameTestAssertException("Breed not registered: " + key.location()));
    }

    private static ResourceKey<LootTable> lootTableKey(String id)
    {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(id));
    }

    /** A random source that always rolls "success" for float based chances, everything else is real randomness. */
    private static final class LuckyRandom implements RandomSource
    {
        private final RandomSource delegate = RandomSource.create(1234L);

        @Override public RandomSource fork() { return delegate.fork(); }
        @Override public PositionalRandomFactory forkPositional() { return delegate.forkPositional(); }
        @Override public void setSeed(long seed) { delegate.setSeed(seed); }
        @Override public int nextInt() { return delegate.nextInt(); }
        @Override public int nextInt(int bound) { return delegate.nextInt(bound); }
        @Override public long nextLong() { return delegate.nextLong(); }
        @Override public boolean nextBoolean() { return delegate.nextBoolean(); }
        @Override public float nextFloat() { return 0f; }
        @Override public double nextDouble() { return delegate.nextDouble(); }
        @Override public double nextGaussian() { return delegate.nextGaussian(); }
    }

    private static void placeFloor(GameTestHelper helper)
    {
        helper.setBlock(FLOOR, Blocks.STONE);
    }

    private static TameableDragon spawnAdultDragon(GameTestHelper helper, ResourceKey<DragonBreed> breedKey)
    {
        ServerLevel level = helper.getLevel();
        TameableDragon dragon = DMLRegistry.DRAGON.get().create(level);
        check(dragon != null, "Could not create a dragon entity");
        dragon.setBreed(breed(helper, breedKey));
        dragon.setBaby(false);
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(ABOVE_FLOOR));
        dragon.moveTo(at.x, at.y, at.z, 0f, 0f);
        check(level.addFreshEntity(dragon), "Dragon was not added to the level");
        return dragon;
    }

    private static ModConfigSpec.BooleanValue booleanConfig(String field) throws ReflectiveOperationException
    {
        Field f = DMLConfig.class.getDeclaredField(field);
        f.setAccessible(true);
        return (ModConfigSpec.BooleanValue) f.get(null);
    }

    /** rolls the table n times, returns the stacks of all rolls */
    private static List<ItemStack> roll(GameTestHelper helper, String tableId, int rolls, long seed)
    {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(lootTableKey(tableId));
        check(table != LootTable.EMPTY, "Loot table does not exist: " + tableId);

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(Vec3.atCenterOf(ABOVE_FLOOR)))
                .create(LootContextParamSets.CHEST);
        RandomSource random = RandomSource.create(seed);

        List<ItemStack> all = new ArrayList<>();
        for (int i = 0; i < rolls; i++)
            all.addAll(table.getRandomItems(params, random));
        return all;
    }

    private static long countEggs(List<ItemStack> stacks, ResourceKey<DragonBreed> breed)
    {
        return stacks.stream()
                .filter(s -> s.is(DMLRegistry.EGG_BLOCK_ITEM.get()))
                .filter(s ->
                {
                    Holder<DragonBreed> held = s.get(DMLRegistry.DRAGON_BREED_COMPONENT.get());
                    return held != null && held.is(breed);
                })
                .count();
    }

    // ------------------------------------------------------------------------------------------------------------
    // 1. breed registry
    // ------------------------------------------------------------------------------------------------------------

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void breedRegistryContainsExactlyBuiltIns(GameTestHelper helper)
    {
        Registry<DragonBreed> registry = breeds(helper);

        Set<ResourceLocation> expected = new HashSet<>();
        for (ResourceKey<DragonBreed> key : ALL_BREEDS)
        {
            expected.add(key.location());
            Holder.Reference<DragonBreed> holder = registry.getHolder(key).orElse(null);
            check(holder != null, "BuiltIn breed does not resolve: " + key.location());
            check(holder.isBound() && holder.value() != null, "BuiltIn breed is not bound: " + key.location());
        }

        Set<ResourceLocation> actual = new HashSet<>(registry.keySet());
        check(actual.equals(expected), "Breed registry mismatch. expected=" + expected + " actual=" + actual);
        check(registry.size() == 8, "Expected 8 breeds but found " + registry.size());
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------------------
    // 2. egg item with component, and block entity breed
    // ------------------------------------------------------------------------------------------------------------

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void eggItemsCarryBreedComponent(GameTestHelper helper)
    {
        // the creative tab way
        List<ItemStack> tabStacks = new ArrayList<>();
        HatchableEggBlock.populateTab(helper.getLevel().registryAccess(), tabStacks::add);
        check(tabStacks.size() == 8, "populateTab should yield one egg per breed (8) but yielded " + tabStacks.size());

        Set<ResourceLocation> seen = new HashSet<>();
        for (ItemStack stack : tabStacks)
        {
            check(stack.is(DMLRegistry.EGG_BLOCK_ITEM.get()), "populateTab stack is not the egg item: " + stack);
            Holder<DragonBreed> held = stack.get(DMLRegistry.DRAGON_BREED_COMPONENT.get());
            check(held != null, "populateTab stack has no breed component");
            seen.add(held.unwrapKey().orElseThrow().location());
        }
        check(seen.size() == 8, "populateTab breeds not distinct: " + seen);

        // the explicit way, per breed
        for (ResourceKey<DragonBreed> key : ALL_BREEDS)
        {
            ItemStack stack = HatchableEggBlock.Item.create(breed(helper, key));
            check(stack.is(DMLRegistry.EGG_BLOCK_ITEM.get()), "Item.create stack is not the egg item for " + key.location());
            Holder<DragonBreed> held = stack.get(DMLRegistry.DRAGON_BREED_COMPONENT.get());
            check(held != null && held.is(key), "Item.create breed component wrong for " + key.location() + ": " + held);
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void eggPlacedFromItemKeepsBreed(GameTestHelper helper)
    {
        // Placement goes through the real BlockItem path (UseOnContext -> BlockItem.place -> applyImplicitComponents)
        placeFloor(helper);
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ServerLevel level = helper.getLevel();
        BlockPos floorAbs = helper.absolutePos(FLOOR);
        BlockPos eggAbs = helper.absolutePos(ABOVE_FLOOR);

        for (ResourceKey<DragonBreed> key : ALL_BREEDS)
        {
            level.removeBlockEntity(eggAbs);
            level.setBlockAndUpdate(eggAbs, Blocks.AIR.defaultBlockState());

            ItemStack stack = HatchableEggBlock.Item.create(breed(helper, key));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(floorAbs).add(0, 0.5, 0), Direction.UP, floorAbs, false);
            InteractionResult result = stack.useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack, hit));
            check(result.consumesAction(), "Placing the egg item failed for " + key.location() + ", result=" + result);

            BlockState state = level.getBlockState(eggAbs);
            check(state.is(DMLRegistry.EGG_BLOCK.get()), "No egg block after placing the item for " + key.location() + ", found " + state);
            check(level.getBlockEntity(eggAbs) instanceof HatchableEggBlockEntity, "No egg block entity after placing the item for " + key.location());
            HatchableEggBlockEntity be = (HatchableEggBlockEntity) level.getBlockEntity(eggAbs);
            check(be.hasBreed() && be.getBreedHolder().is(key), "Egg BE breed wrong after item placement for " + key.location() + ": " + be.getBreedHolder());

            // and back: pick block / clone item must carry the breed
            ItemStack clone = DMLRegistry.EGG_BLOCK.get().getCloneItemStack(level, eggAbs, state);
            Holder<DragonBreed> cloneBreed = clone.get(DMLRegistry.DRAGON_BREED_COMPONENT.get());
            check(cloneBreed != null && cloneBreed.is(key), "getCloneItemStack breed wrong for " + key.location() + ": " + cloneBreed);
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void eggPlacedViaHelperHasBreed(GameTestHelper helper)
    {
        placeFloor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos eggAbs = helper.absolutePos(ABOVE_FLOOR);

        for (ResourceKey<DragonBreed> key : ALL_BREEDS)
        {
            HatchableEggBlock.place(level, eggAbs, DMLRegistry.EGG_BLOCK.get().defaultBlockState(), breed(helper, key));
            check(level.getBlockState(eggAbs).is(DMLRegistry.EGG_BLOCK.get()), "Egg block not present for " + key.location());
            check(level.getBlockEntity(eggAbs) instanceof HatchableEggBlockEntity be && be.hasBreed() && be.getBreedHolder().is(key),
                    "Egg BE breed wrong for " + key.location());
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------------------
    // 3. loot injection
    // ------------------------------------------------------------------------------------------------------------

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 400)
    public void iglooChestYieldsIceEggs(GameTestHelper helper)
    {
        // The chance is read from the (private) config at roll time and defaults to 0.2. Forcing a different value is not needed:
        // with 2000 rolls P(no egg) = 0.8^2000 which is effectively zero. The config is not modified.
        check(DMLConfig.useLootTables(), "use_loot_tables is expected to be true by default");

        int rolls = 2000;
        List<ItemStack> loot = roll(helper, "minecraft:chests/igloo_chest", rolls, 0xC0FFEEL);
        long ice = countEggs(loot, DragonBreed.BuiltIn.ICE);
        check(ice > 0, "No ice egg in " + rolls + " rolls of the igloo chest");

        // sanity check that the config chance is applied: expect ~0.2 * rolls (sd ~ 18), accept a very wide window
        check(ice > 0.1 * rolls && ice < 0.3 * rolls, "Ice egg rate in igloo chest is off: " + ice + "/" + rolls + " (expected ~0.2)");

        // no other breed leaks into the igloo chest
        for (ResourceKey<DragonBreed> other : ALL_BREEDS)
            if (!other.equals(DragonBreed.BuiltIn.ICE))
                check(countEggs(loot, other) == 0, "Unexpected " + other.location() + " egg in igloo chest");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 600)
    public void allBuiltInLootTargetsYieldTheirEgg(GameTestHelper helper)
    {
        int rolls = 1500;
        for (DragonEggLootTargets.Target target : DragonEggLootTargets.BUILT_IN)
        {
            String table = target.table().toString();
            List<ItemStack> loot = roll(helper, table, rolls, 42L);
            long count = countEggs(loot, target.breed());
            double chance = DMLConfig.getEggChanceFor(target.configKey());
            double expected = rolls * chance;
            check(count > 0, "No " + target.breed().location().getPath() + " egg in " + rolls + " rolls of " + table);
            double sd = Math.sqrt(rolls * chance * (1 - chance));
            check(Math.abs(count - expected) < 6 * sd + 2, "Egg rate off for " + table + ": " + count + " vs expected ~" + expected);
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 400)
    public void nonTargetLootTablesNeverYieldEggs(GameTestHelper helper)
    {
        for (String table : List.of("minecraft:chests/village/village_weaponsmith", "minecraft:chests/spawn_bonus_chest", "minecraft:chests/shipwreck_treasure"))
        {
            List<ItemStack> loot = roll(helper, table, 2000, 7L);
            check(loot.stream().noneMatch(s -> s.is(DMLRegistry.EGG_BLOCK_ITEM.get())), "Dragon egg found in non-target table " + table);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------------------
    // 4. hatching
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Deterministic hatching path: HatchableEggBlock#randomTick with a RandomSource whose nextFloat() is always 0, so the
     * breed's hatch chance always succeeds. A hatching egg goes through HATCH_STAGE 0 -> 3 and hatches on the next success.
     */
    private static void hatchAndVerify(GameTestHelper helper, ResourceKey<DragonBreed> key)
    {
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(ABOVE_FLOOR);
        HatchableEggBlock block = (HatchableEggBlock) DMLRegistry.EGG_BLOCK.get();

        BlockState start = block.defaultBlockState().setValue(HatchableEggBlock.HATCHING, true);
        HatchableEggBlock.place(level, abs, start, breed(helper, key));
        check(block.isRandomlyTicking(level.getBlockState(abs)), "Hatching egg should be randomly ticking");

        RandomSource lucky = new LuckyRandom();
        for (int stage = 0; stage < 3; stage++)
        {
            BlockState state = level.getBlockState(abs);
            check(state.is(block) && state.getValue(HatchableEggBlock.HATCH_STAGE) == stage,
                    "Expected hatch stage " + stage + " but state is " + state + " (" + key.location() + ")");
            block.randomTick(state, level, abs, lucky);
        }

        BlockState finalStage = level.getBlockState(abs);
        check(finalStage.is(block) && finalStage.getValue(HatchableEggBlock.HATCH_STAGE) == 3, "Egg should be at hatch stage 3, state is " + finalStage);
        check(level.getEntitiesOfClass(TameableDragon.class, new AABB(abs).inflate(4)).isEmpty(), "Dragon spawned before the egg finished hatching");

        block.randomTick(finalStage, level, abs, lucky);

        check(level.getBlockState(abs).isAir(), "Egg block should be gone after hatching, found " + level.getBlockState(abs) + " (" + key.location() + ")");
        List<TameableDragon> dragons = level.getEntitiesOfClass(TameableDragon.class, new AABB(abs).inflate(4));
        check(dragons.size() == 1, "Expected exactly one hatched dragon but found " + dragons.size() + " (" + key.location() + ")");
        TameableDragon baby = dragons.get(0);
        check(baby.getBreedHolder() != null && baby.getBreedHolder().is(key), "Hatched dragon has wrong breed: " + baby.getBreedHolder());
        check(baby.isBaby() && baby.isHatchling() && !baby.isAdult(), "Hatched dragon should be a baby/hatchling");
        baby.discard();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void iceEggHatchesIntoBabyIceDragon(GameTestHelper helper)
    {
        placeFloor(helper);
        hatchAndVerify(helper, DragonBreed.BuiltIn.ICE);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void everyBreedHatches(GameTestHelper helper)
    {
        placeFloor(helper);
        for (ResourceKey<DragonBreed> key : ALL_BREEDS)
            hatchAndVerify(helper, key);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void freshEggDoesNotRandomTickUntilUsed(GameTestHelper helper)
    {
        placeFloor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(ABOVE_FLOOR);
        HatchableEggBlock.place(level, abs, DMLRegistry.EGG_BLOCK.get().defaultBlockState(), breed(helper, DragonBreed.BuiltIn.FIRE));
        BlockState state = level.getBlockState(abs);
        check(!state.getValue(HatchableEggBlock.HATCHING), "Fresh egg must not be hatching");
        check(!state.isRandomlyTicking(), "Non-hatching egg must not random tick");

        // using it (right click without item) starts hatching
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.useBlock(ABOVE_FLOOR, player);
        check(level.getBlockState(abs).getValue(HatchableEggBlock.HATCHING), "Right clicking the egg should start hatching");
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------------------
    // 5. dragon spawn / tame / saddle
    // ------------------------------------------------------------------------------------------------------------

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void dragonCanBeTamedAndSaddled(GameTestHelper helper)
    {
        placeFloor(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        TameableDragon dragon = spawnAdultDragon(helper, DragonBreed.BuiltIn.END);

        check(dragon.getBreedHolder().is(DragonBreed.BuiltIn.END), "Spawned dragon has wrong breed");
        check(dragon.isAdult() && !dragon.isHatchling(), "Spawned dragon should be an adult");
        check(!dragon.isTame() && !dragon.isSaddled(), "Fresh dragon must be wild and unsaddled");

        // saddling is refused while wild
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
        dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        check(!dragon.isSaddled(), "Wild dragon must not accept a saddle");

        // the real taming path: feed a taming item, the chance is 1 in 5 per attempt, so retry (P(all 300 fail) ~ 1e-29)
        var tamingItem = dragon.getBreed().tamingItems().stream().findFirst().orElseThrow(() -> new GameTestAssertException("Breed has no taming items")).value();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tamingItem, 64));
        int attempts = 0;
        while (!dragon.isTame() && attempts < 300)
        {
            if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(tamingItem, 64));
            attempts++;
            InteractionResult r = dragon.mobInteract(player, InteractionHand.MAIN_HAND);
            check(r.consumesAction(), "Feeding a taming item should be consumed, result=" + r);
        }
        check(dragon.isTame(), "Dragon not tame after " + attempts + " taming attempts");
        check(dragon.getOwnerUUID() != null && dragon.getOwnerUUID().equals(player.getUUID()), "Wrong owner: " + dragon.getOwnerUUID());
        check(dragon.isOwnedBy(player) && dragon.isTamedFor(player), "Dragon should be owned by the taming player");

        // the real saddle path
        check(dragon.isSaddleable(), "Tame adult dragon should be saddleable");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
        InteractionResult saddleResult = dragon.mobInteract(player, InteractionHand.MAIN_HAND);
        check(saddleResult.consumesAction(), "Saddling should consume the interaction, result=" + saddleResult);
        check(dragon.isSaddled(), "Dragon should be saddled");
        check(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(), "Saddle item should have been consumed");

        // a second player is not the owner
        ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
        check(!dragon.isTamedFor(stranger), "A stranger must not be treated as owner");

        dragon.discard();
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void dragonSaveLoadRoundTrip(GameTestHelper helper)
    {
        placeFloor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        TameableDragon dragon = spawnAdultDragon(helper, DragonBreed.BuiltIn.NETHER);
        dragon.tamedFor(player, true);
        dragon.setSaddled(true);

        CompoundTag tag = new CompoundTag();
        check(dragon.saveAsPassenger(tag), "Dragon did not save");
        dragon.discard();

        Optional<Entity> loaded = EntityType.create(tag, level);
        check(loaded.isPresent() && loaded.get() instanceof TameableDragon, "Dragon did not load back from NBT");
        TameableDragon copy = (TameableDragon) loaded.get();
        check(copy.getBreedHolder() != null && copy.getBreedHolder().is(DragonBreed.BuiltIn.NETHER), "Breed lost in save/load: " + copy.getBreedHolder());
        check(copy.isTame() && player.getUUID().equals(copy.getOwnerUUID()), "Tame state / owner lost in save/load");
        check(copy.isSaddled(), "Saddle lost in save/load");
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------------------
    // 6. replenish dragon egg (mixin into EnderDragon#tickDeath)
    // ------------------------------------------------------------------------------------------------------------

    /** exposes the protected EnderDragon#tickDeath, which is where ReplenishDragonEggMixin hooks in */
    private static final class TestableEnderDragon extends EnderDragon
    {
        TestableEnderDragon(Level level)
        {
            super(EntityType.ENDER_DRAGON, level);
        }

        void runTickDeath()
        {
            tickDeath();
        }
    }

    private static int countVanillaEggsInColumn(ServerLevel level, BlockPos column)
    {
        int count = 0;
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++)
            if (level.getBlockState(new BlockPos(column.getX(), y, column.getZ())).is(Blocks.DRAGON_EGG)) count++;
        return count;
    }

    /**
     * Finds every dragon egg (vanilla or ours) in the union of the origin podium column and the column of the fight's exit portal.
     * Mods like YUNG's Better End Island move the exit portal to a world-fixed position and place their own egg there,
     * so both columns have to be considered. The columns are deduplicated if they are the same.
     */
    private static List<BlockPos> findEggs(ServerLevel level, BlockPos origin, EndDragonFight fight)
    {
        Set<Long> columns = new HashSet<>();
        List<BlockPos> found = new ArrayList<>();
        BlockPos podium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.getLocation(origin));
        List<BlockPos> candidates = new ArrayList<>();
        candidates.add(podium);
        if (fight.portalLocation != null) candidates.add(fight.portalLocation);
        for (BlockPos column : candidates)
        {
            if (!columns.add(ChunkPos.asLong(column.getX(), column.getZ()))) continue;
            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++)
            {
                BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.DRAGON_EGG) || state.is(DMLRegistry.EGG_BLOCK.get())) found.add(pos);
            }
        }
        return found;
    }

    /**
     * Builds a real {@link EndDragonFight} in the test level (it only needs a ServerLevel), marks the dragon as previously killed
     * so vanilla does NOT place an egg itself, and runs the dragon's final death tick (dragonDeathTime == 200 calls
     * EndDragonFight#setDragonKilled, which the mixin wraps). Returns the fight.
     */
    private static EndDragonFight runDragonDeath(GameTestHelper helper, BlockPos origin)
    {
        return runDragonDeath(helper, origin, false);
    }

    /**
     * @param viaKill if true, the death is triggered through {@link EnderDragon#kill()} (like /kill does) instead of the death tick
     */
    private static EndDragonFight runDragonDeath(GameTestHelper helper, BlockPos origin, boolean viaKill)
    {
        ServerLevel level = helper.getLevel();
        TestableEnderDragon dragon = new TestableEnderDragon(level);
        dragon.moveTo(origin.getX() + 0.5, origin.getY() + 20, origin.getZ() + 0.5);

        var data = new EndDragonFight.Data(false, false, true, false, Optional.of(dragon.getUUID()), Optional.empty(), Optional.empty());
        EndDragonFight fight = new EndDragonFight(level, level.getSeed(), data, origin);
        dragon.setDragonFight(fight);
        dragon.setFightOrigin(origin);
        if (viaKill)
        {
            dragon.kill();
            return fight;
        }
        dragon.dragonDeathTime = 199; // tickDeath increments to 200, which finishes the fight
        dragon.runTickDeath();
        return fight;
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void replenishPlacesEggAfterDragonDeath(GameTestHelper helper) throws ReflectiveOperationException
    {
        ModConfigSpec.BooleanValue replenish = booleanConfig("REPLENISH_EGGS");
        check(replenish.get(), "replenish_eggs is expected to be true by default");

        BlockPos origin = helper.absolutePos(new BlockPos(3, 0, 3));
        EndDragonFight fight = runDragonDeath(helper, origin);

        check(fight.portalLocation != null, "Fight did not create an exit portal (setDragonKilled was not reached?)");
        ServerLevel level = helper.getLevel();
        List<BlockPos> eggs = findEggs(level, origin, fight);
        check(eggs.size() == 1, "Expected exactly one dragon egg in the podium / exit portal columns, found " + eggs.size() + " at " + eggs + " (portalLocation=" + fight.portalLocation + ")");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void replenishPlacesEggAfterDragonKillCommand(GameTestHelper helper) throws ReflectiveOperationException
    {
        ModConfigSpec.BooleanValue replenish = booleanConfig("REPLENISH_EGGS");
        check(replenish.get(), "replenish_eggs is expected to be true by default");

        BlockPos origin = helper.absolutePos(new BlockPos(3, 0, 3));
        EndDragonFight fight = runDragonDeath(helper, origin, true);

        check(fight.portalLocation != null, "Fight did not create an exit portal (setDragonKilled was not reached via kill()?)");
        ServerLevel level = helper.getLevel();
        List<BlockPos> eggs = findEggs(level, origin, fight);
        check(eggs.size() == 1, "Expected exactly one dragon egg in the podium / exit portal columns after kill(), found " + eggs.size() + " at " + eggs + " (portalLocation=" + fight.portalLocation + ")");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 200)
    public void replenishRespectsConfigToggle(GameTestHelper helper) throws ReflectiveOperationException
    {
        ModConfigSpec.BooleanValue replenish = booleanConfig("REPLENISH_EGGS");
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(3, 0, 3));
        boolean previous = replenish.get();
        replenish.set(false);
        try
        {
            EndDragonFight fight = runDragonDeath(helper, origin);
            check(fight.portalLocation != null, "Fight did not create an exit portal (setDragonKilled was not reached?)");
            // Only the origin podium column is checked: with a mod like YUNG's Better End Island installed, that mod may place its own egg
            // at its world-fixed exit portal regardless of our config, but it never uses the test's origin column.
            BlockPos eggPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, EndPodiumFeature.getLocation(origin));
            check(countVanillaEggsInColumn(level, eggPos) == 0, "No egg should be placed when replenish_eggs is false");
        }
        finally
        {
            replenish.set(previous);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------------------------------------------------
    // 7. vanilla egg conversion (UseBlockCallback)
    // ------------------------------------------------------------------------------------------------------------

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void vanillaEggConvertsToEndEggOnUse(GameTestHelper helper)
    {
        placeFloor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(ABOVE_FLOOR);
        check(DMLConfig.allowEggOverride(), "allow_egg_override is expected to be true by default");

        level.setBlockAndUpdate(abs, Blocks.DRAGON_EGG.defaultBlockState());
        check(level.getBlockState(abs).is(Blocks.DRAGON_EGG), "Setup failed: vanilla egg not placed");

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        InteractionResult result = UseBlockCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, hit);
        check(result.consumesAction(), "UseBlockCallback should consume the interaction, result=" + result);

        BlockState state = level.getBlockState(abs);
        check(state.is(DMLRegistry.EGG_BLOCK.get()), "Vanilla egg was not converted, found " + state);
        check(state.getValue(HatchableEggBlock.HATCHING), "Converted egg should be hatching");
        check(level.getBlockEntity(abs) instanceof HatchableEggBlockEntity be && be.hasBreed() && be.getBreedHolder().is(DragonBreed.BuiltIn.END),
                "Converted egg should have the end breed");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void vanillaEggStaysVanillaWhenOverrideDisabled(GameTestHelper helper) throws ReflectiveOperationException
    {
        placeFloor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(ABOVE_FLOOR);
        ModConfigSpec.BooleanValue override = booleanConfig("ALLOW_EGG_OVERRIDE");
        boolean previous = override.get();
        override.set(false);
        try
        {
            level.setBlockAndUpdate(abs, Blocks.DRAGON_EGG.defaultBlockState());
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, hit);
            check(!result.consumesAction(), "Callback must not consume when override is disabled, result=" + result);
            check(level.getBlockState(abs).is(Blocks.DRAGON_EGG), "Vanilla egg must stay vanilla when override is disabled");
        }
        finally
        {
            override.set(previous);
        }
        helper.succeed();
    }
}
