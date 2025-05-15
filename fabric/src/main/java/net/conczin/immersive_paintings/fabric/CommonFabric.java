package net.conczin.immersive_paintings.fabric;

import java.util.function.Consumer;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.entity.Entities;
import net.conczin.immersive_paintings.fabric.resources.FabricPaintings;
import net.conczin.immersive_paintings.item.Items;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.painting.PlayerManager;
import net.conczin.immersive_paintings.util.Utils.RegisterHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.packs.PackType;

public final class CommonFabric implements ModInitializer {
    private static <T> void registerHelper(Registry<T> register, Consumer<RegisterHelper<T>> consumer) {
        consumer.accept((name, value) -> Registry.register(register, name, value));
    }

    @Override
    public void onInitialize() {
        Main.init();

        // Entities and Serializers
        registerHelper(BuiltInRegistries.ENTITY_TYPE, Entities::registerEntities);
        Entities.registerEntitySerializers((name, serializer) -> EntityDataSerializers.registerSerializer(serializer));

        // Items and Creative Tabs
        registerHelper(BuiltInRegistries.ITEM, Items::registerItems);
        registerHelper(BuiltInRegistries.CREATIVE_MODE_TAB, Items::registerCreativeTab);

        // Network
        Network.register(usingRegistrar());
        Network.registerSender(ServerPlayNetworking::send);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            PlayerManager.playerLoggedOff(handler.player)
        );

        ServerTickEvents.START_SERVER_TICK.register((server) -> LazyNetworkManager.tickServer());

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricPaintings());
    }

    public static Network.Registrar usingRegistrar() {
        return new Network.Registrar() {
            @Override
            public <T extends ImmersivePayload> void register(ImmersivePayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean isServer) {
                if (isServer) {
                    PayloadTypeRegistry.playC2S().register(type, codec);
                    ServerPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> payload.handle(ctx.player()));
                } else {
                    PayloadTypeRegistry.playS2C().register(type, codec);

                    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                        ClientProxy.register(type);
                    }
                }
            }
        };
    }

    // Fabric's APIs are not side-agnostic.
    // We punt this to a separate class file to keep it from being eager-loaded on a server environment.
    private static final class ClientProxy {
        private ClientProxy() {
            throw new RuntimeException("new ClientProxy()");
        }

        public static <T extends ImmersivePayload> void register(ImmersivePayload.Type<T> type) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> payload.handle(ctx.player()));
        }
    }
}
