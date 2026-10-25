package io.github.liam0999.civmod.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;

import java.net.URI;
import java.util.Optional;

public class CoordinateLink {
    public static final Identifier CLICK_ID = Identifier.fromNamespaceAndPath("civmod", "coordinate");
    public static final String MAP_URL = "https://map.edenmc.dev/";
    private static final String MAP_QUERY_URL = "https://edenmc.dev/map";

    public static String mapUrl(String coordinates) {
        String[] parts = coordinates.split(" ");
        return MAP_QUERY_URL + "?x=" + parts[0] + "&z=" + parts[parts.length - 1];
    }

    public static boolean isCoordinateClick(ClickEvent clickEvent) {
        return clickEvent instanceof ClickEvent.Custom custom && CLICK_ID.equals(custom.id());
    }

    public static boolean handleClick(ClickEvent clickEvent, Minecraft minecraft, Screen screen, boolean shift) {
        if (!isCoordinateClick(clickEvent)) {
            return false;
        }

        Optional<String> coordinates = ((ClickEvent.Custom) clickEvent).payload().flatMap(Tag::asString);
        if (coordinates.isPresent()) {
            if (shift) {
                ConfirmLinkScreen.confirmLinkNow(screen, URI.create(mapUrl(coordinates.get())));
            } else {
                minecraft.keyboardHandler.setClipboard(coordinates.get());
            }
        }
        return true;
    }
}
