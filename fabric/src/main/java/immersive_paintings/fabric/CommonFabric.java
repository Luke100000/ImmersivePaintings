package immersive_paintings.fabric;

import immersive_paintings.Entities;
import immersive_paintings.Items;
import immersive_paintings.Messages;
import immersive_paintings.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_paintings.fabric.cobalt.registration.RegistrationImpl;
import net.fabricmc.api.ModInitializer;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Entities.bootstrap();
        Messages.bootstrap();
    }
}

