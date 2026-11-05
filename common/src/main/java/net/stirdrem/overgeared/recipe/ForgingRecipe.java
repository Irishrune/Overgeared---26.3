package net.stirdrem.overgeared.recipe;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.client.ForgingBookCategory;
import net.stirdrem.overgeared.components.BlueprintData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.util.ModTags;

import java.util.*;
import java.util.stream.Collectors;

public class ForgingRecipe implements OvergearedStationRecipe<RecipeInput> {
    private static final int BLUEPRINT_SLOT = 11;
    public final int width;
    public final int height;
    private final String group;
    private final Set<String> blueprintTypes;
    private final String tier;
    private final List<String> pattern;
    private final Map<String, ForgingIngredient> key;
    private final NonNullList<ForgingIngredient> ingredients;
    private final ItemStackTemplate result;
    private final Optional<ItemStackTemplate> failedResult;
    private final int hammering;
    private final boolean hasQuality;
    private final boolean needsMinigame;
    private final boolean requiresBlueprint;
    private final boolean hasPolishing;
    private final boolean needQuenching;
    private final boolean showNotification;
    private final ForgingQuality minimumQuality;
    private final ForgingQuality qualityDifficulty;
    private final ForgingBookCategory tab;

    public ForgingRecipe(String group, boolean requireBlueprint, Set<String> blueprintTypes, String tier,
                         List<String> pattern, Map<String, ForgingIngredient> key, NonNullList<ForgingIngredient> ingredients,
                         ItemStackTemplate result, Optional<ItemStackTemplate> failedResult, int hammering, boolean hasQuality, boolean needsMinigame, boolean hasPolishing, boolean needQuenching, boolean showNotification, ForgingQuality minimumQuality, ForgingQuality qualityDifficulty, int width, int height, ForgingBookCategory tab) {
        this.group = group;
        this.blueprintTypes = blueprintTypes;
        this.tier = tier;
        this.pattern = pattern;
        this.key = key;
        this.ingredients = ingredients;
        this.result = result;
        this.failedResult = failedResult;
        this.hammering = hammering;
        this.hasQuality = hasQuality;
        this.requiresBlueprint = requireBlueprint;
        this.needsMinigame = needsMinigame;
        this.hasPolishing = hasPolishing;
        this.needQuenching = needQuenching;
        this.showNotification = showNotification;
        this.minimumQuality = minimumQuality;
        this.width = width;
        this.height = height;
        this.qualityDifficulty = qualityDifficulty;
        this.tab = tab;
    }

    @Override public boolean isSpecial() { return false; }
    @Override public PlacementInfo placementInfo() { return PlacementInfo.createFromOptionals(getIngredients()); }
    @Override public RecipeBookCategory recipeBookCategory() {
        return switch (tab) {
            case TOOL_HEADS -> ForgingRecipeCategories.TOOLS;
            case ARMORS -> ForgingRecipeCategories.ARMORS;
            case MISC -> ForgingRecipeCategories.MISC;
        };
    }
    @Override public java.util.List<net.minecraft.world.item.crafting.display.RecipeDisplay> display() {
        var inputs = getIngredients().stream().map(i -> i.map(Ingredient::display)
            .orElse(net.minecraft.world.item.crafting.display.SlotDisplay.Empty.INSTANCE)).toList();
        var grid = new net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay(width, height, inputs,
            new net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay(result),
            new net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay(net.stirdrem.overgeared.block.ModBlocks.SMITHING_ANVIL.get().asItem()));
        return List.of(new ForgingRecipeDisplay(grid, tier));
    }

    public static Optional<ForgingRecipe> findBestMatch(Level world, RecipeInput RecipeInput) {
        return findBestMatchHolder(world, RecipeInput).map(RecipeHolder::value);
    }

    public static Optional<RecipeHolder<ForgingRecipe>> findBestMatchHolder(Level world, RecipeInput RecipeInput) {
        return RecipeLookups.all(world, ModRecipeTypes.FORGING.get())
                .stream()
                .filter(holder -> holder.value().matches(RecipeInput, world))
                .max(Comparator.comparingInt(holder -> holder.value().getRecipeSize()));
    }

    private boolean checkBlueprint(RecipeInput recipeInput) {
        ItemStack blueprintStack = recipeInput.getItem(BLUEPRINT_SLOT);

        // Case 1: Blueprint not required and no types defined -> slot must be empty
        if (!requiresBlueprint && blueprintTypes.isEmpty()) {
            return blueprintStack.isEmpty();
        }

        // If slot empty
        if (blueprintStack.isEmpty()) {
            return !requiresBlueprint;
        }

        BlueprintData data = blueprintStack.get(ModComponents.BLUEPRINT_DATA.get());
        if (data == null) return false;

        String toolType = data.toolType();
        if (toolType.isEmpty()) return false;

        return blueprintTypes.contains(toolType);
    }


    @Override
    public boolean matches(RecipeInput recipeInput, Level world) {
        return findMatchOrigin(recipeInput) >= 0;
    }

    /**
     * Returns the top-left grid slot of the matched pattern, or -1.
     * The anvil input also contains a blueprint slot at index 11.
     */
    private int findMatchOrigin(RecipeInput input) {
        if (input.size() <= BLUEPRINT_SLOT || width < 1 || width > 3 || height < 1 || height > 3)
            return -1;
        if (!checkBlueprint(input)) return -1;
        for (int y = 0; y <= 3 - height; y++) {
            for (int x = 0; x <= 3 - width; x++) {
                if (matchesPattern(input, x, y) && checkSurroundingBlanks(input, x, y)) {
                    return y * 3 + x;
                }
            }
        }
        return -1;
    }

    private boolean checkSurroundingBlanks(RecipeInput RecipeInput, int xOffset, int yOffset) {
        // Check if slots outside the recipe pattern are empty
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                // Skip slots that are part of the recipe
                if (x >= xOffset && x < xOffset + width &&
                        y >= yOffset && y < yOffset + height) {
                    continue;
                }

                // Check if non-recipe slots are empty
                int invSlot = y * 3 + x;
                if (!RecipeInput.getItem(invSlot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean matchesPattern(RecipeInput RecipeInput, int xOffset, int yOffset) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int invSlot = (y + yOffset) * 3 + (x + xOffset);
                ForgingIngredient ingredient = ingredients.get(y * width + x);

                if (ingredient.ingredient().isEmpty()) {
                    if (!RecipeInput.getItem(invSlot).isEmpty()) return false;
                } else {
                    if (!ingredient.test(RecipeInput.getItem(invSlot))) return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput) {
        ItemStack output = result.create();
        int origin = findMatchOrigin(recipeInput);
        // Preserve the old preview behavior for nonmatching inputs, but do not
        // copy data components from unrelated slots in an invalid grid.
        if (origin < 0) return output;

        int originX = origin % 3;
        int originY = origin / 3;
        for (int i = 0; i < ingredients.size(); i++) {
            ForgingIngredient ingredient = ingredients.get(i);
            if (ingredient.ingredient().isEmpty() || !ingredient.transferNBT()) continue;

            int inputSlot = ForgingGridSlots.slotForPatternIndex(i, width, originX, originY);
            ItemStack inputStack = recipeInput.getItem(inputSlot);
            if (!inputStack.isEmpty()) {
                // Apply the components from the *matched* component, not from slot i.
                mergeComponents(output, inputStack);
            }
        }
        return output;
    }


    // Legacy helper kept for Overgeared callers; no longer in Recipe (26.3).
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
    }

    // Legacy helper kept for Overgeared callers; no longer in Recipe (26.3).
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.create();
    }

    public boolean hasFailedResult() {
        return failedResult.isPresent();
    }

    public ItemStack getFailedResultItem(HolderLookup.Provider registries) {
        return failedResult.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
    }

    // Legacy helper kept for Overgeared callers; no longer in Recipe (26.3).
    public boolean isIncomplete() {
        return false;
    }

    // Legacy helper kept for Overgeared callers; no longer in Recipe (26.3).
    public List<Optional<Ingredient>> getIngredients() {
        return ingredients.stream().map(ForgingIngredient::ingredient).toList();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return ModRecipeSerializers.FORGING_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ModRecipeTypes.FORGING.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ForgingRecipe that = (ForgingRecipe) obj;
        return Objects.equals(this.result.item(), that.result.item()) &&
                Objects.equals(this.ingredients, that.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result.item(), ingredients);
    }

    public int getHammeringRequired() {
        return hammering;
    }

    public String getAnvilTier() {
        return tier;
    }

    public boolean hasQuality() {
        return hasQuality;
    }

    public boolean needsMinigame() {
        return needsMinigame;
    }

    @Override
    public String group() {
        return getGroup();
    }

    public String getGroup() {
        return group;
    }

    public ForgingQuality getMinimumQuality() {
        return minimumQuality;
    }

    public ForgingQuality getQualityDifficulty() {
        return qualityDifficulty;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    public int getRemainingHits() {
        return hammering;
    }

    public boolean hasPolishing() {
        return hasPolishing;
    }

    public Set<String> getBlueprintTypes() {
        return blueprintTypes.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public boolean requiresBlueprint() {
        return requiresBlueprint;
    }

    public boolean needQuenching() {
        return needQuenching;
    }

    public List<String> getPattern() {
        return pattern;
    }

    public Map<String, ForgingIngredient> getKey() {
        return key;
    }

    private int getRecipeSize() {
        return width * height;
    }

    public NonNullList<ForgingIngredient> getForgingIngredients() {
        return ingredients;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ForgingBookCategory getRecipeBookTab() {
        return tab != null ? tab : ForgingBookCategory.MISC;
    }

    public static final class Serializer {

        // Codec for ForgingQuality enum
        private static final Codec<ForgingQuality> FORGING_QUALITY_CODEC = Codec.STRING.xmap(
                ForgingQuality::fromString,
                ForgingQuality::getDisplayName
        );

        // Codec for Set<String> of blueprint types
        private static final Codec<Set<String>> BLUEPRINT_TYPES_CODEC = Codec.either(
                Codec.STRING,
                Codec.STRING.listOf()
        ).xmap(
                either -> either.map(
                        s -> s.isBlank() ? Set.of() : Set.of(s.toLowerCase(Locale.ROOT)),
                        list -> list.stream()
                                .filter(s -> !s.isBlank())
                                .map(s -> s.toLowerCase(Locale.ROOT))
                                .collect(Collectors.toCollection(LinkedHashSet::new))
                ),
                set -> com.mojang.datafixers.util.Either.right(new ArrayList<>(set))
        );


        public record ForgingFlags(
                boolean hasQuality,
                boolean needsMinigame,
                boolean hasPolishing,
                boolean needQuenching,
                boolean showNotification
        ) {

            public static final Codec<ForgingFlags> BOOLEAN_CODEC =
                    RecordCodecBuilder.create(instance ->
                            instance.group(
                                    Codec.BOOL.optionalFieldOf("has_quality", true)
                                            .forGetter(ForgingFlags::hasQuality),

                                    Codec.BOOL.optionalFieldOf("needs_minigame", false)
                                            .forGetter(ForgingFlags::needsMinigame),

                                    Codec.BOOL.optionalFieldOf("has_polishing", true)
                                            .forGetter(ForgingFlags::hasPolishing),

                                    Codec.BOOL.optionalFieldOf("need_quenching", true)
                                            .forGetter(ForgingFlags::needQuenching),

                                    Codec.BOOL.optionalFieldOf("show_notification", true)
                                            .forGetter(ForgingFlags::showNotification)
                            ).apply(instance, ForgingFlags::new)
                    );
        }

        private static final Codec<ForgingBookCategory> TAB_CODEC =
                Codec.STRING.xmap(
                        ForgingBookCategory::findByName,
                        ForgingBookCategory::name
                );

        // StreamCodec for Set<String>
        private static final StreamCodec<RegistryFriendlyByteBuf, Set<String>> BLUEPRINT_TYPES_STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public Set<String> decode(RegistryFriendlyByteBuf buffer) {
                        int count = buffer.readVarInt();
                        Set<String> set = new LinkedHashSet<>();
                        for (int i = 0; i < count; i++) {
                            set.add(buffer.readUtf());
                        }
                        return set;
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buffer, Set<String> value) {
                        buffer.writeVarInt(value.size());
                        for (String s : value) {
                            buffer.writeUtf(s);
                        }
                    }
                };

        // StreamCodec for ForgingQuality
        private static final StreamCodec<RegistryFriendlyByteBuf, ForgingQuality> FORGING_QUALITY_STREAM_CODEC = new StreamCodec<>() {
            @Override
            public ForgingQuality decode(RegistryFriendlyByteBuf buffer) {
                return ForgingQuality.fromString(buffer.readUtf());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ForgingQuality value) {
                buffer.writeUtf(value.getDisplayName());
            }
        };

        // StreamCodec for NonNullList<Ingredient>
        private static final StreamCodec<RegistryFriendlyByteBuf, NonNullList<ForgingIngredient>> INGREDIENTS_STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public NonNullList<ForgingIngredient> decode(RegistryFriendlyByteBuf buffer) {
                        int size = buffer.readVarInt();
                        NonNullList<ForgingIngredient> list = NonNullList.withSize(size, ForgingIngredient.EMPTY);

                        for (int i = 0; i < size; i++) {
                            list.set(i, ForgingIngredient.STREAM_CODEC.decode(buffer));
                        }

                        return list;
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buffer, NonNullList<ForgingIngredient> value) {
                        buffer.writeVarInt(value.size());
                        for (ForgingIngredient ingredient : value) {
                            ForgingIngredient.STREAM_CODEC.encode(buffer, ingredient);
                        }
                    }
                };

        private static final StreamCodec<RegistryFriendlyByteBuf, ForgingBookCategory> TAB_STREAM_CODEC =
                StreamCodec.of(
                        (buf, value) -> buf.writeUtf(value.name()),
                        buf -> ForgingBookCategory.findByName(buf.readUtf())
                );

        public static final MapCodec<ForgingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("requires_blueprint", false).forGetter(ForgingRecipe::requiresBlueprint),
                    BLUEPRINT_TYPES_CODEC.optionalFieldOf("blueprint", Set.of()).forGetter(ForgingRecipe::getBlueprintTypes),
                    Codec.STRING.optionalFieldOf("tier", AnvilTier.IRON.getDisplayName()).forGetter(ForgingRecipe::getAnvilTier),
                    Codec.list(Codec.STRING).fieldOf("pattern").forGetter(ForgingRecipe::getPattern),
                    Codec.unboundedMap(Codec.STRING, ForgingIngredient.CODEC).fieldOf("key").forGetter(ForgingRecipe::getKey),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                    ItemStackTemplate.CODEC.optionalFieldOf("result_failed").forGetter(r -> r.failedResult),
                    Codec.INT.optionalFieldOf("hammering", 1).forGetter(ForgingRecipe::getHammeringRequired),
                    Codec.BOOL.optionalFieldOf("has_quality", true).forGetter(ForgingRecipe::hasQuality),
                    Codec.BOOL.optionalFieldOf("needs_minigame", false).forGetter(ForgingRecipe::needsMinigame),
                    Codec.BOOL.optionalFieldOf("has_polishing", true).forGetter(ForgingRecipe::hasPolishing),
                    Codec.BOOL.optionalFieldOf("need_quenching", true).forGetter(ForgingRecipe::needQuenching),
                    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ForgingRecipe::showNotification),
                    FORGING_QUALITY_CODEC.optionalFieldOf("minimumQuality", ForgingQuality.POOR).forGetter(ForgingRecipe::getMinimumQuality),
                    FORGING_QUALITY_CODEC.optionalFieldOf("quality_difficulty", ForgingQuality.NONE).forGetter(ForgingRecipe::getQualityDifficulty),
                    TAB_CODEC.optionalFieldOf("category", ForgingBookCategory.MISC).forGetter(ForgingRecipe::getRecipeBookTab)
            ).apply(instance, (requiresBlueprint, blueprintTypes, tier, pattern, key, result, failedResult,
                               hammering, hasQuality, needsMinigame, hasPolishing, needQuenching, showNotification,
                               minimumQuality, qualityDifficulty, tab) -> {
                // Parse the pattern using the key map
                if (pattern.isEmpty()) {
                    throw new IllegalArgumentException("Pattern cannot be empty");
                }
                ForgingGridSlots.validatePattern(pattern, key.keySet());
                int width = pattern.getFirst().length();
                int height = pattern.size();

                NonNullList<ForgingIngredient> ingredients =
                        NonNullList.withSize(width * height, ForgingIngredient.EMPTY);

                for (int y = 0; y < height; y++) {
                    String row = pattern.get(y);
                    if (row.length() != width) {
                        throw new IllegalArgumentException("Pattern row width mismatch");
                    }

                    for (int x = 0; x < width; x++) {
                        char c = row.charAt(x);
                        String keyStr = String.valueOf(c);

                        ForgingIngredient ingredient =
                                c == ' ' ? ForgingIngredient.EMPTY : key.get(keyStr);

                        ingredients.set(y * width + x, ingredient);
                    }
                }

                // Since 26.3, armor is identified by an equippable component,
                // not the removed ArmorItem class. Keep the original exemption
                // from quenching for armor, but not for non-armor equipment.
                boolean actualNeedQuenching = needQuenching;
                var equippable = result.create().get(DataComponents.EQUIPPABLE);
                if (equippable != null && equippable.slot().isArmor()) {
                    actualNeedQuenching = false;
                }

                return new ForgingRecipe("", requiresBlueprint, new LinkedHashSet<>(blueprintTypes), tier,
                        new ArrayList<>(pattern), new LinkedHashMap<>(key), ingredients,
                        result, failedResult, hammering, hasQuality, needsMinigame, hasPolishing, actualNeedQuenching,
                        showNotification, minimumQuality, qualityDifficulty, width, height, tab);
            }));


        public static final StreamCodec<RegistryFriendlyByteBuf, ForgingRecipe> STREAM_CODEC = new StreamCodec<>() {
                @Override
                public ForgingRecipe decode(RegistryFriendlyByteBuf buffer) {
                    String group = buffer.readUtf();
                    boolean requiresBlueprint = buffer.readBoolean();
                    Set<String> blueprintTypes = BLUEPRINT_TYPES_STREAM_CODEC.decode(buffer);
                    String tier = buffer.readUtf();
                    int hammering = buffer.readVarInt();
                    boolean hasQuality = buffer.readBoolean();
                    boolean needsMinigame = buffer.readBoolean();
                    boolean hasPolishing = buffer.readBoolean();
                    boolean needQuenching = buffer.readBoolean();
                    boolean showNotification = buffer.readBoolean();
                    int width = buffer.readVarInt();
                    int height = buffer.readVarInt();
                    ForgingQuality minimumQuality = FORGING_QUALITY_STREAM_CODEC.decode(buffer);
                    ForgingQuality qualityDifficulty = FORGING_QUALITY_STREAM_CODEC.decode(buffer);
                    NonNullList<ForgingIngredient> ingredients = INGREDIENTS_STREAM_CODEC.decode(buffer);
                    ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
                    Optional<ItemStackTemplate> failedResult = buffer.readBoolean()
                            ? Optional.of(ItemStackTemplate.STREAM_CODEC.decode(buffer)) : Optional.empty();
                    ForgingBookCategory tab = TAB_STREAM_CODEC.decode(buffer);
                    // Pattern and key are not synced over network - only ingredients are needed at runtime
                    return new ForgingRecipe(group, requiresBlueprint, blueprintTypes, tier,
                            List.of(), Map.of(), ingredients,
                            result, failedResult, hammering, hasQuality, needsMinigame, hasPolishing,
                            needQuenching, showNotification, minimumQuality, qualityDifficulty, width, height, tab);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ForgingRecipe recipe) {
                    buffer.writeUtf(recipe.group);
                    buffer.writeBoolean(recipe.requiresBlueprint);
                    BLUEPRINT_TYPES_STREAM_CODEC.encode(buffer, recipe.blueprintTypes);
                    buffer.writeUtf(recipe.tier);
                    buffer.writeVarInt(recipe.hammering);
                    buffer.writeBoolean(recipe.hasQuality);
                    buffer.writeBoolean(recipe.needsMinigame);
                    buffer.writeBoolean(recipe.hasPolishing);
                    buffer.writeBoolean(recipe.needQuenching);
                    buffer.writeBoolean(recipe.showNotification);
                    buffer.writeVarInt(recipe.width);
                    buffer.writeVarInt(recipe.height);
                    FORGING_QUALITY_STREAM_CODEC.encode(buffer, recipe.minimumQuality);
                    FORGING_QUALITY_STREAM_CODEC.encode(buffer, recipe.qualityDifficulty);
                    INGREDIENTS_STREAM_CODEC.encode(buffer, recipe.ingredients);
                    ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
                    buffer.writeBoolean(recipe.failedResult.isPresent());
                    recipe.failedResult.ifPresent(template -> ItemStackTemplate.STREAM_CODEC.encode(buffer, template));
                    TAB_STREAM_CODEC.encode(buffer, recipe.tab);
                }
            };

    }

    private Serializer.ForgingFlags getFlags() {
        return new Serializer.ForgingFlags(
                this.hasQuality,
                this.needsMinigame,
                this.hasPolishing,
                this.needQuenching,
                this.showNotification
        );
    }


    public record ForgingIngredient(
            Optional<Ingredient> ingredient,
            boolean requiresHeated,
            boolean transferNBT
    ) {
        public static final ForgingIngredient EMPTY =
                new ForgingIngredient(Optional.empty(), false, false);

        public boolean test(ItemStack stack) {
            if (ingredient.isEmpty()) return stack.isEmpty();
            if (!ingredient.get().test(stack)) return false;
            return !requiresHeated || Boolean.TRUE.equals(stack.get(ModComponents.HEATED_COMPONENT.get())) || stack.is(ModTags.Items.HEATED_METALS);
        }

        public static final Codec<ForgingIngredient> CODEC = new Codec<>() {
            private <T> boolean getBool(DynamicOps<T> ops, T input, String key) {
                return ops.get(input, key).flatMap(ops::getBooleanValue).result().orElse(false);
            }

            @Override
            public <T> DataResult<Pair<ForgingIngredient, T>> decode(DynamicOps<T> ops, T input) {
                // Accept the new explicit ingredient field and old datapack item/tag objects.
                T encoded = ops.get(input, "ingredient").result().orElse(input);
                var item = ops.get(input, "item").flatMap(ops::getStringValue).result();
                var tag = ops.get(input, "tag").flatMap(ops::getStringValue).result();
                if (item.isPresent()) encoded = ops.createString(item.get());
                else if (tag.isPresent()) encoded = ops.createString("#" + tag.get());
                return Ingredient.CODEC.parse(ops, encoded).map(ing -> Pair.of(
                        new ForgingIngredient(Optional.of(ing), getBool(ops, input, "requires_heated"), getBool(ops, input, "transfer_nbt")),
                        input
                ));
            }

            @Override
            public <T> DataResult<T> encode(ForgingIngredient input, DynamicOps<T> ops, T prefix) {
                if (input.ingredient().isEmpty()) {
                    return DataResult.error(() -> "Blank slots must be represented by spaces in the forging pattern");
                }
                return Ingredient.CODEC.encodeStart(ops, input.ingredient().get()).flatMap(encoded -> {
                    DataResult<T> result = ops.mergeToMap(prefix, ops.createString("ingredient"), encoded);
                    if (input.requiresHeated) {
                        result = result.flatMap(m -> ops.mergeToMap(m, ops.createString("requires_heated"), ops.createBoolean(true)));
                    }
                    if (input.transferNBT) {
                        result = result.flatMap(m -> ops.mergeToMap(m, ops.createString("transfer_nbt"), ops.createBoolean(true)));
                    }
                    return result;
                });
            }
        };

        /* ---------------- Network Codec ---------------- */

        public static final StreamCodec<RegistryFriendlyByteBuf, ForgingIngredient> STREAM_CODEC =
                StreamCodec.of(ForgingIngredient::write, ForgingIngredient::read);

        private static ForgingIngredient read(RegistryFriendlyByteBuf buf) {
            Optional<Ingredient> ingredient = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf);
            boolean requiresHeated = buf.readBoolean();
            boolean transferNBT = buf.readBoolean();
            return new ForgingIngredient(ingredient, requiresHeated, transferNBT);
        }

        private static void write(RegistryFriendlyByteBuf buf, ForgingIngredient ingredient) {
            Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, ingredient.ingredient());
            buf.writeBoolean(ingredient.requiresHeated());
            buf.writeBoolean(ingredient.transferNBT());
        }
    }


    public static void mergeComponents(ItemStack target, ItemStack source) {
        DataComponentMap sourceComponents = source.getComponents();
        DataComponentMap targetComponents = target.getComponents();

        for (DataComponentType<?> type : sourceComponents.keySet()) {
            // If target already has component â†’ overwrite
            if (targetComponents.has(type)) {
                target.remove(type);
            }

            // Copy component value
            copyComponent(target, source, type);
        }
    }

    private static <T> void copyComponent(ItemStack target, ItemStack source, DataComponentType<T> type) {
        T value = source.get(type);
        if (value != null) {
            target.set(type, value);
        }
    }

}