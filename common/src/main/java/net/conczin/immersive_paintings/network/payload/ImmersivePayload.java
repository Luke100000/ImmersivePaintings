package net.conczin.immersive_paintings.network.payload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface ImmersivePayload extends CustomPacketPayload {
    interface Runner {
        void run(Runnable runnable);
    }

    void handle(Player player, Runner runnable);
}
