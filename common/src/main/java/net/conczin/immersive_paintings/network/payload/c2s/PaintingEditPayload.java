package net.conczin.immersive_paintings.network.payload.c2s;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public record PaintingEditPayload(int entityId, Map<Option, String> options) implements ImmersivePayload {
    public static final Type<PaintingEditPayload> TYPE = new Type<>(Main.locate("painting_edit"));
    public static final StreamCodec<FriendlyByteBuf, PaintingEditPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, PaintingEditPayload::entityId,
        ByteBufCodecs.map(HashMap::new, Option.STREAM_CODEC, ByteBufCodecs.STRING_UTF8), PaintingEditPayload::options,
        PaintingEditPayload::new
    );

    @Override
    public void handle(Player player, Runner runner) {
        int entityId = entityId();
        Map<Option, String> options = options();

        runner.run(() -> {
            Entity entity = player.level().getEntity(entityId);

            if (entity instanceof ImmersivePaintingEntity painting) {
                options.forEach((option, value) -> {
                    switch (option) {
                        case Option.MOTIVE:
                            painting.setMotive(ResourceLocation.parse(value));
                            break;
                        case Option.FRAME:
                            painting.setFrame(ResourceLocation.parse(value));
                            break;
                        case Option.MATERIAL:
                            painting.setMaterial(ResourceLocation.parse(value));
                            break;
                        case Option.DELETE:

                            break;
                    }
                });
            }
        });
    }

    @Override
    public Type<PaintingEditPayload> type() {
        return TYPE;
    }

    public enum Option implements StringRepresentable {
        MOTIVE,
        FRAME,
        MATERIAL,
        DELETE;

        @Override
        public String getSerializedName() {
            return name();
        }

        private static final Codec<Option> CODEC = StringRepresentable.fromValues(Option::values);
        public static final StreamCodec<ByteBuf, Option> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    }
}
