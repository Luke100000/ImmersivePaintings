package net.conczin.immersive_paintings.neoforge;

import java.util.function.Consumer;

import net.conczin.immersive_paintings.Main;
import net.conczin.immersive_paintings.entity.Entities;
import net.conczin.immersive_paintings.item.Items;
import net.conczin.immersive_paintings.network.Network;
import net.conczin.immersive_paintings.network.payload.ImmersivePayload;
import net.conczin.immersive_paintings.util.Utils.RegisterHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Main.MOD_ID)
@EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonNeoForge {
    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        Main.init();
    }

    private static <T> void registerHelper(RegisterEvent event, Registry<T> register, Consumer<RegisterHelper<T>> consumer) {
        event.register(
            register.key(),
            registry -> consumer.accept(registry::register)
        );
    }

    @SubscribeEvent
    public static void registerEntities(RegisterEvent event) {
        registerHelper(event, BuiltInRegistries.ENTITY_TYPE, Entities::registerEntities);
        registerHelper(event, NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Entities::registerEntitySerializers);
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        registerHelper(event, BuiltInRegistries.ITEM, Items::registerItems);
        registerHelper(event, BuiltInRegistries.CREATIVE_MODE_TAB, Items::registerCreativeTab);
    }

    @SubscribeEvent
    public static void registerNetwork(final RegisterPayloadHandlersEvent event) {
        Network.register(usingRegistrar(event.registrar("1")));
        Network.registerSender(PacketDistributor::sendToPlayer);
    }

    public static Network.Registrar usingRegistrar(final PayloadRegistrar registrar) {
        return new Network.Registrar() {
            @Override
            public <T extends ImmersivePayload> void register(ImmersivePayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean isServer) {
                if (isServer) {
                    registrar.playToServer(type, codec, (payload, ctx) -> payload.handle(ctx.player()));
                } else {
                    registrar.playToClient(type, codec, (payload, ctx) -> payload.handle(ctx.player()));
                }
            }
        };
    }
}