package net.conczin.immersive_paintings.network.payload.s2c;


import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.Painting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record PaintingSyncPayload(Map<Identifier, Optional<Painting>> paintings) implements ImmersivePayload {
    public static final Type<PaintingSyncPayload> TYPE = new Type<>(Main.locate("painting_list"));
    public static final StreamCodec<FriendlyByteBuf, PaintingSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.optional(Painting.STREAM_CODEC)), PaintingSyncPayload::paintings,
            PaintingSyncPayload::new
    );

    public PaintingSyncPayload(Identifier identifier, Painting painting) {
        this(Map.of(identifier, Optional.ofNullable(painting)));
    }

    @Override
    public void handle(Player player, Runner runner) {
        Map<Identifier, Optional<Painting>> paintings = paintings();
        runner.run(() -> {
            paintings.forEach((id, painting) -> {
                if (painting.isEmpty()) {
                    ClientPaintingManager.deregisterPainting(id);
                } else {
                    ClientPaintingManager.registerPainting(id, painting.get());
                }
            });

            if (Minecraft.getInstance().screen instanceof ImmersivePaintingScreen screen) {
                screen.refreshPage();
            }
        });
    }

    @Override
    public Type<PaintingSyncPayload> type() {
        return TYPE;
    }
}
