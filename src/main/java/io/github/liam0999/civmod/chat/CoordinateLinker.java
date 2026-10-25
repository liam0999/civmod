package io.github.liam0999.civmod.chat;

import io.github.liam0999.civmod.CivMod;
import io.github.liam0999.civmod.event.ChatEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CoordinateLinker {
    private static final String NEW_LINE = String.valueOf((char) 10);

    private static boolean reportedSuppressFailure;
    private static boolean reportedDecorateFailure;

    private CoordinateLinker() {
    }

    public static boolean shouldSuppress(Component message) {
        try {
            return ChatEvent.EVENT.invoker().onChat(message);
        } catch (Throwable t) {
            if (!reportedSuppressFailure) {
                reportedSuppressFailure = true;
                CivMod.LOGGER.error("Chat filter failed; messages will no longer be filtered", t);
            }
            return false;
        }
    }

    public static Component decorate(Component original) {
        try {
            if (shouldSuppress(original)) {
                return original;
            }
            return addLinks(original);
        } catch (Throwable t) {
            if (!reportedDecorateFailure) {
                reportedDecorateFailure = true;
                CivMod.LOGGER.error("Coordinate linking failed; chat will be left unmodified", t);
            }
            return original;
        }
    }

    private static Component addLinks(Component original) {
        List<Component> output = new ArrayList<>();
        boolean linked = false;

        for (Component component : original.toFlatList()) {
            if (!(component.getContents() instanceof PlainTextContents plainTextContents)) {
                output.add(component);
                continue;
            }

            String text = plainTextContents.text();
            List<ChatCoordinate> coordinates = ChatCoordinate.find(text);
            if (coordinates.isEmpty()) {
                output.add(component);
                continue;
            }

            linked = true;
            int lastPos = 0;
            for (ChatCoordinate coordinate : coordinates) {
                output.add(Component.literal(text.substring(lastPos, coordinate.textPosStart())).setStyle(component.getStyle()));
                output.add(link(text.substring(coordinate.textPosStart(), coordinate.textPosEnd()), coordinate, component));
                lastPos = coordinate.textPosEnd();
            }

            output.add(Component.literal(text.substring(lastPos)).setStyle(component.getStyle()));
        }

        if (!linked) {
            return original;
        }

        MutableComponent result = Component.empty();
        result.getSiblings().addAll(output);
        return result;
    }

    private static Component link(String label, ChatCoordinate coordinate, Component source) {
        String copied = coordinate.clipboardText();

        Style style = source.getStyle()
            .withClickEvent(new ClickEvent.Custom(CoordinateLink.CLICK_ID, Optional.of(StringTag.valueOf(copied))))
            .withHoverEvent(new HoverEvent.ShowText(
                Component.literal("Click to copy " + copied + " to clipboard.")
                    .append(Component.literal(NEW_LINE + "Shift click to open in " + CoordinateLink.MAP_URL)
                        .withStyle(ChatFormatting.GRAY))));

        // plain chat carries an explicit white, so treat that as uncolored too
        TextColor color = style.getColor();
        if (color == null || color.equals(TextColor.WHITE)) {
            style = style.withColor(ChatFormatting.YELLOW).withUnderlined(true);
        }

        return Component.literal(label).setStyle(style);
    }
}
