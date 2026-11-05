package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

/**
 * Bridge for Overgeared's existing raw-JSON datapack listeners.
 *
 * Minecraft 26.3 replaced the old (Gson, directory) reload listener
 * constructor with (Codec<T>, FileToIdConverter). The existing listeners
 * intentionally parse JSON elements themselves, with per-file diagnostics
 * and custom data validation, so preserve their Map<Identifier, JsonElement>
 * apply hooks by adapting the new codec entry point.
 *
 * The Gson parameter preserves the old listener constructors. Vanilla now
 * parses the JSON via JsonOps, and the codec below is lossless on JsonElement.
 * Custom Gson adapters have not been used by these legacy listeners.
 */
public abstract class LegacyJsonResourceReloadListener
        extends SimpleJsonResourceReloadListener<JsonElement> {

    private static final Codec<JsonElement> RAW_JSON = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            json -> new Dynamic<>(JsonOps.INSTANCE, json));

    protected LegacyJsonResourceReloadListener(Gson gson, String directory) {
        super(RAW_JSON, FileToIdConverter.json(directory));
    }
}
