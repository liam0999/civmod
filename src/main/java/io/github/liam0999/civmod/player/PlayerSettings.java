package io.github.liam0999.civmod.player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlayerSettings {
    private final Map<String, PlayerEntry> players = new HashMap<>();
    private final Map<PlayerType, PlayerTypeInfo> playerTypes = new HashMap<>();

    private transient Runnable saveHook;

    public PlayerSettings() {
        playerTypes.put(PlayerType.Neutral, new PlayerTypeInfo(Color.WHITE));
        playerTypes.put(PlayerType.Enemy, new PlayerTypeInfo(Color.YELLOW));

        PlayerTypeInfo ally = new PlayerTypeInfo(Color.GREEN);
        ally.inPlayerList = false;
        playerTypes.put(PlayerType.Ally, ally);
    }

    public void setSaveHook(Runnable hook) {
        this.saveHook = hook;
    }

    public void save() {
        if (saveHook != null) {
            saveHook.run();
        }
    }

    public void afterLoad() {
        for (PlayerType type : PlayerType.values()) {
            playerTypes.computeIfAbsent(type, t -> new PlayerTypeInfo(Color.WHITE));
        }
    }

    public PlayerTypeInfo getPlayerTypeInfo(PlayerType playerType) {
        return playerTypes.get(playerType);
    }

    public PlayerType getPlayerType(String playerName) {
        PlayerEntry entry = players.get(playerName.toLowerCase());
        return entry != null ? entry.type : PlayerType.Neutral;
    }

    public void setPlayerType(String playerName, PlayerType playerType) {
        String key = playerName.toLowerCase();

        if (playerType == PlayerType.Neutral) {
            players.remove(key);
            return;
        }

        PlayerEntry entry = players.get(key);

        if (entry == null) {
            players.put(key, new PlayerEntry(playerName, playerType));
        } else {
            entry.type = playerType;
        }
    }

    public List<String> getPlayers(PlayerType playerType) {
        List<String> result = new ArrayList<>();

        for (PlayerEntry entry : players.values()) {
            if (entry.type == playerType) {
                result.add(entry.name);
            }
        }

        Collections.sort(result);

        return result;
    }

    private static class PlayerEntry {
        public String name;
        public PlayerType type;

        public PlayerEntry(String name, PlayerType type) {
            this.name = name;
            this.type = type;
        }
    }
}
