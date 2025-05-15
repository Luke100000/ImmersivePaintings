package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record PaintingEditPayload(int entityId, ResourceLocation motive, ResourceLocation frame, ResourceLocation material) implements ImmersivePayload {
    public static final Type<PaintingEditPayload> TYPE = new Type<>(Main.locate("painting_edit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PaintingEditPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, PaintingEditPayload::entityId,
        ResourceLocation.STREAM_CODEC, PaintingEditPayload::motive,
        ResourceLocation.STREAM_CODEC, PaintingEditPayload::frame,
        ResourceLocation.STREAM_CODEC, PaintingEditPayload::material,
        PaintingEditPayload::new
    );

    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(entityId());
        if (entity instanceof ImmersivePaintingEntity painting) {
            painting.setMotive(motive());
            painting.setFrame(frame());
            painting.setMaterial(material());
        }
    }

    @Override
    public Type<PaintingEditPayload> type() {
        return TYPE;
    }
}
