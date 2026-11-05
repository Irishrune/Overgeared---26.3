package net.stirdrem.overgeared.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import java.util.*;

/** Snapshots the anvil contents during extraction; submission never reads live inventory. */
public class SmithingAnvilBlockEntityRenderer implements BlockEntityRenderer<AbstractSmithingAnvilBlockEntity, SmithingAnvilBlockEntityRenderer.State> {
    private final ItemModelResolver resolver;
    public SmithingAnvilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        resolver = context.itemModelResolver();
    }
    public static final class State extends BlockEntityRenderState {
        float facingRotation;
        final List<RenderedItem> items = new ArrayList<>();
    }
    private record RenderedItem(ItemStackRenderState model, boolean block, float y, float z,
                                float rotation, float scale, float heightScale) {}
    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(AbstractSmithingAnvilBlockEntity be, State state, float partialTick,
                                   Vec3 camera, ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, camera, breaking);
        state.items.clear();
        Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.facingRotation = switch (facing) {
            case NORTH -> 180f; case WEST -> 270f; case EAST -> 90f; default -> 0f;
        };
        boolean inputsEmpty = true;
        for (int i = 0; i < 9; i++) inputsEmpty &= be.getRenderStack(i).isEmpty();
        int max = be.getContainerData().get(1);
        float height = max <= 0 ? 1f : Math.clamp(1f - (float)be.getContainerData().get(0) / max, 0f, 1f);
        ItemStack output = be.getRenderStack(10);
        add(state, be, output, isBlock(output) ? 1.05f : 1.02f, inputsEmpty ? 0 : -0.43f, 110, 0.4f, 1);
        // Prefer distinct materials, then fill the remaining three visible inputs.
        Set<Item> seen = new HashSet<>();
        Set<Integer> slots = new HashSet<>();
        float y = 1.01f;
        for (boolean unique : new boolean[]{true, false}) {
            for (int i = 0; i < 9 && slots.size() < 3; i++) {
                ItemStack stack = be.getRenderStack(i);
                if (stack.isEmpty() || slots.contains(i) || (unique && seen.contains(stack.getItem()))) continue;
                boolean block = isBlock(stack);
                add(state, be, stack, y + (block ? 0.09f : 0), 0, 96 + slots.size() * 14,
                    block ? 0.4f : 0.35f, height);
                y += block ? 0.2f : 0.02f;
                seen.add(stack.getItem());
                slots.add(i);
            }
        }
        add(state, be, be.getRenderStack(9), 1.025f, 0.43f, 135, 0.5f, 1);
    }
    private static boolean isBlock(ItemStack stack) { return stack.getItem() instanceof BlockItem; }
    private void add(State state, AbstractSmithingAnvilBlockEntity be, ItemStack stack, float y, float z,
                     float rotation, float scale, float height) {
        if (stack.isEmpty()) return;
        ItemStackRenderState model = new ItemStackRenderState();
        resolver.updateForTopItem(model, stack, ItemDisplayContext.FIXED, be.getLevel(), null, (int)be.getBlockPos().asLong());
        state.items.add(new RenderedItem(model, isBlock(stack), y, z, rotation, scale, height));
    }
    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        double radians = Math.toRadians(state.facingRotation);
        for (RenderedItem item : state.items) {
            pose.pushPose();
            pose.translate(0.5 + item.z * Math.sin(radians), item.y - 0.01 * (1 - item.heightScale),
                           0.5 + item.z * Math.cos(radians));
            pose.rotateDegrees(Axis.YP, state.facingRotation + item.rotation);
            pose.rotateDegrees(Axis.XP, item.block ? 0 : 90);
            pose.scale(item.scale, item.scale, item.scale * item.heightScale);
            item.model.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
