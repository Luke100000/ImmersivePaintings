package net.conczin.immersive_paintings.network.payload.s2c;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.c2s.PaintingEditPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PaintingRegisterErrorPayload(ResourceLocation identifier, String error) implements ImmersivePayload {
    public static final Type<PaintingRegisterErrorPayload> TYPE = new Type<>(Main.locate("painting_register_response"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PaintingRegisterErrorPayload> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, PaintingRegisterErrorPayload::identifier,
        ByteBufCodecs.STRING_UTF8, PaintingRegisterErrorPayload::error,
        PaintingRegisterErrorPayload::new
    );

    @Override
    public void handle(Player player) {
        if (Minecraft.getInstance().screen instanceof ImmersivePaintingScreen screen) {
            if (error().isEmpty()) {
                if (screen.entity != null) {
                    Network.Client.sendToServer(new PaintingEditPayload(screen.entity.getId(), identifier(), screen.entity.getFrame(), screen.entity.getMaterial()));

                    if (screen.entity.isGraffiti()) {
                        Minecraft.getInstance().setScreen(null);
                    } else {
                        screen.setPage(ImmersivePaintingScreen.Page.FRAME);
                    }
                }
            } else {
                screen.setPage(ImmersivePaintingScreen.Page.CREATE);
                screen.setError(Component.translatable("immersive_paintings.error." + error()));
            }
        }
    }

    @Override
    public Type<PaintingRegisterErrorPayload> type() {
        return TYPE;
    }
}
