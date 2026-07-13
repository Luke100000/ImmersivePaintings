package net.conczin.immersive_paintings.network.payload.s2c;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.c2s.PaintingEditPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;

public record PaintingRegisterResponsePayload(Optional<Identifier> identifier, String error) implements ImmersivePayload {
    public static final Type<PaintingRegisterResponsePayload> TYPE = new Type<>(ImmersivePaintings.locate("painting_register_response"));
    public static final StreamCodec<FriendlyByteBuf, PaintingRegisterResponsePayload> STREAM_CODEC = StreamCodec.of((buf, msg) -> {
        buf.writeOptional(msg.identifier(), Identifier.STREAM_CODEC);
        buf.writeUtf(msg.error());
    }, buf -> {
        Optional<Identifier> identifier = buf.readOptional(Identifier.STREAM_CODEC);
        String err = buf.readUtf();
        return new PaintingRegisterResponsePayload(identifier, err);
    });

    @Override
    public void handle(Player player, Runner runner) {
        Optional<Identifier> id = identifier();
        String err = error();

        runner.run(() -> {
            if (Minecraft.getInstance().gui.screen() instanceof ImmersivePaintingScreen screen) {
                if (err.isEmpty()) {
                    if (screen.entity != null && id.isPresent()) {
                        NetworkHandler.Client.sendToServer(new PaintingEditPayload(screen.entity.getUUID(), Map.of(
                                PaintingEditPayload.Option.MOTIVE, id.get().toString(),
                                PaintingEditPayload.Option.FRAME, screen.entity.getFrame().toString(),
                                PaintingEditPayload.Option.MATERIAL, screen.entity.getMaterial().toString()
                        )));

                        if (screen.entity.isGraffiti()) {
                            Minecraft.getInstance().gui.setScreen(null);
                        } else {
                            screen.setPage(ImmersivePaintingScreen.Page.FRAME);
                        }
                    }
                } else {
                    screen.setPage(ImmersivePaintingScreen.Page.CREATE);
                    screen.setError(Component.translatable("immersive_paintings.error." + err));
                }
            }
        });
    }

    @Override
    public Type<PaintingRegisterResponsePayload> type() {
        return TYPE;
    }
}
