package io.github.liam0999.civmod.snitch;

import io.github.liam0999.civmod.util.ChatFormat;
import io.github.liam0999.civmod.util.Utils;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.liam0999.civmod.CivMod;
import io.github.liam0999.civmod.Keybinds;
import io.github.liam0999.civmod.render.SnitchRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.liam0999.civmod.util.Direction;
import io.github.liam0999.civmod.snitch.model.JalistEntry;
import io.github.liam0999.civmod.snitch.model.Snitch;
import io.github.liam0999.civmod.snitch.model.SnitchAlert;
import io.github.liam0999.civmod.snitch.model.SnitchCreatedChatParser;
import io.github.liam0999.civmod.snitch.model.SnitchFieldPreview;
import io.github.liam0999.civmod.snitch.model.SnitchRename;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public final class SnitchManager {
	protected static Minecraft mc = Minecraft.getInstance();
	private static SnitchManager INSTANCE;

	private final SnitchSettings settings;

	@Nullable
	public SnitchFieldPreview snitchFieldToPreview = null;
	@Nullable
	public Snitch lastBrokenSnitch = null;

	@Nullable
	private SnitchesStore store;

	public static SnitchManager getMod() {
		return INSTANCE;
	}

	public SnitchManager(SnitchSettings settings) {
		if (INSTANCE != null) throw new IllegalStateException("Constructor called twice");
		INSTANCE = this;
		this.settings = settings;
	}

	public SnitchSettings settings() {
		return settings;
	}

	public @Nullable String getCurrentServer() {
		final ServerData currentServer = mc.getCurrentServer();
		if (currentServer == null) return null;
		return currentServer.ip;
	}

	public String getCurrentWorld() {
		if (mc.level == null) return null;
		String dimension = mc.level.dimension().identifier().getPath();
		// civ server world names as they occur in snitches are different from dimension names
		return switch (dimension) {
			case "overworld" -> "world";
			case "the_nether" -> "world_nether";
			case "the_end" -> "world_the_end";
			default -> dimension;
		};
	}

	public UUID getClientUuid() {
		if (mc.player == null) return null;
		return mc.player.getUUID();
	}

	public @Nullable SnitchesStore getStore() {
		String server = getCurrentServer();
		if (store != null && !store.server.equals(server)) {
			store.close();
			store = null;
		}
		if (store == null && server != null) {
			store = new SnitchesStore(server);
		}
		return store;
	}

	public void handleConnectedToServer() {
		getStore();
	}

	public void handleDisconnectedFromServer() {
		if (store != null) store.close();
		store = null;
	}

	public void handleTick() {
		// shares L with vanilla advancements, so stay silent in singleplayer where there is no store
		while (Keybinds.SNITCH_LIST.consumeClick()) {
			if (store != null) store.close();
			store = null;
			if (getStore() != null) {
				logToChat(Component.literal("Reloaded the snitch database"));
			}
		}

		JalistAutoPaginator.getInstance().tick();
		while (Keybinds.JALIST_AUTO.consumeClick()) {
			JalistAutoPaginator.getInstance().startAutoPagination();
		}

		while (Keybinds.SNITCH_TOGGLE_GONE.consumeClick()) {
			if (!settings.isRangeOverlayVisible()) {
				break;
			}

			Optional<Snitch> optSnitch = getMod().streamNearbySnitches(mc.player.position(), 260)
				.filter(s -> !s.wasBroken())
				.limit(100)
				.filter(s -> Utils.playerIsLookingAtSnitch(mc.player, s))
				.findFirst();
			if (optSnitch.isEmpty()) {
				logToChat(Component.literal("Error: you must be looking at a snitch to change its status"));
				break;
			}

			Snitch snitch = optSnitch.get();
			String formattedSnitch = String.format(
				"\"%s\" on [%s] at %d %d %d",
				snitch.getName() != null ? snitch.getName() : "",
				snitch.getGroup(),
				snitch.getPos().getX(),
				snitch.getPos().getY(),
				snitch.getPos().getZ()
			);
			if (snitch.isGone()) {
				store.updateSnitchNoLongerGone(snitch.pos);
				logToChat(Component.literal(String.format("Marked snitch %s as alive", formattedSnitch)));
			} else {
				store.updateSnitchGone(snitch.pos);
				logToChat(Component.literal(String.format("Marked snitch %s as gone", formattedSnitch)));
			}
		}

		while (Keybinds.SNITCH_RANGE_OVERLAY.consumeClick()) {
			settings.setRangeOverlayVisible(!settings.isRangeOverlayVisible());
			CivMod.config().save();
			logToChat(
				Component.literal("Snitch range overlay " + (settings.isRangeOverlayVisible() ? "visible" : "hidden"))
			);
		}

		while (Keybinds.SNITCH_PLACEMENT_HELPER.consumeClick()) {
			settings.setPlacementHelperVisible(!settings.isPlacementHelperVisible());
			CivMod.config().save();

			if (settings.isPlacementHelperVisible()) {
				snitchFieldToPreview = null;
			}

			logToChat(
				Component.literal("Snitch placement helper " + (settings.isPlacementHelperVisible() ? "visible" : "hidden"))
			);
		}

		while (Keybinds.SNITCH_FIELD_PREVIEW.consumeClick()) {
			Optional<Snitch> optNearestSnitch = streamNearbySnitches(mc.player.position(), 2*23)
				.filter(Snitch::isAlive)
				.findFirst();
			if (optNearestSnitch.isEmpty()) {
				snitchFieldToPreview = null;
				logToChat(Component.literal("No nearby alive snitches to base a field preview on"));
				break;
			}
			Snitch nearestSnitch = optNearestSnitch.get();

			if (settings.isPlacementHelperVisible()) {
				settings.setPlacementHelperVisible(false);
			}

			SnitchFieldPreview newSnitchFieldToPreview = new SnitchFieldPreview(
				nearestSnitch, Direction.ofPlayer(mc.player));
			if (
				snitchFieldToPreview != null
				&& newSnitchFieldToPreview.equals(snitchFieldToPreview)
			) {
				logToChat(Component.literal("Turning off the snitch field preview"));
				snitchFieldToPreview = null;
				break;
			}

			snitchFieldToPreview = newSnitchFieldToPreview;
			logToChat(Component.literal("Showing a snitch field preview in the direction you're looking"));
		}
		var mc = Minecraft.getInstance();
		if (mc.gui.screen() instanceof AbstractContainerScreen<?> containerScreen) {
			String title = containerScreen.getTitle().getString();
			if ((title.toLowerCase().contains("snitches") || title.contains("JukeAlert"))
					&& InputConstants.isKeyDown(InputConstants.KEY_J)) {

				if (!JalistAutoPaginator.getInstance().isActive()) {
					JalistAutoPaginator.getInstance().startAutoPagination();
				}
			}
		}
	}

	public boolean handleChat(Component message) {
		getStore();
		if (store == null) return false;

		SnitchAlert snitchAlert = SnitchAlert.fromChat(message, store.server, getCurrentWorld());
		if (snitchAlert != null) {
			store.updateSnitchFromAlert(snitchAlert);
			return false;
		}

		SnitchRename snitchRename = SnitchRename.fromChat(message, store.server, getCurrentWorld(), getClientUuid());
		if (snitchRename != null) {
			store.updateSnitchFromRename(snitchRename);
			return false;
		}

		Snitch snitchCreated = SnitchCreatedChatParser.fromChat(message, store.server, getCurrentWorld(), getClientUuid());
		if (snitchCreated != null) {
			Snitch alreadyExistingSnitch = store.getSnitch(snitchCreated.pos);
			store.updateSnitchFromCreation(snitchCreated);
			if (
				alreadyExistingSnitch != null
				&& alreadyExistingSnitch.getName() != null
				&& !alreadyExistingSnitch.getName().equals("")
			) {
				mc.player.connection.sendCommand(
					String.format(
						"janameat %d %d %d %s",
						alreadyExistingSnitch.pos.getX(),
						alreadyExistingSnitch.pos.getY(),
						alreadyExistingSnitch.pos.getZ(),
						alreadyExistingSnitch.getName()
					)
				);
				logToChat(Component.literal(String.format("Named the replaced snitch \"%s\"", alreadyExistingSnitch.getName())));
			}

			if (
				snitchFieldToPreview != null
				&& snitchFieldToPreview.field().pos.equals(snitchCreated.pos)
			) {
				if (settings.isPlacementHelperVisible()) {
					settings.setPlacementHelperVisible(false);
				}

				snitchFieldToPreview = new SnitchFieldPreview(
					snitchCreated, snitchFieldToPreview.direction());

				logToChat(Component.literal("Showing an inferred snitch field preview"));
			}

			return false;
		}

		return false;
	}

	public void handleWindowItems(List<ItemStack> stacks) {
		getStore();
		if (store == null) return;
		List<JalistEntry> jalistEntries = new ArrayList<JalistEntry>(stacks.size());
		for (int i = 0; i < stacks.size(); i++) {
			ItemStack stack = stacks.get(i);
			try {
				JalistEntry jalistEntry = JalistEntry.fromStack(stack, store.server);
				if (jalistEntry != null) {
					jalistEntries.add(jalistEntry);
					if (JalistAutoPaginator.getInstance().isActive()) {
						JalistAutoPaginator.getInstance().addSnitchEntry(jalistEntry.group, jalistEntry.dormantTs, jalistEntry.cullTs);
					}
				}
			} catch (Throwable e) {
				System.err.println("Failed parsing jalist stack " + i + " " + stack);
				e.printStackTrace();
				logToChat(Component.literal("Failed reading snitch " + i + " on JAList page"));
			}
		}
		store.updateSnitchesFromJalist(jalistEntries);
		if (jalistEntries.size() > 0) {
			if (JalistAutoPaginator.getInstance().isActive()) {
				JalistAutoPaginator.getInstance().onJalistPageLoaded(stacks, jalistEntries.size());
			} else {
				logToChat(Component.literal("Found " + jalistEntries.size() + " snitches on JAList page"));
			}
		}
	}

	public void handleRenderBlockOverlay(PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		SnitchRenderer.renderOverlays(poseStack, collector, camera);
	}

	public Stream<Snitch> streamNearbySnitches(Vec3 playerPos, int distance) {
		getStore();
		if (store == null) return Stream.empty();
		String currentWorld = getCurrentWorld();
		return store.getAllSnitches().stream()
			.filter(s -> s.getPos().getCenter().distanceTo(playerPos) < distance)
			.filter(s -> currentWorld != null && currentWorld.equals(s.getPos().getWorld()))
			.sorted(Comparator.comparing(s -> s.getPos().getCenter().distanceTo(playerPos)));
	}

	public void clearGoneSnitches() {
		getStore();
		if (store == null) {
			logToChat(Component.literal("No snitch database loaded"));
			return;
		}
		int cleared = store.deleteGoneSnitches();
		logToChat(Component.literal(cleared == 0
			? "No snitches are marked as gone"
			: String.format("Cleared %d snitch%s marked as gone", cleared, cleared == 1 ? "" : "es")));
	}

	private void logToChat(Component msg) {
		mc.gui.hud.getChat().addClientSystemMessage(ChatFormat.prefixed(msg));
	}
}
