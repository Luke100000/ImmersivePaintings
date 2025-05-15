package net.conczin.immersive_paintings.network.payload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface ImmersivePayload extends CustomPacketPayload {
    void handle(Player player);
}
