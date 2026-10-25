package io.github.liam0999.civmod.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class SnitchRenderTypes {
	private static final DepthStencilState DEPTH_TESTED_WRITE =
		new DepthStencilState(CompareOp.GREATER_THAN, true);

	private static final DepthStencilState DEPTH_BIASED_WRITE =
		new DepthStencilState(CompareOp.GREATER_THAN, true, 1.0f, 1.0f);

	public static final RenderType QUADS = quads("quads", Optional.of(DEPTH_TESTED_WRITE));
	public static final RenderType LINES = lines("lines", Optional.of(DEPTH_BIASED_WRITE));
	public static final RenderType SEE_THROUGH_QUADS = quads("see_through_quads", Optional.empty());
	public static final RenderType SEE_THROUGH_LINES = lines("see_through_lines", Optional.empty());

	private static RenderType quads(String name, Optional<DepthStencilState> depth) {
		RenderPipeline pipeline = RenderPipeline.builder()
			.withLocation(Identifier.fromNamespaceAndPath("civmod", "pipelines/" + name))
			.withBindGroupLayout(BindGroupLayouts.PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withDepthStencilState(depth)
			.withCull(false)
			.build();

		RenderPipeline.Builder oitBase = RenderPipeline.builder()
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withCull(false);

		return create(name, pipeline, oitBase, depth.isEmpty());
	}

	private static RenderType lines(String name, Optional<DepthStencilState> depth) {
		RenderPipeline pipeline = RenderPipeline.builder()
			.withLocation(Identifier.fromNamespaceAndPath("civmod", "pipelines/" + name))
			.withBindGroupLayout(BindGroupLayouts.GLOBALS)
			.withBindGroupLayout(BindGroupLayouts.PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withBindGroupLayout(BindGroupLayouts.FOG)
			.withVertexShader("core/rendertype_lines")
			.withFragmentShader("core/rendertype_lines")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
			.withPrimitiveTopology(PrimitiveTopology.LINES)
			.withDepthStencilState(depth)
			.withCull(false)
			.build();

		RenderPipeline.Builder oitBase = RenderPipeline.builder()
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withBindGroupLayout(BindGroupLayouts.FOG)
			.withVertexShader("core/rendertype_lines")
			.withFragmentShader("core/rendertype_lines")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
			.withPrimitiveTopology(PrimitiveTopology.LINES)
			.withCull(false);

		return create(name, pipeline, oitBase, depth.isEmpty());
	}

	// Without an OIT pipeline set, submitting this type crashes when Improved Transparency is on.
	private static RenderType create(String name, RenderPipeline pipeline, RenderPipeline.Builder oitBase, boolean seeThrough) {
		OitPipelineSet.Builder oit = OitPipelineSet.builder("snitchmod_" + name, oitBase);
		if (seeThrough) {
			oit.withoutDepthTest();
		}
		return RenderType.create(
			"snitchmod_" + name,
			RenderSetup.builder(pipeline).setOitPipelines(oit.build()).createRenderSetup());
	}
}
