package net.conczin.immersive_paintings.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.conczin.immersive_paintings.ClientPaintingManager;
import net.conczin.immersive_paintings.Painting;
import net.conczin.immersive_paintings.ServerPaintingManager;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PaintingArgumentType implements ArgumentType<String> {
    public static final DynamicCommandExceptionType ERROR_INVALID_PLAYER = new DynamicCommandExceptionType(p -> Component.translatable("immersive_paintings.command.error.no_player", p));

    public static final SingletonArgumentInfo<PaintingArgumentType> INFO = SingletonArgumentInfo.contextFree(PaintingArgumentType::single);

    public static PaintingArgumentType single() {
        return new PaintingArgumentType();
    }

    public static String getAuthor(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Map<Identifier, Painting> paintings = new HashMap<>();
        if (context.getSource() instanceof CommandSourceStack sourceStack) {
            paintings = ServerPaintingManager.getCustomPaintings(sourceStack.getServer());
        } else if (context.getSource() instanceof ClientSuggestionProvider) {
            paintings = ClientPaintingManager.getPaintings();
        }

        List<String> authors = paintings.values().stream()
                .filter(p -> !p.is(Painting.Type.DATAPACK))
                .map(Painting::author)
                .distinct()
                .toList();

        if (authors.isEmpty())
            return Suggestions.empty();

        return SharedSuggestionProvider.suggest(authors, builder);
    }
}
