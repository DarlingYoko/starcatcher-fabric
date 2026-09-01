package com.wdiscute.starcatcher.registry;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class EnumArgument<T extends Enum<T>> implements ArgumentType<T>
{
    private static final DynamicCommandExceptionType INVALID_ENUM = new DynamicCommandExceptionType(
            o -> Component.translatable("argument.enum.invalid", o));

    private final Class<T> enumClass;

    private EnumArgument(Class<T> enumClass)
    {
        this.enumClass = enumClass;
    }

    public static <T extends Enum<T>> EnumArgument<T> enumArgument(Class<T> enumClass)
    {
        return new EnumArgument<>(enumClass);
    }

    @Override
    public T parse(StringReader reader) throws com.mojang.brigadier.exceptions.CommandSyntaxException
    {
        String name = reader.readUnquotedString();
        for (T value : enumClass.getEnumConstants())
        {
            if (value.name().equalsIgnoreCase(name)) return value;
        }
        throw INVALID_ENUM.create(name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        for (T value : enumClass.getEnumConstants())
        {
            builder.suggest(value.name().toLowerCase(Locale.ROOT));
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples()
    {
        return Arrays.stream(enumClass.getEnumConstants()).map(v -> v.name().toLowerCase(Locale.ROOT)).toList();
    }
}
