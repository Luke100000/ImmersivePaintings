package immersive_paintings;

import immersive_paintings.network.ClientNetworkManager;

public class ClientMain {
    public static void postLoad() {
        //finish the items
        Client.postLoad();

        Main.networkManager = new ClientNetworkManager();
    }
}
