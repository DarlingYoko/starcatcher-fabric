package com.wdiscute.libtooltips;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In-repo replacement for wd's original {@code libtooltips} (a thin wrapper around Ember's
 * Text API). Ember's Text API's markup pipeline is built around its own ImmersiveMessage/
 * TextSpan renderer, not vanilla {@link Component}, so it doesn't actually produce what this
 * mod's tooltip/name code needs — see FABRIC_PORT_PLAN.md §7bis.1. Since every real tag
 * processor this mod registers already has its own self-contained logic (the gradient math in
 * {@code SCTooltipGradient}/{@code SCLegendary}), only the small "resolve a markdown-tagged
 * string into a styled Component" framework piece needed reimplementing.
 */
public class Tooltips
{
    private static final Pattern TAG = Pattern.compile("<(\\w+)>(.*?)</\\1>", Pattern.DOTALL);

    @FunctionalInterface
    public interface TagProcessor
    {
        MutableComponent process(String text, ItemStack stack, Entity entity);
    }

    private static final Map<String, TagProcessor> PROCESSORS = new HashMap<>();

    public static void registerProcessor(String tagName, TagProcessor processor)
    {
        PROCESSORS.put(tagName, processor);
    }

    public static MutableComponent resolveTagsToComponent(String markup)
    {
        return resolveTagsToComponent(markup, ItemStack.EMPTY, null);
    }

    public static MutableComponent resolveTagsToComponent(String markup, ItemStack stack, Entity entity)
    {
        MutableComponent result = Component.empty();
        Matcher matcher = TAG.matcher(markup);

        int last = 0;
        while (matcher.find())
        {
            if (matcher.start() > last)
                result.append(Component.literal(markup.substring(last, matcher.start())));

            String tagName = matcher.group(1);
            String inner = matcher.group(2);
            TagProcessor processor = PROCESSORS.get(tagName);

            //unregistered tag (e.g. no processors registered yet on a dedicated server) -> plain text passthrough
            result.append(processor != null ? processor.process(inner, stack, entity) : Component.literal(inner));

            last = matcher.end();
        }

        if (last < markup.length())
            result.append(Component.literal(markup.substring(last)));

        return result;
    }

    public static MutableComponent resolveTagsToComponentFromTranslationKey(String key)
    {
        return resolveTagsToComponent(I18n.get(key));
    }
}
