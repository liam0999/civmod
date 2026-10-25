package io.github.liam0999.civmod.radar;

import io.github.liam0999.civmod.player.PlayerType;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.cubemob.MagmaCube;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RadarSettings {
    private final transient List<RadarEntityInfo> entityList;
    private final transient Map<String, RadarEntityInfo> entityMap;
    private final Map<GroupType, Boolean> groups = new HashMap<>();
    private boolean enabled = true;
    private boolean pingsEnabled = true;
    private boolean speedometerEnabled = false;
    private float radarOpacity = 0.5f;
    private float lineOpacity = 1.0f;
    private Color radarColor = new Color(128, 128, 128);
    private float radarSize = 0.35f;
    private int radarDistance = 128;
    private float radarX = 0;
    private float radarY = 0;
    private float iconScale = 0.8f;
    private float fontScale = 1.4f;
    private boolean showPlayerNames = true;
    private boolean showPlayerAvatar = true;
    private boolean showExtraPlayerInfo = true;
    private boolean logPlayerStatus = true;
    private boolean showYLevel = true;
    private boolean useLogScale = false;
    private boolean playerListEnabled = true;
    private float playerListX = 0.5f;
    private float playerListY = 0.9f;
    private List<String> playersExcludedFromLog;
    private List<String> disabledEntities = new ArrayList<>();
    private transient Runnable saveHook;
    private transient boolean isJourneyMapEnabled;
    private transient boolean isVoxelMapEnabled;
    private transient boolean isXaerosEnabled;

    public RadarSettings() {

        entityList = new ArrayList<>();
        entityList.add(new RadarEntityInfo(Bat.class, "Bat", "icons/bat.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Chicken.class, "Chicken", "icons/chicken.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Cow.class, "Cow", "icons/cow/cow.png", GroupType.NEUTRAL));
        entityList.add(new RadarHorseInfo("Horse", "icons/horse/horse_chestnut.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Mule.class, "Mule", "icons/horse/mule.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Donkey.class, "Donkey", "icons/horse/donkey.png", GroupType.NEUTRAL));
        entityList.add(
                new RadarEntityInfo(Llama.class, "Llama", "icons/llama/llama.png", GroupType.NEUTRAL)
                        .addEntity(TraderLlama.class, "icons/llama/llama_trader.png")
        );
        entityList.add(new RadarEntityInfo(MushroomCow.class, "Mooshroom", "icons/cow/mooshroom.png", GroupType.NEUTRAL));
        entityList.add(
                new RadarEntityInfo(Ocelot.class, "Ocelot", "icons/cat/ocelot.png", GroupType.NEUTRAL)
                        .addEntity(Cat.class, "icons/cat/black.png")
        );
        entityList.add(new RadarEntityInfo(Pig.class, "Pig", "icons/pig/pig.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Rabbit.class, "Rabbit", "icons/rabbit/white.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Sheep.class, "Sheep", "icons/sheep/sheep.png", GroupType.NEUTRAL));
        entityList.add(
                new RadarEntityInfo(Squid.class, "Squid", "icons/squid.png", GroupType.NEUTRAL)
                        .addEntity(GlowSquid.class, "icons/squid_glow.png")
        );
        entityList.add(new RadarEntityInfo(Villager.class, "Villager", "icons/villager/villager.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Wolf.class, "Wolf", "icons/wolf/wolf.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Blaze.class, "Blaze", "icons/blaze.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(CaveSpider.class, "Cave Spider", "icons/spider/cave_spider.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Creeper.class, "Creeper", "icons/creeper.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Enderman.class, "Enderman", "icons/enderman/enderman.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Ghast.class, "Ghast", "icons/ghast/ghast.png", GroupType.AGGRESSIVE));
        entityList.add(
                new RadarEntityInfo(Guardian.class, "Guardian", "icons/guardian.png", GroupType.AGGRESSIVE)
                        .addEntity(ElderGuardian.class, "icons/elder_guardian.png")
        );
        entityList.add(new RadarEntityInfo(IronGolem.class, "Iron Golem", "icons/iron_golem.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(MagmaCube.class, "Magma Cube", "icons/slime/magma_cube.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Silverfish.class, "Silverfish", "icons/silverfish.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Skeleton.class, "Skeleton", "icons/skeleton/skeleton.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Slime.class, "Slime", "icons/slime/slime.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(SnowGolem.class, "Snow Golem", "icons/snowman.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Spider.class, "Spider", "icons/spider/spider.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Witch.class, "Witch", "icons/witch.png", GroupType.AGGRESSIVE));
        entityList.add(
                new RadarEntityInfo(Zombie.class, "Zombie", "icons/zombie/zombie.png", GroupType.AGGRESSIVE)
                        .addEntity(Drowned.class, "icons/zombie/drowned.png")
                        .addEntity(Husk.class, "icons/zombie/husk.png")
        );
        entityList.add(new RadarEntityInfo(ItemEntity.class, "Item", "icons/item.png", GroupType.OTHER));
        entityList.add(new RadarEntityInfo(Boat.class, "Boat", "icons/boat.png", GroupType.OTHER));
        entityList.add(new RadarEntityInfo(AbstractMinecart.class, "Minecart", "icons/minecart.png", GroupType.OTHER));
        entityList.add(new PlayerRadarEntityInfo(PlayerType.Neutral, "Player (Neutral)", "icons/player.png", GroupType.OTHER));
        entityList.add(new PlayerRadarEntityInfo(PlayerType.Ally, "Player (Ally)", "icons/player.png", GroupType.OTHER));
        entityList.add(new PlayerRadarEntityInfo(PlayerType.Enemy, "Player (Enemy)", "icons/player.png", GroupType.OTHER));
        entityList.add(new RadarEntityInfo(PolarBear.class, "Polar Bear", "icons/bear/polarbear.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Shulker.class, "Shulker", "icons/shulker/shulker.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Stray.class, "Stray", "icons/skeleton/stray.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(ExperienceOrb.class, "XP Orb", "icons/xp_orb.png", GroupType.OTHER));
        entityList.add(new RadarEntityInfo(WitherBoss.class, "Wither", "icons/wither/wither.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(WitherSkeleton.class, "Wither Skeleton", "icons/skeleton/wither_skeleton.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Parrot.class, "Parrot", "icons/parrot/parrot.png", GroupType.NEUTRAL));

        entityList.add(
                new RadarEntityInfo(Evoker.class, "Illager", "icons/illager/evoker.png", GroupType.AGGRESSIVE)
                        .addEntity(Illusioner.class, "icons/illager/illusioner.png")
                        .addEntity(Vex.class, "icons/illager/vex.png")
                        .addEntity(Vindicator.class, "icons/illager/vindicator.png")
                        .addEntity(Pillager.class, "icons/illager/pillager.png")
                        .addEntity(Ravager.class, "icons/illager/ravager.png")
        );

        entityList.add(new RadarEntityInfo(Axolotl.class, "Axolotl", "icons/axolotl.png", GroupType.NEUTRAL));
        entityList.add(
                new RadarEntityInfo(Salmon.class, "Fish", "icons/salmon.png", GroupType.NEUTRAL)
                        .addEntity(Cod.class, "icons/cod.png")
                        .addEntity(Pufferfish.class, "icons/pufferfish.png")
                        .addEntity(TropicalFish.class, "icons/tropical_fish.png")
        );
        entityList.add(new RadarEntityInfo(Fox.class, "Fox", "icons/fox.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Strider.class, "Strider", "icons/strider.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Turtle.class, "Turtle", "icons/turtle.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Turtle.class, "Trader", "icons/wandering_trader.png", GroupType.NEUTRAL));

        entityList.add(new RadarEntityInfo(Bee.class, "Bee", "icons/bee.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Dolphin.class, "Dolphin", "icons/dolphin.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Goat.class, "Goat", "icons/goat.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Panda.class, "Panda", "icons/panda.png", GroupType.NEUTRAL));
        entityList.add(
                new RadarEntityInfo(Piglin.class, "Piglin", "icons/piglin.png", GroupType.NEUTRAL)
                        .addEntity(ZombifiedPiglin.class, "icons/zombie_pigman.png")
        );

        entityList.add(new RadarEntityInfo(Endermite.class, "Endermite", "icons/endermite.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Endermite.class, "Hoglin", "icons/hoglin.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Phantom.class, "Phantom", "icons/phantom.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(PiglinBrute.class, "Piglin Brute", "icons/piglin_brute.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Zoglin.class, "Zoglin", "icons/zoglin.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(ZombieVillager.class, "Zombie Villager", "icons/zombie_villager.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(EnderDragon.class, "Ender Dragon", "icons/dragon.png", GroupType.AGGRESSIVE));

        entityList.add(new RadarEntityInfo(Allay.class, "Allay", "icons/allay.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Frog.class, "Frog", "icons/frog.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Tadpole.class, "Tadpole", "icons/tadpole.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Warden.class, "Warden", "icons/warden.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(ChestBoat.class, "Boat With Chest", "icons/boat_chest.png", GroupType.OTHER));

        entityList.add(new RadarEntityInfo(Camel.class, "Camel", "icons/camel/camel.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Sniffer.class, "Sniffer", "icons/sniffer.png", GroupType.NEUTRAL));

        entityList.add(new RadarEntityInfo(Armadillo.class, "Armadillo", "icons/armadillo.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Bogged.class, "Bogged", "icons/bogged.png", GroupType.AGGRESSIVE));
        entityList.add(new RadarEntityInfo(Breeze.class, "Breeze", "icons/breeze.png", GroupType.AGGRESSIVE));

        entityList.add(new RadarEntityInfo(Creaking.class, "Creaking", "icons/creaking.png", GroupType.AGGRESSIVE));

        entityList.add(new RadarEntityInfo(HappyGhast.class, "Happy Ghast", "icons/ghast/happy_ghast.png", GroupType.NEUTRAL));

        entityList.add(new RadarEntityInfo(CopperGolem.class, "Copper Golem", "icons/copper_golem.png", GroupType.NEUTRAL));

        entityList.add(new RadarEntityInfo(CamelHusk.class, "Camel Husk", "icons/camel/camel_husk.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Nautilus.class, "Nautilus", "icons/nautilus/nautilus.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(ZombieNautilus.class, "Zombie Nautilus", "icons/nautilus/zombie_nautilus.png", GroupType.NEUTRAL));
        entityList.add(new RadarEntityInfo(Parched.class, "Parched", "icons/parched.png", GroupType.AGGRESSIVE));

        entityList.add(new RadarEntityInfo(SulfurCube.class, "Sulfur Cube", "icons/slime/sulfur_cube.png", GroupType.NEUTRAL));

        entityList.sort(new RadarEntityInfo.EntityComparator());

        entityMap = new HashMap<>();

        for (RadarEntityInfo radarEntityInfo : entityList) radarEntityInfo.addToMap(entityMap);

        groups.put(GroupType.NEUTRAL, true);
        groups.put(GroupType.AGGRESSIVE, true);
        groups.put(GroupType.OTHER, true);

        playersExcludedFromLog = new ArrayList<>();
        playersExcludedFromLog.add("~BTLP SLOT");
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    public boolean getPingsEnabled() {
        return pingsEnabled;
    }

    public void setPingsEnabled(boolean value) {
        pingsEnabled = value;
    }

    public boolean getSpeedometerEnabled() {
        return speedometerEnabled;
    }

    public void setSpeedometerEnabled(boolean value) {
        speedometerEnabled = value;
    }

    public float getRadarOpacity() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.radarOpacity() : radarOpacity;
    }

    public float getLineOpacity() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.lineOpacity() : lineOpacity;
    }

    public boolean setLineOpacity(float value) {
        if (lineOpacity == value)
            return false;

        lineOpacity = value;

        return true;
    }

    public boolean setRadarOpacity(float value) {
        if (radarOpacity == value)
            return false;

        radarOpacity = value;

        return true;
    }

    public Color getRadarColor() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.radarColor() : radarColor;
    }

    public boolean setRadarColor(Color value) {
        if (Objects.equals(radarColor, value))
            return false;

        radarColor = value;

        return true;
    }

    public float getRadarSize() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.radarSize() : radarSize;
    }

    public boolean setRadarSize(float value) {
        if (radarSize == value)
            return false;

        radarSize = value;

        return true;
    }

    public int getRadarDistance() {
        return radarDistance;
    }

    public boolean setRadarDistance(int value) {
        if (radarDistance == value)
            return false;

        radarDistance = value;

        return true;
    }

    public float getRadarX() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.radarX() : radarX;
    }

    public void setRadarX(float value) {
        radarX = value;
    }

    public float getRadarY() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.radarY() : radarY;
    }

    public void setRadarY(float value) {
        radarY = value;
    }

    public float getIconScale() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.iconScale() : iconScale;
    }

    public boolean setIconScale(float value) {
        if (iconScale == value)
            return false;

        iconScale = value;

        return true;
    }

    public float getFontScale() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.fontScale() : fontScale;
    }

    public boolean setFontScale(float value) {
        if (fontScale == value)
            return false;

        fontScale = value;

        return true;
    }

    public boolean getPlayerListEnabled() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.playerListEnabled() : playerListEnabled;
    }

    public void setPlayerListEnabled(boolean value) {
        playerListEnabled = value;
    }

    public float getPlayerListX() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.playerListX() : playerListX;
    }

    public boolean setPlayerListX(float value) {
        if (playerListX == value) return false;
        playerListX = value;
        return true;
    }

    public float getPlayerListY() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.playerListY() : playerListY;
    }

    public boolean setPlayerListY(float value) {
        if (playerListY == value) return false;
        playerListY = value;
        return true;
    }

    public boolean getShowPlayerAvatar() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.showPlayerAvatar() : showPlayerAvatar;
    }

    public void setShowPlayerAvatar(boolean value) {
        showPlayerAvatar = value;
    }

    public boolean getShowPlayerNames() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.showPlayerNames() : showPlayerNames;
    }

    public void setShowPlayerNames(boolean value) {
        showPlayerNames = value;
    }

    public boolean getShowExtraPlayerInfo() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.showExtraPlayerInfo() : showExtraPlayerInfo;
    }

    public void setShowExtraPlayerInfo(boolean value) {
        showExtraPlayerInfo = value;
    }

    public boolean getLogPlayerStatus() {
        return logPlayerStatus;
    }

    public void setLogPlayerStatus(boolean value) {
        logPlayerStatus = value;
    }

    public boolean getShowYLevel() {
        RadarPreview.Source preview = RadarPreview.active();
        return preview != null ? preview.showYLevel() : showYLevel;
    }

    public void setShowYLevel(boolean value) {
        showYLevel = value;
    }

    public List<RadarEntityInfo> getEntityList() {
        return entityList;
    }

    public RadarEntityInfo getEntity(String name) {
        for (RadarEntityInfo info : entityList) {
            if (info.getName().equalsIgnoreCase(name))
                return info;
        }

        return null;
    }

    public void setEntityEnabled(String name, boolean enabled) {
        getEntity(name).setEnabled(enabled);
    }

    public Identifier getEnabledIcon(Entity entity, PlayerType playerType) {
        String entityClass;

        if (entity instanceof ItemEntity) {
            entityClass = ItemEntity.class.getCanonicalName();
        } else if (entity instanceof Boat) {
            entityClass = Boat.class.getCanonicalName();
        } else if (entity instanceof AbstractMinecart) {
            entityClass = AbstractMinecart.class.getCanonicalName();
        } else if (entity instanceof Player) {
            entityClass = Player.class.getCanonicalName() + "." + playerType;
        } else {
            entityClass = entity.getClass().getCanonicalName();
        }

        var info = entityMap.getOrDefault(entityClass, null);

        return info != null && info.getEnabled() && groups.get(info.getGroupType())
                ? info.getIcon(entity)
                : null;
    }

    public boolean isGroupEnabled(GroupType groupType) {
        return groups.get(groupType);
    }

    public void setGroupEnabled(GroupType groupType, boolean value) {
        groups.put(groupType, value);
    }

    public boolean getIsJourneyMapEnabled() {
        return isJourneyMapEnabled;
    }

    public void setIsJourneyMapEnabled(boolean value) {
        isJourneyMapEnabled = value;
    }

    public boolean getIsVoxelMapEnabled() {
        return isVoxelMapEnabled;
    }

    public void setIsVoxelMapEnabled(boolean value) {
        isVoxelMapEnabled = value;
    }

    public boolean getIsXaerosEnabled() {
        return isXaerosEnabled;
    }

    public void setIsXaerosEnabled(boolean value) {
        isXaerosEnabled = value;
    }

    public List<String> getPlayersExcludedFromLog() {
        return playersExcludedFromLog;
    }

    public void setPlayersExcludedFromLog(List<String> value) {
        playersExcludedFromLog = value;

        playersExcludedFromLog.replaceAll(String::toUpperCase);
    }

    public boolean isPlayerExcluded(String playerName) {
        String upperPlayerName = playerName.toUpperCase();

        for (String p : playersExcludedFromLog) {
            if (upperPlayerName.startsWith(p)) {
                return true;
            }
        }

        return false;
    }

    public void setSaveHook(Runnable hook) {
        this.saveHook = hook;
    }

    public void save() {
        if (saveHook != null) {
            saveHook.run();
        }
    }

    public void beforeSave() {
        disabledEntities = new ArrayList<>();
        for (RadarEntityInfo info : entityList) {
            if (!info.getEnabled()) {
                disabledEntities.add(info.getName());
            }
        }
    }

    public void afterLoad() {
        if (disabledEntities == null) {
            disabledEntities = new ArrayList<>();
        }
        for (String name : disabledEntities) {
            RadarEntityInfo info = getEntity(name);
            if (info != null) {
                info.setEnabled(false);
            }
        }
        if (groups.isEmpty()) {
            setGroupEnabled(GroupType.NEUTRAL, true);
            setGroupEnabled(GroupType.AGGRESSIVE, true);
            setGroupEnabled(GroupType.OTHER, true);
        }
        if (playersExcludedFromLog == null) {
            playersExcludedFromLog = new ArrayList<>();
        }
    }

    public void setLogScaleEnabled(boolean b) {
        useLogScale = b;
    }

    public boolean getLogScaleEnabled() {
        return useLogScale;
    }

}