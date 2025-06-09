package net.conczin.immersive_paintings.network.payload.s2c;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.client.gui.ImmersivePaintingScreen;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;

public record OpenGuiPayload(GuiType guiType, int entityId, int minResolution, int maxResolution, boolean showOtherPlayersPaintings, int uploadPermissionLevel) implements ImmersivePayload {
    public static final Type<OpenGuiPayload> TYPE = new Type<>(Main.locate("open_gui"));
    public static final StreamCodec<FriendlyByteBuf, OpenGuiPayload> STREAM_CODEC = StreamCodec.composite(
        GuiType.STREAM_CODEC, OpenGuiPayload::guiType,
        ByteBufCodecs.INT, OpenGuiPayload::entityId,
        ByteBufCodecs.INT, OpenGuiPayload::minResolution,
        ByteBufCodecs.INT, OpenGuiPayload::maxResolution,
        ByteBufCodecs.BOOL, OpenGuiPayload::showOtherPlayersPaintings,
        ByteBufCodecs.INT, OpenGuiPayload::uploadPermissionLevel,
        OpenGuiPayload::new
    );

    @Override
    public void handle(Player player, Runner runnable) {
        GuiType type = guiType();
        int entityId = entityId();
        int minResolution = minResolution();
        int maxResolution = maxResolution();
        boolean showOtherPlayersPaintings = showOtherPlayersPaintings();
        int uploadPermissionLevel = uploadPermissionLevel();

        runnable.run(() -> {
            if (type == GuiType.EDITOR)
                ScreenProxy.load(entityId, minResolution, maxResolution, showOtherPlayersPaintings, uploadPermissionLevel);
        });
    }

    @Override
    public Type<OpenGuiPayload> type() {
        return TYPE;
    }

    public enum GuiType implements StringRepresentable {
        EDITOR;

        public static final Codec<GuiType> CODEC = StringRepresentable.fromEnum(GuiType::values);
        public static final StreamCodec<ByteBuf, GuiType> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

        public String getSerializedName() {
            return name();
        }
    }

    // Move to different class so that the network registration does not think Screen is being loaded on the server
    private static class ScreenProxy {
        public static void load(int entityId, int minResolution, int maxResolution, boolean showOtherPlayersPaintings, int uploadPermissionLevel) {
            Minecraft.getInstance().setScreen(new ImmersivePaintingScreen(
                    entityId,
                    minResolution, maxResolution,
                    showOtherPlayersPaintings, uploadPermissionLevel
            ));
        }
    }
}
