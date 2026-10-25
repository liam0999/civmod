package io.github.liam0999.civmod.render;

import com.mojang.blaze3d.vertex.*;
import io.github.liam0999.civmod.util.Utils;
import io.github.liam0999.civmod.util.Utils.Color;
import io.github.liam0999.civmod.snitch.model.Snitch;
import io.github.liam0999.civmod.snitch.model.SnitchFieldPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.github.liam0999.civmod.snitch.SnitchManager.getMod;

public class SnitchRenderer {
	private final static Minecraft mc = Minecraft.getInstance();

	private final static Color BLACK = new Color(0x333333);
	private final static Color WHITE = new Color(0xEEEEEE);
	private final static Color RED = new Color(0xEE4056);
	private final static Color GREEN = new Color(0x28CC52);
	private final static Color BLUE = new Color(0x00B2FF);
	private final static Color YELLOW = new Color(0xEED840);
	private final static Color ORANGE = new Color(0xEE8140);
	private final static Color PINK = new Color(0xFC66CC);
    private static PoseStack eventPoseStack = null;

    private static final float GUIDE_THICKNESS = 2;
    private static final float RANGE_OUTLINE_SCALE = 0.4f;
    private static final float RANGE_OUTLINE_ALPHA = 0.6f;
	// shrink and expand must stay disjoint, and jitter separates boxes sharing a face, or they z-fight
    private static final double BOX_EXPAND = 0.01;
    private static final double BOX_SHRINK = 0.04;
    private static final int JITTER_STEPS = 8;
    private static final double JITTER_STEP = 0.002;
    private static final float TEXT_SCALE = 0.005f;
    private static final float BLOCK_OUTLINE_SCALE = 0.8f;

    private static SubmitNodeCollector collector = null;
    private static CameraRenderState cameraState = null;

	public static void renderOverlays(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		if (mc.player == null) return;
		if (mc.level == null) return;

		if (mc.gui.hud.isHidden()) return;

		eventPoseStack = poseStack;
		collector = submitNodeCollector;
		cameraState = camera;

		Vec3 camPos = camera.pos;
		poseStack.pushPose();
		poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

		if (getMod().settings().isRangeOverlayVisible()) {
			int fieldDist = 260;
			getMod().streamNearbySnitches(mc.player.position(), fieldDist)
				.filter(s -> !s.wasBroken())
				.limit(400)
				.forEach(SnitchRenderer::renderSnitch);
		}

		if (getMod().settings().isPlacementHelperVisible()) {
			int placeHelperDist = 50;
			getMod().streamNearbySnitches(mc.player.position(), placeHelperDist)
				.filter(Snitch::isAlive)
				.limit(10)
				.forEach(SnitchRenderer::renderPlacementHelper);
		}

		if (getMod().snitchFieldToPreview != null) {
			renderSnitchFieldPreview(getMod().snitchFieldToPreview);
		}

		poseStack.popPose();
		eventPoseStack = null;
		collector = null;
		cameraState = null;
	}

	private static double boxInflation(BlockPos pos, boolean playerInRange) {
		int step = Math.floorMod(pos.getX() * 31 + pos.getY() * 17 + pos.getZ() * 13, JITTER_STEPS);
		double jitter = step * JITTER_STEP;
		return playerInRange ? -(BOX_SHRINK + jitter) : (BOX_EXPAND + jitter);
	}

	private static void renderSnitchFieldPreview(SnitchFieldPreview preview) {
        final Color color = PINK;
        final float boxAlpha = 0.2f;
        final float lineAlpha = 1;
        final float lineWidth = lineWidth(RANGE_OUTLINE_SCALE);
        final int blockHlDist = 64;
		final AABB range = preview.field().getRangeAABB();
		final boolean playerInRange = range.contains(mc.player.position());
		AABB rangeBox = range.inflate(boxInflation(preview.field().pos, playerInRange));

        renderFilledBox(rangeBox, color, boxAlpha, SnitchRenderTypes.QUADS);
        renderBoxOutline(rangeBox, color, lineAlpha * RANGE_OUTLINE_ALPHA, lineWidth, SnitchRenderTypes.LINES);
        renderBoxGuides(rangeBox, color, lineAlpha, GUIDE_THICKNESS, SnitchRenderTypes.QUADS);

		if (preview.field().pos.distSqr(mc.player.blockPosition()) < blockHlDist * blockHlDist) {
            final AABB blockBox = new AABB(preview.field().pos).inflate(.01);
            renderBoxOutline(blockBox, color, lineAlpha, lineWidth(BLOCK_OUTLINE_SCALE), SnitchRenderTypes.SEE_THROUGH_LINES);
		}
	}

	private static void renderSnitch(Snitch snitch) {
		final AABB range = snitch.getRangeAABB();
		final boolean playerInRange = range.contains(mc.player.position());
		AABB rangeBox = range.inflate(boxInflation(snitch.pos, playerInRange));
		AABB outlineBox = rangeBox;
		if (playerInRange) {
			snitch.maybeRefreshed = true;
		}

		enum SnitchLiveliness {
			BROKEN(RED),
			GONE(RED),
			LONG_TIME_NOT_SEEN(RED),
			CULLED(RED),
			WILL_CULL(ORANGE),
			WILL_CULL_MAYBE_REFRESHED(GREEN),
			DORMANT_NOW(ORANGE),
			DORMANT_NOW_MAYBE_REFRESHED(GREEN),
			DORMANT_SOON(ORANGE),
			DORMANT_SOON_MAYBE_REFRESHED(GREEN),
			DORMANT_SOONISH(YELLOW),
			DORMANT_SOONISH_MAYBE_REFRESHED(GREEN),
			DORMANT_EVENTUALLY(GREEN);

			private final Color color;

			SnitchLiveliness(Color color) {
				this.color = color;
			}
		}

		long now = System.currentTimeMillis();
		long snitchTimer = snitch.getType() != null ? snitch.getType().timer : Snitch.Type.NOTEBLOCK.timer;
		SnitchLiveliness snitchLiveliness = SnitchLiveliness.DORMANT_EVENTUALLY;
		if (snitch.wasBroken()) {
			snitchLiveliness = SnitchLiveliness.BROKEN;
		} else if (snitch.isGone()) {
			snitchLiveliness = SnitchLiveliness.GONE;
		} else if (snitch.getLastSeenTs() != 0 && now - snitch.getLastSeenTs() > snitchTimer) {
			snitchLiveliness = SnitchLiveliness.LONG_TIME_NOT_SEEN;
		} else if (snitch.hasCullTs() && snitch.getCullTs() < now) {
			snitchLiveliness = SnitchLiveliness.CULLED;
		} else if (snitch.hasCullTs() && snitch.getCullTs() > now) {
			snitchLiveliness = snitch.maybeRefreshed
				? SnitchLiveliness.WILL_CULL_MAYBE_REFRESHED
				: SnitchLiveliness.WILL_CULL;
		} else if (snitch.hasDormantTs() && snitch.getDormantTs() < now) {
			snitchLiveliness = snitch.maybeRefreshed
				? SnitchLiveliness.DORMANT_NOW_MAYBE_REFRESHED
				: SnitchLiveliness.DORMANT_NOW;
		} else if (snitch.hasDormantTs() && snitch.getDormantTs() > now) {
			final long goodThreshold = 1000L * 60L * 60L * 24L * 7L;
			final long badThreshold = 1000L * 60L * 60L * 24L * 3L;
			final long delta = snitch.getDormantTs() - now;
			if (delta >= goodThreshold) {
			} else if (delta >= badThreshold) {
				snitchLiveliness = snitch.maybeRefreshed
					? SnitchLiveliness.DORMANT_SOONISH_MAYBE_REFRESHED
					: SnitchLiveliness.DORMANT_SOONISH;
			} else {
				snitchLiveliness = snitch.maybeRefreshed
					? SnitchLiveliness.DORMANT_SOON_MAYBE_REFRESHED
					: SnitchLiveliness.DORMANT_SOON;
			}
		}

		float boxAlpha = 0.2f;
		float lineAlpha = 1;
		if (snitch.isGone()) {
			boxAlpha = 0f;
			lineAlpha = 0.5f;
		}

        final float lineWidth = lineWidth(RANGE_OUTLINE_SCALE);

        renderFilledBox(rangeBox, snitchLiveliness.color, boxAlpha, SnitchRenderTypes.QUADS);
        if (!snitch.isGone()) {
            renderBoxOutline(outlineBox, snitchLiveliness.color, lineAlpha * RANGE_OUTLINE_ALPHA, lineWidth, SnitchRenderTypes.LINES);
        }

		final int blockHlDist = 64;
		if (snitch.pos.distSqr(mc.player.blockPosition()) < blockHlDist * blockHlDist) {
            {
                final AABB blockBox = new AABB(snitch.pos).inflate(.01);
                Color boxOutlineColor = snitchLiveliness.color;
                if (
                        getMod().snitchFieldToPreview != null
                                && getMod().snitchFieldToPreview.source().equals(snitch)
                ) {
                    boxOutlineColor = PINK;
                }
                renderBoxOutline(blockBox, boxOutlineColor, lineAlpha, lineWidth(BLOCK_OUTLINE_SCALE), SnitchRenderTypes.SEE_THROUGH_LINES);

                Color boxFillColor = snitch.isGone() ? BLACK : snitchLiveliness.color;
                renderFilledBox(blockBox, boxFillColor, boxAlpha, SnitchRenderTypes.SEE_THROUGH_QUADS);
            }
		}

		record ColoredComponent(Component text, Color color) {}

		List<ColoredComponent> linesToRender = new ArrayList<>(3);
		boolean playerLookingAtSnitch = Utils.playerIsLookingAtSnitch(mc.player, snitch);
		Vec3 eyePosition = new Vec3(mc.player.position().x, mc.player.position().y + 1, mc.player.position().z);
		if (
			(
				(!snitch.isGone() && playerInRange)
				|| playerLookingAtSnitch
			)
			&& (
				eyePosition.y != snitch.pos.getY()
				|| eyePosition.distanceTo(snitch.pos.getCenter()) > 3
			)
		) {
			String name = snitch.getName();
			if (name != null && !name.isEmpty()) {
				linesToRender.add(new ColoredComponent(Component.literal(name), WHITE));
			}

			String group = snitch.getGroup();
			if (group != null) {
				linesToRender.add(new ColoredComponent(Component.literal(String.format("[%s]", group)), WHITE));
			}

			String lastSeenText = null;
			if (snitch.getLastSeenTs() != 0) {
				lastSeenText = String.format("last seen %s", timestampRelativeText(snitch.getLastSeenTs()));
			}

			String maxSnitchTimerText = "";
			if (playerLookingAtSnitch) {
				maxSnitchTimerText = String.format(" / %s", durationToText(snitchTimer));
			}

			String livelinessText = null;
			switch (snitchLiveliness) {
				case BROKEN:
					livelinessText = "broken " + timestampRelativeText(snitch.getBrokenTs());
					break;
				case GONE:
					livelinessText = "gone " + timestampRelativeText(snitch.getGoneTs());
					break;
				case LONG_TIME_NOT_SEEN:
					livelinessText = lastSeenText;
					break;
				case CULLED:
					livelinessText = "culled " + timestampRelativeText(snitch.getCullTs());
					break;
				case WILL_CULL, WILL_CULL_MAYBE_REFRESHED:
					livelinessText = String.format(
						"culls %s%s",
						timestampRelativeText(snitch.getCullTs()),
						maxSnitchTimerText
					);
					break;
				case DORMANT_NOW, DORMANT_NOW_MAYBE_REFRESHED:
					livelinessText = "deactivated " + timestampRelativeText(snitch.getDormantTs());
					break;
				case DORMANT_SOON, DORMANT_SOON_MAYBE_REFRESHED, DORMANT_SOONISH, DORMANT_SOONISH_MAYBE_REFRESHED,
						DORMANT_EVENTUALLY:
					if (snitch.hasDormantTs()) {
						livelinessText = String.format(
							"deactivates %s%s",
							timestampRelativeText(snitch.getDormantTs()),
							maxSnitchTimerText
						);
					}
					break;
			}
			if (livelinessText != null) {
				if (snitch.maybeRefreshed) {
					switch (snitchLiveliness) {
						case WILL_CULL_MAYBE_REFRESHED, DORMANT_NOW_MAYBE_REFRESHED, DORMANT_SOON_MAYBE_REFRESHED,
								DORMANT_SOONISH_MAYBE_REFRESHED, DORMANT_EVENTUALLY:
							livelinessText = livelinessText + " (refreshed?)";
							break;
						default:
					}
				}
				linesToRender.add(new ColoredComponent(Component.literal(livelinessText), snitchLiveliness.color));
			}

			if (playerLookingAtSnitch) {
				linesToRender.add(
						new ColoredComponent(
								Component.literal(
										String.format(
												"%d %d %d",
												snitch.getPos().getX(),
												snitch.getPos().getY(),
												snitch.getPos().getZ()
										)
								),
								WHITE
						)
				);

				if (snitch.getType() != null) {
					linesToRender.add(
						new ColoredComponent(
							Component.literal(StringUtils.capitalize(snitch.getType().toString().replaceAll("_", ""))),
							WHITE
						)
					);
				}

				if (lastSeenText != null && snitchLiveliness != SnitchLiveliness.LONG_TIME_NOT_SEEN) {
					linesToRender.add(new ColoredComponent(Component.literal(lastSeenText), WHITE));
				}
			}
		}

		Vec3 center = snitch.pos.getCenter();
		int offset = -1;
		for (ColoredComponent line : linesToRender) {
			renderTextFacingCamera(line.text, center, offset, 1f, line.color.hex);
			offset += 1;
		}
	}

	private static void renderPlacementHelper(Snitch snitch) {
		final boolean playerInRange = snitch.getRangeAABB().contains(mc.player.position());
		if (playerInRange) return;
		final AABB helperBox = new AABB(snitch.pos).inflate(22.3);

        renderFilledBox(helperBox, BLUE, 0.2f, SnitchRenderTypes.QUADS);
	}

	private static void submitAfterTerrain(RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
		SubmitNodeCollection collection = (SubmitNodeCollection) collector.order(0);
		collection.afterTerrain.submit(
			new CustomFeatureRenderer.Submit(eventPoseStack.last().copy(), renderType, renderer));
	}

	private static float lineWidth(float scale) {
		return mc.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth * scale;
	}

	private static void renderBoxOutline(AABB box, Color color, float alpha, float lineWidth, RenderType renderType) {
		int alphaInt = Math.round(alpha * 255.0f) & 0xFF;
		int colorInt = (alphaInt << 24) | (color.hex & 0x00FFFFFF);
		collector.submitShapeOutline(eventPoseStack, Shapes.create(box), renderType, colorInt, lineWidth, true);
	}

    private static void renderFilledBox(AABB box, Color color, float alpha, RenderType renderType) {
        final float minX = (float) box.minX;
        final float minY = (float) box.minY;
        final float minZ = (float) box.minZ;
        final float maxX = (float) box.maxX;
        final float maxY = (float) box.maxY;
        final float maxZ = (float) box.maxZ;
        final int alphaInt = Math.round(alpha * 255.0f) & 0xFF;
        final int colorInt = (alphaInt << 24) | (color.hex & 0x00FFFFFF);

        submitAfterTerrain(renderType, (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, minX, minY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, minY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, minY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, minX, minY, maxZ).setColor(colorInt);

            vertexConsumer.addVertex(pose, minX, maxY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, minX, maxY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, maxY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, maxY, minZ).setColor(colorInt);

            vertexConsumer.addVertex(pose, minX, minY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, minX, maxY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, maxY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, minY, minZ).setColor(colorInt);

            vertexConsumer.addVertex(pose, minX, minY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, minY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, maxY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, minX, maxY, maxZ).setColor(colorInt);

            vertexConsumer.addVertex(pose, minX, minY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, minX, minY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, minX, maxY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, minX, maxY, minZ).setColor(colorInt);

            vertexConsumer.addVertex(pose, maxX, minY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, maxY, minZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, maxY, maxZ).setColor(colorInt);
            vertexConsumer.addVertex(pose, maxX, minY, maxZ).setColor(colorInt);
        });
    }

	private static void renderBoxGuides(AABB box, Color color, float alpha, float lineWidth, RenderType renderType) {
		Vec3 center = box.getCenter();
		double radius = box.maxX - center.x;

		double t = Math.max(0.01, lineWidth * 0.02);
		double halfT = t / 2.0;

		double cx = center.x;
		double cy = center.y;
		double cz = center.z;

		renderFilledBox(
				new AABB(cx + 1.0, cy - halfT, cz - halfT,
						cx + radius, cy + halfT, cz + halfT),
				color, alpha, renderType
		);

		renderFilledBox(
				new AABB(cx - radius, cy - halfT, cz - halfT,
						cx - 1.0, cy + halfT, cz + halfT),
				color, alpha, renderType
		);

		renderFilledBox(
				new AABB(cx - halfT, cy + 1.0, cz - halfT,
						cx + halfT, cy + radius, cz + halfT),
				color, alpha, renderType
		);

		renderFilledBox(
				new AABB(cx - halfT, cy - radius, cz - halfT,
						cx + halfT, cy - 1.0, cz + halfT),
				color, alpha, renderType
		);

		renderFilledBox(
				new AABB(cx - halfT, cy - halfT, cz + 1.0,
						cx + halfT, cy + halfT, cz + radius),
				color, alpha, renderType
		);

		renderFilledBox(
				new AABB(cx - halfT, cy - halfT, cz - radius,
						cx + halfT, cy + halfT, cz - 1.0),
				color, alpha, renderType
		);
	}

	private static void renderTextFacingCamera(Component text, Vec3 pos, float offset, float scale, int colorAlphaHex) {
		eventPoseStack.pushPose();
		eventPoseStack.translate(pos.x, pos.y, pos.z);
		eventPoseStack.rotate(cameraState.orientation);

		scale *= TEXT_SCALE * (mc.player.position().distanceTo(pos) / 2.4);
		scale = Math.clamp(scale, 0.015f, 0.15f);
		eventPoseStack.scale(scale, -scale, scale);

		float x = -mc.font.width(text) / 2f;
		float y = -(.5f - offset) * (mc.font.lineHeight + 2);

		float bgOpacity = mc.options.getBackgroundOpacity(0.25f);
		int bgColor = (int) (bgOpacity * 255.0f) << 24;
		if ((colorAlphaHex & 0xFF000000) == 0) {
			colorAlphaHex |= 0xFF000000;
		}

		collector.submitText(eventPoseStack, x, y, text.getVisualOrderText(), false,
				Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, colorAlphaHex, bgColor, 0);

		eventPoseStack.popPose();
	}

	private static @NotNull String timestampRelativeText(long ts) {
		long now = System.currentTimeMillis();
		if (ts < now) return durationToText(now - ts) + " ago";
		return "in " + durationToText(ts - now);
	}

	private static @NotNull String durationToText(long ms) {
		if (ms < 0) ms = -ms;
		if (ms < 1000) return ms + "ms";
		long sec = ms / 1000;
		if (sec < 100) return sec + "s";
		long min = sec / 60;
		if (min < 100) return min + "min";
		long hours = min / 60;
		if (hours < 2 * 24) return hours + "h";
		long days = hours / 24;
		return days + " days";
	}
}
