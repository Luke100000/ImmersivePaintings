package net.conczin.immersive_paintings.network.payload.c2s;

import net.conczin.immersive_paintings.Config;
import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingListPayload;
import net.conczin.immersive_paintings.network.payload.s2c.PaintingRegisterErrorPayload;
import net.conczin.immersive_paintings.painting.Painting;
import net.conczin.immersive_paintings.painting.ServerPaintingManager;
import net.conczin.immersive_paintings.util.ByteImage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public record PaintingRegisterPayload(int width, int height, int resolution, String name, boolean hidden, boolean graffiti) implements ImmersivePayload {
    public static final Type<PaintingRegisterPayload> TYPE = new Type<>(Main.locate("painting_register"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PaintingRegisterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PaintingRegisterPayload::width,
            ByteBufCodecs.INT, PaintingRegisterPayload::height,
            ByteBufCodecs.INT, PaintingRegisterPayload::resolution,
            ByteBufCodecs.STRING_UTF8, PaintingRegisterPayload::name,
            ByteBufCodecs.BOOL, PaintingRegisterPayload::hidden,
            ByteBufCodecs.BOOL, PaintingRegisterPayload::graffiti,
            PaintingRegisterPayload::new
    );

    private static void paintingRegisterError(Player player, String error, ResourceLocation i) {
        Network.sendToClient((ServerPlayer)player, new PaintingRegisterErrorPayload(i, error));
    }

    // Separate method to allow for Xerca compatibility
    public ResourceLocation handle(Player player, ByteImage image, String author, Painting.Type type) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String hash = String.format("%032x", new BigInteger(1, md5.digest(image.encode())));
            Painting painting = new Painting(width(), height(), resolution(), name(), author, player.getUUID(), type, hidden(), graffiti(), hash);
            ResourceLocation identifier = painting.location();

            ServerPaintingManager.registerPainting(player.getServer(), identifier, painting, image);
            Network.sendToAllClients(player.getServer(), new PaintingListPayload(identifier, painting));
            paintingRegisterError(player, "", identifier);
            return identifier;
        } catch (NoSuchAlgorithmException e) {
            Painting painting = new Painting(width(), height(), resolution(), name(), author, player.getUUID(), type, hidden(), graffiti(), "");
            paintingRegisterError(player, "", painting.location());
        }

        return null;
    }

    @Override
    public void handle(Player player) {
        ByteImage image = ServerPaintingManager.uploadedImages.remove(player.getUUID());

        if (!player.hasPermissions(Config.getInstance().uploadPermissionLevel)) {
            paintingRegisterError(player, "no_permission", null);
            return;
        }

        if (image.getWidth() > Config.getInstance().maxUserImageWidth || image.getHeight() > Config.getInstance().maxUserImageHeight) {
            paintingRegisterError(player, "too_large", null);
            return;
        }

        long count = ServerPaintingManager.getCustomPaintings(player.getServer()).values().stream().filter(p -> p.author().equals(player.getGameProfile().getName())).count();
        if (count > Config.getInstance().maxUserImages) {
            paintingRegisterError(player, "limit_reached", null);
            return;
        }

        handle(player, image, player.getGameProfile().getName(), Painting.Type.PAINTING);
    }

    @Override
    public Type<PaintingRegisterPayload> type() {
        return TYPE;
    }
}
