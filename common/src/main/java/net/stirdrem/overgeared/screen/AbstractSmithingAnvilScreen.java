package net.stirdrem.overgeared.screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.client.ForgingRecipeBookComponent;
import net.stirdrem.overgeared.config.ClientConfig;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

@Environment(EnvType.CLIENT)
public abstract class AbstractSmithingAnvilScreen<T extends AbstractSmithingAnvilMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
	private final ForgingRecipeBookComponent recipeBookComponent;
    private final Identifier texture;
	private boolean widthTooNarrow;

	public AbstractSmithingAnvilScreen(final T menu, final Inventory inventory, final Component title, boolean enableBlueprintSlot) {
		super(menu, inventory, title);
		this.recipeBookComponent = new ForgingRecipeBookComponent(menu);
        this.texture = OvergearedMod.loc(enableBlueprintSlot ? "textures/gui/smithing_anvil.png" : "textures/gui/stone_smithing_anvil.png");
        this.titleLabelX = 28;
	}

	@Override
	protected void init() {
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
		if (!ClientConfig.ENABLE_ANVIL_RECIPE_BOOK.get()) this.recipeBookComponent.hide();
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		this.initButton();
	}

	protected ScreenPosition getRecipeBookButtonPosition() { return new ScreenPosition(leftPos + 5, height / 2 - 49); }

	private void initButton() {
		ScreenPosition buttonPos = this.getRecipeBookButtonPosition();
		if (ClientConfig.ENABLE_ANVIL_RECIPE_BOOK.get()) this.addRenderableWidget(new ImageButton(buttonPos.x(), buttonPos.y(), 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
			this.recipeBookComponent.toggleVisibility();
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
			ScreenPosition updatedButtonPos = this.getRecipeBookButtonPosition();
			button.setPosition(updatedButtonPos.x(), updatedButtonPos.y());
			this.onRecipeBookButtonClick();
		}));
		this.addWidget(this.recipeBookComponent);
	}

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        if (widthTooNarrow && recipeBookComponent.isVisible()) return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.isCrafting()) graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 89, topPos + 35,
            176, 0, menu.getScaledProgress(), 17, 256, 256);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        if (menu.getRemainingHits() > 0) graphics.text(font,
            Component.translatable("gui.overgeared.remaining_hits", menu.getRemainingHits()), 89, 17, 0xff404040, false);
    }

	protected void onRecipeBookButtonClick() {
	}

	@Override
	public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
			this.extractBackground(graphics, mouseX, mouseY, a);
		} else {
			super.extractContents(graphics, mouseX, mouseY, a);
		}

		graphics.nextStratum();
		this.recipeBookComponent.extractRenderState(graphics, mouseX, mouseY, a);
		graphics.nextStratum();
		this.extractCarriedItem(graphics, mouseX, mouseY);
		this.extractTooltip(graphics, mouseX, mouseY);
		this.recipeBookComponent.extractTooltip(graphics, mouseX, mouseY, this.hoveredSlot);
	}

	@Override
	protected void extractSlots(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
		super.extractSlots(graphics, mouseX, mouseY);
		this.recipeBookComponent.extractGhostRecipe(graphics, this.isBiggerResultSlot());
        if (!menu.getResultSlot().hasItem()) {
            ItemStack preview = menu.getGhostResult();
            if (!preview.isEmpty()) {
                graphics.fakeItem(preview, 124, 35);
                graphics.fill(124, 35, 140, 51, 0x80c6c6c6);
                if (isHovering(124, 35, 16, 16, mouseX, mouseY))
                    graphics.setTooltipForNextFrame(font, preview, mouseX, mouseY);
            }
        }
	}

	protected boolean isBiggerResultSlot() {
		return true;
	}

	@Override
	public boolean charTyped(final CharacterEvent event) {
		return this.recipeBookComponent.charTyped(event) ? true : super.charTyped(event);
	}

	@Override
	public boolean keyPressed(final KeyEvent event) {
		return this.recipeBookComponent.keyPressed(event) ? true : super.keyPressed(event);
	}

	@Override
	public boolean isInputCaptured() {
		return super.isInputCaptured() || this.recipeBookComponent.capturesInput();
	}

	@Override
	public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
		if (this.recipeBookComponent.mouseClicked(event, doubleClick)) {
			this.setFocused(this.recipeBookComponent);
			return true;
		} else {
			return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(event, doubleClick);
		}
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
		return this.recipeBookComponent.mouseDragged(event, dx, dy) ? true : super.mouseDragged(event, dx, dy);
	}

	@Override
	protected boolean isHovering(final int left, final int top, final int w, final int h, final double xm, final double ym) {
		return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(left, top, w, h, xm, ym);
	}

	@Override
	protected boolean hasClickedOutside(final double mx, final double my, final int xo, final int yo) {
		boolean clickedOutside = mx < xo || my < yo || mx >= xo + this.imageWidth || my >= yo + this.imageHeight;
		return this.recipeBookComponent.hasClickedOutside(mx, my, this.leftPos, this.topPos, this.imageWidth, this.imageHeight) && clickedOutside;
	}

	@Override
	protected void slotClicked(final Slot slot, final int slotId, final int buttonNum, final ContainerInput containerInput) {
		super.slotClicked(slot, slotId, buttonNum, containerInput);
		this.recipeBookComponent.slotClicked(slot);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		this.recipeBookComponent.tick();
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookComponent.recipesUpdated();
	}

	@Override
	public void fillGhostRecipe(final RecipeDisplay display) {
		this.recipeBookComponent.fillGhostRecipe(display);
	}
}
