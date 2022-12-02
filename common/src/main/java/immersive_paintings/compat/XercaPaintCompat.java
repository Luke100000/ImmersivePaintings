package immersive_paintings.compat;

import immersive_paintings.Main;
import immersive_paintings.entity.ImmersivePaintingEntity;
import immersive_paintings.network.LazyNetworkManager;
import immersive_paintings.network.c2s.PaintingModifyRequest;
import immersive_paintings.network.c2s.RegisterPaintingRequest;
import immersive_paintings.network.c2s.UploadPaintingRequest;
import immersive_paintings.resources.Painting;
import immersive_paintings.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class XercaPaintCompat {
    public static boolean interactWithPainting(ImmersivePaintingEntity painting, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Identifier id = Registry.ITEM.getId(stack.getItem());
        if (id.getNamespace().equals("xercapaint")) {
            int w = 0, h = 0;
            if (id.getPath().equals("item_canvas")) {
                w = 16;
                h = 16;
            } else if (id.getPath().equals("item_canvas_large")) {
                w = 32;
                h = 32;
            } else if (id.getPath().equals("item_canvas_long")) {
                w = 32;
                h = 16;
            } else if (id.getPath().equals("item_canvas_tall")) {
                w = 16;
                h = 32;
            }

            NbtCompound nbt = stack.getNbt();
            if (w > 0 && nbt != null && nbt.contains("pixels")) {
                // convert
                int[] pixels = nbt.getIntArray("pixels");
                BufferedImage bufferedImage = new BufferedImage(w, h, TYPE_INT_RGB);
                int x = 0, y = 0;
                for (int n : pixels) {
                    bufferedImage.setRGB(x, y, n);
                    x++;
                    if (x >= w) {
                        x = 0;
                        y++;
                    }
                }

                // title
                String title = nbt.contains("title") ? nbt.getString("title") : nbt.contains("ip_title") ? nbt.getString("ip_title") : ("Unnamed Painting #" + player.getRandom().nextInt(1048576));
                nbt.putString("ip_title", title);

                // upload
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", stream);
                    Utils.processByteArrayInChunks(stream.toByteArray(), (ints, split, splits) -> LazyNetworkManager.sendToServer(new UploadPaintingRequest(ints, split, splits)));
                    LazyNetworkManager.sendToServer(new RegisterPaintingRequest(title, new Painting(null, w / 16, h / 16, 16, false)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // apply
                String name = Utils.escapeString(player.getGameProfile().getName()) + "/" + Utils.escapeString(title);
                Identifier identifier = Main.locate(name);
                painting.setMotive(identifier);
                LazyNetworkManager.sendToServer(new PaintingModifyRequest(painting));

                return true;
            }
        }

        return false;
    }
}
