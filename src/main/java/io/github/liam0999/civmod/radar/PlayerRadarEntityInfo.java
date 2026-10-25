package io.github.liam0999.civmod.radar;

import io.github.liam0999.civmod.player.PlayerType;

import net.minecraft.world.entity.player.Player;

public class PlayerRadarEntityInfo extends RadarEntityInfo {
    private final PlayerType _playerType;

    public PlayerRadarEntityInfo(PlayerType playerType, String name, String iconPath, GroupType groupType) {
        super(Player.class.getCanonicalName() + "." + playerType, name, iconPath, groupType);
        _playerType = playerType;
    }

    public PlayerType getPlayerType() {
        return _playerType;
    }
}
