package net.conczin.immersive_paintings.neoforge;

import net.conczin.immersive_paintings.ImmersivePaintings;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.network.LazyNetworkManager;
import net.conczin.immersive_paintings.registry.*;
import net.conczin.immersive_paintings.network.NetworkHandler;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.resources.PaintingsLoader;
import net.conczin.immersive_paintings.util.PaintingArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(ImmersivePaintings.MOD_ID)
@EventBusSubscriber(modid = ImmersivePaintings.MOD_ID)
public final class CommonNeoForge {
    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        ImmersivePaintings.init();
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        // Item
        Item.register((id, item) -> event.register(BuiltInRegistries.ITEM.key(), id, () -> item));

        // Entity
        Entity.register((id, entityType) -> event.register(BuiltInRegistries.ENTITY_TYPE.key(), id, () -> entityType));
        Entity.registerSerializers((id, entitySerializer) -> event.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS.key(), id, () -> entitySerializer));

        // Commands
        ArgumentTypeInfos.registerByClass(PaintingArgumentType.class, PaintingArgumentType.INFO);
        event.register(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.key(), ImmersivePaintings.locate("painting_argument"), () -> PaintingArgumentType.INFO);
    }

    @SubscribeEvent
    public static void registerNetwork(final RegisterPayloadHandlersEvent event) {
        Network.register(usingRegistrar(event.registrar("1")));
        NetworkHandler.registerSender(PacketDistributor::sendToPlayer);
    }

    public static Network.Registrar usingRegistrar(final PayloadRegistrar registrar) {
        return new Network.Registrar() {
            @Override
            public <T extends ImmersivePayload> void register(ImmersivePayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec, boolean isServer) {
                if (isServer) {
                    registrar.playToServer(type, codec, (payload, ctx) -> payload.handle(ctx.player(), ctx::enqueueWork));
                } else {
                    registrar.playToClient(type, codec, (payload, ctx) -> payload.handle(ctx.player(), ctx::enqueueWork));
                }
            }
        };
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        Command.register(event.getDispatcher()::register);
    }

    @SubscribeEvent
    public static void registerCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            Item.getItems().forEach(event::accept);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        LazyNetworkManager.tickServer();
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            ServerPaintingManager.playerLoggedIn((ServerPlayer)event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(PaintingsLoader.ID, new PaintingsLoader());
    }
}