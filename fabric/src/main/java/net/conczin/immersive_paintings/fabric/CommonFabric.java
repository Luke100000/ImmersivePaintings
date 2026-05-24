package net.conczin.immersive_paintings.fabric;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.registry.*;
import net.conczin.immersive_paintings.resources.PaintingsLoader;
import net.conczin.immersive_paintings.util.PaintingArgumentType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTabs;

public final class CommonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ImmersivePaintings.init();

        // Item and Creative Tabs
        Item.register((id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(group -> Item.getItems().forEach(group::accept));

        // Entity and Serializers
        Entity.register((id, entityType) -> Registry.register(BuiltInRegistries.ENTITY_TYPE, id, entityType));
        Entity.registerSerializers(FabricEntityDataRegistry::register);

        // Network
        Network.register(usingRegistrar());
        NetworkHandler.registerSender(ServerPlayNetworking::send);

        // Commands
        ArgumentTypeRegistry.registerArgumentType(ImmersivePaintings.locate("painting_argument"), PaintingArgumentType.class, PaintingArgumentType.INFO);
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> Command.register(dispatcher::register));

        ServerPlayConnectionEvents.JOIN.register((handler, _, _) -> ServerPaintingManager.playerLoggedIn(handler.player));

        ServerTickEvents.START_SERVER_TICK.register(_ -> LazyNetworkManager.tickServer());

        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(PaintingsLoader.ID, new PaintingsLoader());
    }

    public static Network.Registrar usingRegistrar() {
        return new Network.Registrar() {
            @Override
            public <T extends ImmersivePayload> void register(ImmersivePayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean isServer) {
                if (isServer) {
                    PayloadTypeRegistry.serverboundPlay().register(type, codec);
                    ServerPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> payload.handle(ctx.player(), ctx.server()::execute));
                } else {
                    PayloadTypeRegistry.clientboundPlay().register(type, codec);

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
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, ctx) -> payload.handle(ctx.player(), ctx.client()::execute));
        }
    }
}
