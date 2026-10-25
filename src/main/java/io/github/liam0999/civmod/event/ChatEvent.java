package io.github.liam0999.civmod.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;

// a listener returning true suppresses the message
public interface ChatEvent {
    Event<ChatEvent> EVENT = EventFactory.createArrayBacked(ChatEvent.class,
        (listeners) -> (component) -> {
            for (ChatEvent listener : listeners) {
                if (listener.onChat(component)) {
                    return true;
                }
            }
            return false;
        });

    boolean onChat(Component component);
}
