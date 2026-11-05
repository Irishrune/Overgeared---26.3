package net.stirdrem.overgeared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.compat.ModCompat;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.armor.custom.ArmorModelHelper;
import net.stirdrem.overgeared.item.armor.model.CopperHelmet;
import net.stirdrem.overgeared.item.armor.model.CopperLeggings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Client setup shared by both loaders.
 */
public final class OvergearedClient {
    private OvergearedClient() {
    }

    public static void init() {
        // Initialize client-side mod compatibility
        ModCompat.initClient();
    }

    public static void registerItemProperties() {
        net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties.ID_MAPPER.put(
            OvergearedMod.loc("potion_type"), ArrowPotionType.CODEC);
    }

    /** Armor model drawn for the copper helmet. */
    public static HumanoidModel<?> copperHelmetModel() {
        return ArmorModelHelper.withPart("head", new CopperHelmet<>(Minecraft.getInstance().getEntityModels().bakeLayer(CopperHelmet.LAYER_LOCATION)).Head);
    }

    /** Armor model drawn for the copper leggings. */
    public static HumanoidModel<?> copperLeggingsModel() {
        CopperLeggings<?> leggings = new CopperLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(CopperLeggings.LAYER_LOCATION));
        Map<String, ModelPart> parts = new HashMap<>();
        parts.put("head", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("body", leggings.Body);
        parts.put("right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        parts.put("right_leg", leggings.RightLeg);
        parts.put("left_leg", leggings.LeftLeg);

        return new HumanoidModel<>(new ModelPart(Collections.emptyList(), parts));
    }
}
