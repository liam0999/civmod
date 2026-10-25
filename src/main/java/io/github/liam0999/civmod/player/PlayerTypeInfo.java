package io.github.liam0999.civmod.player;

import java.awt.*;

public class PlayerTypeInfo {
    public Color color;
    public String soundEventName;
    public boolean ping;
    public boolean nameTag;
    public boolean inPlayerList;

    public PlayerTypeInfo(Color color) {
        this.color = color;
        this.soundEventName = "pling";
        this.ping = true;
        this.nameTag = true;
        this.inPlayerList = true;
    }
}
