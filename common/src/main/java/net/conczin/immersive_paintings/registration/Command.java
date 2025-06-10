package net.conczin.immersive_paintings.registration;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.conczin.immersive_paintings.Config;

import java.util.function.Consumer;

public class Command {
    private static final LiteralArgumentBuilder<?> PAINTING_COMMAND = net.minecraft.commands.Commands.literal("immersive_paintings")
        .requires(source -> source.hasPermission(4))
        .then(
                net.minecraft.commands.Commands.literal("reload_server_config")
                        .executes(command -> {
                            Config.setInstance();
                            return 1;
                        })
        );

    public static void registerCommands(Consumer<LiteralArgumentBuilder<?>> consumer) {
        consumer.accept(PAINTING_COMMAND);
    }
}
