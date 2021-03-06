package immersive_paintings;

import immersive_paintings.cobalt.registration.Registration;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

import static immersive_paintings.Items.PAINTING;

public class ItemGroups {
    @SuppressWarnings("Convert2MethodRef")
    public static final ItemGroup GROUP = Registration.ObjectBuilders.ItemGroups.create(
            new Identifier(Main.MOD_ID, Main.MOD_ID + "_tab"),
            () -> PAINTING.getDefaultStack()
    );
}
