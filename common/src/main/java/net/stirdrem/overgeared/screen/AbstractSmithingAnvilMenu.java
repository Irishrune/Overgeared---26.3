package net.stirdrem.overgeared.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.stirdrem.overgeared.platform.Services;
import net.stirdrem.overgeared.util.items.IItemHandler;
import net.stirdrem.overgeared.util.items.SlotItemHandler;
import net.stirdrem.overgeared.util.items.RecipeWrapper;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.compat.polymorph.Polymorph;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbstractSmithingAnvilMenu extends RecipeBookMenu {
    private final CraftingContainer craftingContainer = new CraftingContainer() {
        @Override
        public void fillStackedContents(StackedContents contents) {
            for (int i = 0; i < getContainerSize(); i++) {
                ItemStack stack = getItem(i);
                if (!stack.isEmpty()) {
                    contents.accountSimpleStack(stack);
                }
            }
        }

        @Override
        public int getWidth() {
            return 3;
        }

        @Override
        public int getHeight() {
            return 3;
        }

        @Override
        public List<ItemStack> getItems() {
            List<ItemStack> items = new ArrayList<>(getContainerSize());
            for (int i = 0; i < getContainerSize(); i++) {
                items.add(getItem(i));
            }
            return items;
        }

        @Override
        public int getContainerSize() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < 9; i++) {
                if (!getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return blockEntity.getItemHandler().getStackInSlot(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = getItem(slot).copy();
            if (!stack.isEmpty()) {
                if (stack.getCount() <= amount) {
                    blockEntity.getItemHandler().setStackInSlot(slot, ItemStack.EMPTY);
                    return stack;
                } else {
                    ItemStack split = stack.split(amount);
                    blockEntity.getItemHandler().setStackInSlot(slot, stack);
                    return split;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = blockEntity.getItemHandler().getStackInSlot(slot).copy();
            blockEntity.getItemHandler().setStackInSlot(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            blockEntity.getItemHandler().setStackInSlot(slot, stack);
        }

        @Override
        public void setChanged() {
            blockEntity.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < 9; i++) {
                blockEntity.getItemHandler().setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    };
    public final AbstractSmithingAnvilBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private Slot resultSlot;
    private final Player player;
    private final List<Integer> craftingSlotIndices = new ArrayList<>();

    public AbstractSmithingAnvilMenu(MenuType<?> pMenuType, int pContainerId, Inventory inv, AbstractSmithingAnvilBlockEntity entity, ContainerData data, boolean hasBlueprint) {
        super(pMenuType, pContainerId);
        checkContainerSize(inv, 12);
        blockEntity = entity;
        this.level = inv.player.level();
        this.data = data;
        this.player = inv.player;

        IItemHandler iItemHandler = this.blockEntity.getItemHandler();

        this.addSlot(new SlotItemHandler(iItemHandler, 9, 152, 61) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.SMITHING_HAMMERS);
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }
        }); //hammer

        if (hasBlueprint && ServerConfig.ENABLE_BLUEPRINT_FORGING.get())
            this.addSlot(new SlotItemHandler(iItemHandler, 11, 95, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ModItems.BLUEPRINT.get()) || stack.getItem() instanceof SmithingTemplateItem;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return true; // This is crucial for JEI transfers
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }

                @Override
                public void set(ItemStack stack) {
                    // Always limit to a single item
                    super.set(stack.copyWithCount(1));
                }
            });

        //crafting slot
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                Slot slotIndex = this.addSlot(new SlotItemHandler(iItemHandler, j + i * 3, 30 + j * 18, 17 + i * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return true; // This is crucial for JEI transfers
                    }
                });
                craftingSlotIndices.add(slotIndex.index); // Store the index, not the Slot object
            }
        }

        //output slot
        this.resultSlot = new SlotItemHandler(iItemHandler, 10, 124, 35) {
            private int removeCount;

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return true; // This is crucial for JEI transfers
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                this.checkTakeAchievements(stack);
                super.onTake(player, stack);
            }

            @Override
            public ItemStack remove(int amount) {
                if (this.hasItem())
                    this.removeCount += Math.min(amount, this.getItem().getCount());
                return super.remove(amount);
            }

            @Override
            public void onQuickCraft(ItemStack output, int amount) {
                this.removeCount += amount;
                this.checkTakeAchievements(output);
            }

            @Override
            protected void onSwapCraft(int amount) {
                this.removeCount = amount;
            }

            @Override
            protected void checkTakeAchievements(ItemStack stack) {
                if (this.removeCount > 0) {
                    stack.onCraftedBy(AbstractSmithingAnvilMenu.this.player, this.removeCount);
                    Services.PLATFORM.firePlayerCraftingEvent(AbstractSmithingAnvilMenu.this.player, stack, AbstractSmithingAnvilMenu.this.craftingContainer);
                }
                // Award recipe to player for recipe book integration
                AbstractSmithingAnvilMenu.this.blockEntity.getCurrentRecipeHolder().ifPresent(holder ->
                        AbstractSmithingAnvilMenu.this.player.awardRecipes(List.of(holder))
                );
                this.removeCount = 0;
            }
        };
        this.addSlot(this.resultSlot);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addDataSlots(data);
    }

    public List<Integer> getInputSlots() {
        return new ArrayList<>(craftingSlotIndices);
    }

    /**
     * Gets the result slot directly.
     * This is preferred over using slot index which varies based on blueprint presence.
     */
    public Slot getResultSlot() {
        return this.resultSlot;
    }


    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static int blueprintEnabled = ServerConfig.ENABLE_BLUEPRINT_FORGING.get() ? 1 : 0;
    private static int VANILLA_FIRST_SLOT_INDEX = 11 + blueprintEnabled;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 12;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot clickedSlot = this.slots.get(index);
        if (clickedSlot == null || !clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = clickedSlot.getItem();
        ItemStack copy = stack.copy();

        int totalSlots = this.slots.size();

        // Determine boundaries dynamically
        int firstPlayerSlot = -1;

        // Player inventory always starts after result slot
        firstPlayerSlot = this.slots.indexOf(this.resultSlot) + 1;
        Slot hammerSlot = this.slots.get(0); // first slot added
        boolean blueprintEnabled = ServerConfig.ENABLE_BLUEPRINT_FORGING.get();
        Slot blueprintSlot = blueprintEnabled ? this.slots.get(1) : null;

        if (clickedSlot == hammerSlot || (blueprintEnabled && clickedSlot == blueprintSlot)) {
            if (!moveItemStackTo(stack, firstPlayerSlot, totalSlots, true)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                clickedSlot.set(ItemStack.EMPTY);
            } else {
                clickedSlot.setChanged();
            }

            clickedSlot.onTake(player, stack);
            return copy;
        }

        // =========================
        // If clicking TE slot
        // =========================
        if (index < firstPlayerSlot) {

            // Prevent shift-clicking output
            if (clickedSlot == this.resultSlot) {
                if (!moveItemStackTo(stack, firstPlayerSlot, totalSlots, false)) {
                    return ItemStack.EMPTY;
                }
                clickedSlot.onTake(player, stack);
                return copy;
            }

            if (!moveItemStackTo(stack, firstPlayerSlot, totalSlots, false)) {
                return ItemStack.EMPTY;
            }
        }
        // =========================
        // If clicking Player inventory
        // =========================
        else {

            // Hammer slot detection
            // Move hammer
            if (stack.is(ModTags.Items.SMITHING_HAMMERS)) {
                if (!moveItemStackTo(stack,
                        hammerSlot.index,
                        hammerSlot.index + 1,
                        false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Move blueprint
            else if (blueprintEnabled &&
                    (stack.is(ModItems.BLUEPRINT.get())
                            || stack.getItem() instanceof SmithingTemplateItem)) {

                if (!moveItemStackTo(stack,
                        blueprintSlot.index,
                        blueprintSlot.index + 1,
                        false)) {
                    return ItemStack.EMPTY;
                }
            }
            // Move to crafting grid
            else {
                boolean moved = false;

                for (int gridIndex : craftingSlotIndices) {
                    if (moveItemStackTo(stack, gridIndex, gridIndex + 1, false)) {
                        moved = true;
                        break;
                    }
                }

                if (!moved) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            clickedSlot.set(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }

        clickedSlot.onTake(player, stack);

        return copy;
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);  // Max Progress
        int progressArrowSize = 24; // This is the height in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        // get block at the container’s position
        BlockState state = player.level().getBlockState(blockEntity.getBlockPos());
        Block block = state.getBlock();

        // check if the block at that position is in your tag
        boolean isValid = state.is(ModTags.Blocks.SMITHING_ANVIL);

        return AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player,
                block  // only passed here for distance check
        ) && isValid;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getRemainingHits() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        return maxProgress - progress;
    }

    public ItemStack getResultItem() {
        // Check if the block entity exists and has an item handler
        if (blockEntity != null) {
            IItemHandler handler = blockEntity.getItemHandler();
            // Slot 10 is the output slot based on your menu setup
            ItemStack result = handler.getStackInSlot(10);
            // Return a copy to prevent modification of the original stack
            return result.copy();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getGhostResult() {
        // On client side, check if Polymorph has a selected output
        if (level.isClientSide()) {
            try {
                if (Polymorph.LOADED) {
                    Optional<ItemStack> polymorphOutput =
                            Polymorph.getSelectedOutput();
                    if (polymorphOutput.isPresent()) {
                        return polymorphOutput.get();
                    } else {
                        Optional<ForgingRecipe> recipeOptional = blockEntity.getCurrentRecipe();
                        if (recipeOptional.isPresent()) {
                            ForgingRecipe recipe = recipeOptional.get();
                            if (blockEntity.hasRecipe()) {
                                return recipe.getResultItem(level.registryAccess()).copy();
                            }
                        }
                        return ItemStack.EMPTY;
                    }
                }
            } catch (Exception e) {
                // Polymorph not available - fall through to default behavior
            }
        }

        // Default behavior: return the expected result based on current inputs
        // Avoid using cached recipe (getCurrentRecipe())
        Optional<ForgingRecipe> recipeOptional = blockEntity.getCurrentRecipe();
        if (recipeOptional.isPresent()) {
            ForgingRecipe recipe = recipeOptional.get();
            if (blockEntity.hasRecipe()) {
                return recipe.getResultItem(level.registryAccess()).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
        for (int i = 0; i < this.craftingContainer.getContainerSize(); ++i) {
            contents.accountSimpleStack(this.craftingContainer.getItem(i));
        }
    }

    public void clearCraftingContent() {
        if (this.player.level().isClientSide()) return;
        this.craftingContainer.clearContent();
    }

    public boolean recipeMatches(RecipeHolder<ForgingRecipe> recipe) {
        return recipe.value().matches(new RecipeWrapper(blockEntity.getItemHandler()), level);
    }

    public int getResultSlotIndex() {
        return this.resultSlot.index;
    }

    public int getGridWidth() {
        return 3;
    }

    public int getGridHeight() {
        return 3;
    }

    public int getSize() {
        return 9;
    }

    @Override
    public @NotNull RecipeBookType getRecipeBookType() {
        return OvergearedMod.FORGING;
    }

    public boolean shouldMoveToInventory(int slot) {
        return slot < (getGridWidth() * getGridHeight());
    }

    public AbstractSmithingAnvilBlockEntity getBlockEntity() {
        return blockEntity;
    }

    /**
     * Fabric 26.3 calls the recipe-book placement hook with a ServerLevel and
     * a drop-permission flag.  Forging recipes deliberately use
     * PlacementInfo.NOT_PLACEABLE (their optional blueprint and patterned metal
     * parts do not fit vanilla's crafting placement algorithm), so retain a
     * custom, inventory-conserving transfer here instead.
     *
     * The existing hands-on hammering/minigame is still required after placement.
     * Never overwrite an occupied grid slot, and preflight every ingredient
     * before removing a stack from the player's inventory.  Players may clear
     * an incompatible grid manually; the recipe book only places into a clean
     * or compatible grid.
     */
    @Override
    public PostPlaceAction handlePlacement(boolean useMaxItems, boolean allowDroppingItemsToClear,
                                           RecipeHolder<?> recipeHolder, ServerLevel serverLevel,
                                           Inventory inventory) {
        if (!(recipeHolder.value() instanceof ForgingRecipe recipe)
                || inventory.player != this.player
                || this.player.containerMenu != this
                || this.level != serverLevel) {
            return PostPlaceAction.NOTHING;
        }

        int width = recipe.getWidth();
        int height = recipe.getHeight();
        List<Optional<Ingredient>> ingredients = recipe.getIngredients();
        if (width < 1 || width > 3 || height < 1 || height > 3
                || ingredients.size() != width * height) {
            return PostPlaceAction.NOTHING;
        }

        int playerSlotStart = this.slots.indexOf(this.resultSlot) + 1;
        int[] remaining = new int[this.slots.size()];
        for (int i = playerSlotStart; i < this.slots.size(); i++) {
            remaining[i] = this.slots.get(i).getItem().getCount();
        }

        // Select a source inventory slot for every unfilled pattern position
        // *before* moving anything, including repeated ingredients.
        int[] sourceSlot = new int[9];
        java.util.Arrays.fill(sourceSlot, -1);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int patternIndex = row * width + col;
                int gridIndex = row * 3 + col;
                Slot target = this.slots.get(this.craftingSlotIndices.get(gridIndex));
                Optional<Ingredient> optional = ingredients.get(patternIndex);
                if (optional.isEmpty()) {
                    if (target.hasItem()) return PostPlaceAction.PLACE_GHOST_RECIPE;
                    continue;
                }
                Ingredient ingredient = optional.get();
                if (target.hasItem()) {
                    if (!ingredient.test(target.getItem())) {
                        return PostPlaceAction.PLACE_GHOST_RECIPE;
                    }
                    // Keep manually placed items intact (do not silently grow a
                    // stack or add extra resources when placing one recipe).
                    continue;
                }

                int matchingSource = -1;
                for (int i = playerSlotStart; i < this.slots.size(); i++) {
                    if (remaining[i] > 0 && ingredient.test(this.slots.get(i).getItem())) {
                        matchingSource = i;
                        remaining[i]--;
                        break;
                    }
                }
                if (matchingSource < 0) return PostPlaceAction.PLACE_GHOST_RECIPE;
                sourceSlot[gridIndex] = matchingSource;
            }
        }

        // A partial/shifted grid can invalidate the forged recipe. Do not
        // discard outside-pattern items without a deliberate player action.
        for (int gridIndex = 0; gridIndex < 9; gridIndex++) {
            int row = gridIndex / 3;
            int col = gridIndex % 3;
            if ((row >= height || col >= width)
                    && this.slots.get(this.craftingSlotIndices.get(gridIndex)).hasItem()) {
                return PostPlaceAction.PLACE_GHOST_RECIPE;
            }
        }

        for (int gridIndex = 0; gridIndex < sourceSlot.length; gridIndex++) {
            int src = sourceSlot[gridIndex];
            if (src < 0) continue;
            Slot from = this.slots.get(src);
            Slot target = this.slots.get(this.craftingSlotIndices.get(gridIndex));
            ItemStack one = from.remove(1);
            target.set(one);
            from.setChanged();
        }
        this.broadcastChanges();
        return PostPlaceAction.NOTHING;
    }
}
