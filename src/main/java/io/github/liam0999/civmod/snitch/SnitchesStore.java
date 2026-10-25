package io.github.liam0999.civmod.snitch;

import io.github.liam0999.civmod.util.WorldPos;

import io.github.liam0999.civmod.snitch.model.*;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.*;

public class SnitchesStore {
	public final @NotNull String server;

	private final HashMap<WorldPos, Snitch> snitches = new HashMap<>(1000);
	private final ConcurrentLinkedQueue<Snitch> queuedDBSnitches = new ConcurrentLinkedQueue<>();

	private @Nullable SnitchSqliteDb db;
	private final Thread dbThread;
	private volatile boolean closed;

	public SnitchesStore(@NotNull String server) {
		this.server = server;

		try {
			db = new SnitchSqliteDb(server);
			for (Snitch snitch : db.selectAllSnitches()) {
				snitches.put(snitch.pos, snitch);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		dbThread = new Thread(this::writeQueuedSnitches, "snitchmod-db-writer");
		dbThread.setDaemon(true);
		dbThread.start();
	}

	private void writeQueuedSnitches() {
		int lastAddedSnitchCount = 0;
		while (!closed) {
			try {
				TimeUnit.MILLISECONDS.sleep(1000);
			} catch (InterruptedException err) {
				Thread.currentThread().interrupt();
				break;
			}

			final long startTime = System.currentTimeMillis();
			List<Snitch> snitches = new ArrayList<>(lastAddedSnitchCount);
			while (true) {
				Snitch newSnitch = queuedDBSnitches.poll();

				if (newSnitch != null) {
					snitches.add(newSnitch);
				}

				if (
					newSnitch == null
					|| snitches.size() >= 1000
					|| System.currentTimeMillis() - startTime >= 1000
				) {
					if (db != null) db.upsertSnitches(snitches);
					lastAddedSnitchCount = snitches.size();
					break;
				}
			}
		}

		drainQueue();
	}

	private void drainQueue() {
		List<Snitch> remaining = new ArrayList<>();
		Snitch snitch;
		while ((snitch = queuedDBSnitches.poll()) != null) {
			remaining.add(snitch);
		}
		if (db != null && !remaining.isEmpty()) db.upsertSnitches(remaining);
	}

	public void close() {
		closed = true;
		dbThread.interrupt();
		try {
			dbThread.join(2000);
		} catch (InterruptedException err) {
			Thread.currentThread().interrupt();
		}
		if (db != null) db.close();
	}

	public @Nullable Snitch getSnitch(@NotNull WorldPos pos) {
		return snitches.get(pos);
	}

	public @Nullable Snitch getSnitch(@NotNull String world, @NotNull BlockPos pos) {
		return snitches.get(new WorldPos(server, world, pos));
	}

	public Collection<Snitch> getAllSnitches() {
		return snitches.values();
	}

	public void updateSnitchesFromJalist(List<JalistEntry> jalist) {
		List<Snitch> jalistSnitches = new ArrayList<>(jalist.size());
		for (JalistEntry entry : jalist) {
			Snitch snitch = snitches.computeIfAbsent(entry.pos, Snitch::new);
			snitch.updateFromJalist(entry);
			jalistSnitches.add(snitch);
		}
		if (db != null) db.upsertSnitches(jalistSnitches);
	}

	public void updateSnitchFromRename(SnitchRename rename) {
		Snitch snitch = snitches.computeIfAbsent(rename.pos, Snitch::new);
		snitch.updateFromRename(rename);
		upsertSnitchToDB(snitch);
	}

	public void updateSnitchFromAlert(SnitchAlert alert) {
		Snitch snitch = snitches.computeIfAbsent(alert.pos, Snitch::new);
		snitch.updateFromAlert(alert);
		upsertSnitchToDB(snitch);
	}

	public void updateSnitchFromCreation(Snitch snitch) {
		snitches.put(snitch.pos, snitch);
		upsertSnitchToDB(snitch);
	}

	public void updateSnitchBroken(SnitchBroken snitchBroken) {
		Snitch snitch = snitches.computeIfAbsent(snitchBroken.pos, Snitch::new);
		snitch.updateFromBroken(snitchBroken);
		upsertSnitchToDB(snitch);
	}

	public @Nullable Snitch updateSnitchGone(@NotNull WorldPos pos) {
		Snitch snitch = snitches.get(pos);
		if (snitch == null) return null;
		snitch.updateGone();
		upsertSnitchToDB(snitch);
		return snitch;
	}

	public @Nullable Snitch updateSnitchNoLongerGone(@NotNull WorldPos pos) {
		Snitch snitch = snitches.get(pos);
		if (snitch == null) return null;
		if (!snitch.isGone()) return null;
		snitch.updateNoLongerGone();
		upsertSnitchToDB(snitch);
		return snitch;
	}

	public int deleteGoneSnitches() {
		List<WorldPos> gone = snitches.values().stream()
			.filter(Snitch::isGone)
			.map(Snitch::getPos)
			.toList();
		for (WorldPos pos : gone) {
			snitches.remove(pos);
			if (db != null) db.deleteSnitch(pos);
		}
		return gone.size();
	}

	public @Nullable Snitch deleteSnitch(@NotNull WorldPos pos) {
		Snitch snitch = snitches.remove(pos);
		if (snitch == null) return null;
		db.deleteSnitch(pos);
		return snitch;
	}

	private void upsertSnitchToDB(Snitch snitch) {
		if (snitch != null) queuedDBSnitches.add(snitch);
	}
}
