package net.conczin.immersive_paintings.registry;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.conczin.immersive_paintings.network.payload.c2s.PaintingDeletePayload;
import net.conczin.immersive_paintings.util.PaintingArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Command {
    private static final LiteralArgumentBuilder<CommandSourceStack> PAINTING_COMMAND = Commands.literal("immersive_paintings")
        .requires(source -> source.hasPermission(2))
        .then(
            Commands.literal("delete").then(
                Commands.argument("author", PaintingArgumentType.single())
                .executes(context -> {
                    String author = PaintingArgumentType.getAuthor(context, "author");

                    Optional<Map.Entry<ResourceLocation, Painting>> e = ServerPaintingManager.getCustomPaintings(context.getSource().getServer()).entrySet().stream()
                            .filter(entry -> entry.getValue().author().equals(author))
                            .findFirst();

                    if (e.isEmpty())
                        throw PaintingArgumentType.ERROR_INVALID_PLAYER.create(author);

                    // TODO: Option to delete a single painting from an author

                    new PaintingDeletePayload(e.get().getKey(), true).handle(context.getSource().getPlayer(), Runnable::run);
                    context.getSource().sendSuccess(() -> Component.translatable("immersive_paintings.command.success.delete", author), true);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                })
            )
        );

    public static void registerCommands(Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
        consumer.accept(PAINTING_COMMAND);
    }
}
