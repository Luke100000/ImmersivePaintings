package net.conczin.immersive_paintings.registration;

import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.c2s.*;
import net.conczin.immersive_paintings.network.payload.s2c.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class Network {
    public static void register(Registrar c) {
        c.register(ImageRequestPayload.TYPE, ImageRequestPayload.STREAM_CODEC, true);
        c.register(ImageUploadPayload.TYPE, ImageUploadPayload.STREAM_CODEC, true);
        c.register(PaintingRegisterPayload.TYPE, PaintingRegisterPayload.STREAM_CODEC, true);
        c.register(PaintingEditPayload.TYPE, PaintingEditPayload.STREAM_CODEC, true);
        c.register(PaintingDeletePayload.TYPE, PaintingDeletePayload.STREAM_CODEC, true);

        c.register(ImageResponsePayload.TYPE, ImageResponsePayload.STREAM_CODEC, false);
        c.register(OpenGuiPayload.TYPE, OpenGuiPayload.STREAM_CODEC, false);
        c.register(PaintingSyncPayload.TYPE, PaintingSyncPayload.STREAM_CODEC, false);
        c.register(PaintingRegisterErrorPayload.TYPE, PaintingRegisterErrorPayload.STREAM_CODEC, false);
    }

    public interface Registrar {
        <T extends ImmersivePayload> void register(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean isServer);
    }
}
