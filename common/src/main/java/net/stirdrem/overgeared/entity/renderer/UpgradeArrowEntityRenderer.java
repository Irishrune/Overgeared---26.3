package net.stirdrem.overgeared.entity.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class UpgradeArrowEntityRenderer extends ArrowRenderer<UpgradeArrowEntity, UpgradeArrowEntityRenderer.UpgradeArrowRenderState> {
    public UpgradeArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static final class UpgradeArrowRenderState extends ArrowRenderState {
        public ArrowTier tier = ArrowTier.FLINT;
    }

    @Override
    public UpgradeArrowRenderState createRenderState() {
        return new UpgradeArrowRenderState();
    }

    @Override
    public void extractRenderState(UpgradeArrowEntity entity, UpgradeArrowRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tier = entity.getArrowTier();
    }

    @Override
    protected Identifier getTextureLocation(UpgradeArrowRenderState state) {
        ArrowTier tier = state.tier;
        String tierName = tier.getSerializedName(); // e.g., "golden", "iron", etc.
        return Identifier.fromNamespaceAndPath(OvergearedMod.MOD_ID, "textures/entity/projectiles/arrows/" + tierName + ".png");
    }
}
