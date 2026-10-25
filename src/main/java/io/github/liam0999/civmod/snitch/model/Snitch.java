package io.github.liam0999.civmod.snitch.model;

import io.github.liam0999.civmod.util.WorldPos;

import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Snitch {
	private final static String JUKEBOX_DB_REPRESENTATION = "jukebox";
	private final static String NOTEBLOCK_DB_REPRESENTATION = "note_block";
	public enum Type {
		JUKEBOX (1000L * 60L * 60L * 24L * 42L, JUKEBOX_DB_REPRESENTATION),
		NOTEBLOCK (1000L * 60L * 60L * 24L * 28L, NOTEBLOCK_DB_REPRESENTATION);

		public final long timer;
		public final String dbRepresentation;
		Type(long timer, String dbRepresentation) {
			this.timer = timer;
			this.dbRepresentation = dbRepresentation;
		}

		@Override
		public String toString() {
			return this.dbRepresentation;
		}
	}

	public boolean maybeRefreshed;
	public final @NotNull WorldPos pos;
	private @Nullable String group;
	private @Nullable Type type;
	private @Nullable String name;
	private long dormantTs;
	private long cullTs;
	private long firstSeenTs;
	private long lastSeenTs;
	private long createdTs;
	private @Nullable UUID createdByUuid;
	private long renamedTs;
	private @Nullable UUID renamedByUuid;
	private long lostJalistAccessTs;
	private long brokenTs;
	private long goneTs;
	private @NotNull HashSet<String> tags = new HashSet<>();
	private @Nullable String notes;

	public Snitch(@NotNull WorldPos pos) {
		this.pos = pos;
	}

	public Snitch(
		@NotNull String server,
		@NotNull String world,
		int x, int y, int z,
		@Nullable String group,
		@Nullable String rawType,
		@Nullable String name,
		long dormantTs,
		long cullTs,
		long firstSeenTs,
		long lastSeenTs,
		long createdTs,
		@Nullable String createdByUuid,
		long renamedTs,
		@Nullable String renamedByUuid,
		long lostJalistAccessTs,
		long brokenTs,
		long goneTs,
		@Nullable String tags,
		@Nullable String notes
	) {
		this.pos = new WorldPos(server, world, x, y, z);
		this.group = group;
		if (rawType != null) {
			String type = rawType.trim().toLowerCase();
			switch (type) {
			case JUKEBOX_DB_REPRESENTATION, "juke_box", "juke box", "logsnitch":
				this.type = Type.JUKEBOX;
				break;
			case NOTEBLOCK_DB_REPRESENTATION, "noteblock", "note block", "snitch":
				this.type = Type.NOTEBLOCK;
				break;
			}
		}
		this.name = name;
		this.dormantTs = dormantTs;
		this.cullTs = cullTs;
		this.firstSeenTs = firstSeenTs;
		this.lastSeenTs = lastSeenTs;
		this.createdTs = createdTs;
		if (createdByUuid != null) this.createdByUuid = UUID.fromString(createdByUuid);
		this.renamedTs = renamedTs;
		if (renamedByUuid != null) this.renamedByUuid = UUID.fromString(renamedByUuid);
		this.lostJalistAccessTs = lostJalistAccessTs;
		this.brokenTs = brokenTs;
		this.goneTs = goneTs;
		this.tags.clear();
		if (tags != null && !tags.isEmpty()) {
			this.tags.addAll(Arrays.asList(tags.split("\n")));
		}
		this.notes = notes;
	}

	public void updateFromCreation(String group, @Nullable Type type, UUID createdByUuid) {
		this.group = group;
		this.createdByUuid = createdByUuid;
		this.type = type;
		createdTs = System.currentTimeMillis();
		firstSeenTs = createdTs;
		lastSeenTs = createdTs;
		if (type != null) {
			this.dormantTs = createdTs + type.timer;
		}
	}

	public void updateFromJalist(JalistEntry jalist) {
		group = jalist.group;
		type = jalist.type;
		name = jalist.name;
		dormantTs = jalist.dormantTs;
		cullTs = jalist.cullTs;
		updateSeen(jalist.ts);
		lostJalistAccessTs = 0;
		this.maybeRefreshed = false;
	}

	public void updateFromRename(SnitchRename rename) {
		this.group = rename.group;
		this.name = rename.snitchName;
		this.renamedTs = rename.ts;
		this.renamedByUuid = rename.clientUuid;
		updateSeen(rename.ts);
	}

	public void updateFromAlert(SnitchAlert alert) {
		group = alert.group;
		name = alert.snitchName;
		if (dormantTs != 0 && dormantTs < alert.ts) dormantTs = 0;
		if (cullTs != 0 && cullTs < alert.ts) cullTs = 0;
		updateSeen(alert.ts);
	}

	public void updateFromBroken(SnitchBroken snitchBroken) {
		this.group = snitchBroken.group;
		dormantTs = 0;
		cullTs = 0;
		updateSeen(snitchBroken.ts);
		brokenTs = snitchBroken.ts;
		goneTs = snitchBroken.ts;
	}

	public void updateGone() {
		goneTs = System.currentTimeMillis();
	}

	public void updateNoLongerGone() {
		goneTs = 0;
	}

	private void updateSeen(long ts) {
		if (firstSeenTs == 0 || firstSeenTs > ts) firstSeenTs = ts;
		if (lastSeenTs < ts) lastSeenTs = ts;
		goneTs = 0;
	}

	public WorldPos getPos() {
		return pos;
	}

	public AABB getRangeAABB() {
		return new AABB(pos).inflate(11);
	}

	@Nullable
	public String getGroup() {
		return group;
	}

	@Nullable
	public Type getType() {
		return type;
	}

	@Nullable
	public String getName() {
		return name;
	}

	public boolean isAlive() {
		if (brokenTs != 0 || goneTs != 0) return false;
		if (dormantTs == 0 && cullTs == 0) return true;
		long now = System.currentTimeMillis();
		return (dormantTs == 0 || now < dormantTs) && (cullTs == 0 || now < cullTs);
	}

	public long getDormantTs() {
		return dormantTs;
	}

	public boolean hasDormantTs() {
		return dormantTs != 0;
	}

	public long getCullTs() {
		return cullTs;
	}

	public boolean hasCullTs() {
		return cullTs != 0;
	}

	public long getFirstSeenTs() {
		return firstSeenTs;
	}

	public long getLastSeenTs() {
		return lastSeenTs;
	}

	public long getCreatedTs() {
		return createdTs;
	}

	public boolean hasCreatedTs() {
		return createdTs != 0;
	}

	public @Nullable UUID getCreatedByUuid() {
		return createdByUuid;
	}

	public long getRenamedTs() {
		return renamedTs;
	}

	public boolean hasRenamedTs() {
		return renamedTs != 0;
	}

	public @Nullable UUID getRenamedByUuid() {
		return renamedByUuid;
	}

	public long getLostJalistAccessTs() {
		return lostJalistAccessTs;
	}

	public boolean haveLostJalistAccess() {
		return lostJalistAccessTs != 0;
	}

	public long getBrokenTs() {
		return brokenTs;
	}

	public boolean wasBroken() {
		return brokenTs != 0;
	}

	public long getGoneTs() {
		return goneTs;
	}

	public boolean isGone() {
		return goneTs != 0;
	}

	public @NotNull HashSet<String> getTags() {
		return tags;
	}

	public @Nullable String getNotes() {
		return notes;
	}
}
