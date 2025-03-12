/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient;

import java.util.OptionalDouble;

import net.minecraft.client.gl.ShaderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;

public enum WurstRenderLayers
{
	;
	
	/**
	 * Similar to {@link RenderLayer#getDebugLineStrip(double)}, but as a
	 * non-srip version with support for transparency.
	 *
	 * @implNote Just like {@link RenderLayer#getDebugLineStrip(double)}, this
	 *           layer doesn't support any other line width than 1px. Changing
	 *           the line width number does nothing.
	 */
	public static final RenderLayer.MultiPhase ONE_PIXEL_LINES =
		RenderLayer.method_24048("wurst:1px_lines", 1536,
			WurstShaderPipelines.ONE_PIXEL_LINES,
			RenderLayer.MultiPhaseParameters.builder()
				.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1)))
				.build(false));
	
	/**
	 * Similar to {@link RenderLayer#getDebugLineStrip(double)}, but with
	 * support for transparency.
	 *
	 * @implNote Just like {@link RenderLayer#getDebugLineStrip(double)}, this
	 *           layer doesn't support any other line width than 1px. Changing
	 *           the line width number does nothing.
	 */
	public static final RenderLayer.MultiPhase ONE_PIXEL_LINE_STRIP =
		RenderLayer.method_24048("wurst:1px_line_strip", 1536,
			WurstShaderPipelines.ONE_PIXEL_LINE_STRIP,
			RenderLayer.MultiPhaseParameters.builder()
				.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1)))
				.build(false));
	
	/**
	 * Similar to {@link RenderLayer#getLines()}, but with line width 2.
	 */
	public static final RenderLayer.MultiPhase LINES = RenderLayer.method_24048(
		"wurst:lines", 1536, ShaderPipelines.field_56833,
		RenderLayer.MultiPhaseParameters.builder()
			.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2)))
			.layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
			.target(RenderLayer.ITEM_ENTITY_TARGET).build(false));
	
	/**
	 * Similar to {@link RenderLayer#getLines()}, but with line width 2 and no
	 * depth test.
	 */
	public static final RenderLayer.MultiPhase ESP_LINES = RenderLayer
		.method_24048("wurst:esp_lines", 1536, WurstShaderPipelines.ESP_LINES,
			RenderLayer.MultiPhaseParameters.builder()
				.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2)))
				.layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
				.target(RenderLayer.ITEM_ENTITY_TARGET).build(false));
	
	/**
	 * Similar to {@link RenderLayer#getLineStrip()}, but with line width 2.
	 */
	public static final RenderLayer.MultiPhase LINE_STRIP =
		RenderLayer.method_24049("wurst:line_strip", 1536, false, true,
			ShaderPipelines.field_56835,
			RenderLayer.MultiPhaseParameters.builder()
				.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2)))
				.layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
				.target(RenderLayer.ITEM_ENTITY_TARGET).build(false));
	
	/**
	 * Similar to {@link RenderLayer#getLineStrip()}, but with line width 2 and
	 * no depth test.
	 */
	public static final RenderLayer.MultiPhase ESP_LINE_STRIP =
		RenderLayer.method_24049("wurst:esp_line_strip", 1536, false, true,
			WurstShaderPipelines.ESP_LINE_STRIP,
			RenderLayer.MultiPhaseParameters.builder()
				.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2)))
				.layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
				.target(RenderLayer.ITEM_ENTITY_TARGET).build(false));
	
	/**
	 * Similar to {@link RenderLayer#getDebugQuads()}, but with culling enabled.
	 */
	public static final RenderLayer.MultiPhase QUADS = RenderLayer.method_24049(
		"wurst:quads", 1536, false, true, WurstShaderPipelines.QUADS,
		RenderLayer.MultiPhaseParameters.builder().build(false));
	
	/**
	 * Similar to {@link RenderLayer#getDebugQuads()}, but with culling enabled
	 * and no depth test.
	 */
	public static final RenderLayer.MultiPhase ESP_QUADS =
		RenderLayer.method_24049("wurst:esp_quads", 1536, false, true,
			WurstShaderPipelines.ESP_QUADS,
			RenderLayer.MultiPhaseParameters.builder().build(false));
	
	/**
	 * Similar to {@link RenderLayer#getDebugQuads()}, but with no depth test.
	 */
	public static final RenderLayer.MultiPhase ESP_QUADS_NO_CULLING =
		RenderLayer.method_24049("wurst:esp_quads_no_culling", 1536, false,
			true, WurstShaderPipelines.ESP_QUADS_NO_CULLING,
			RenderLayer.MultiPhaseParameters.builder().build(false));
	
	/**
	 * Returns either {@link #QUADS} or {@link #ESP_QUADS} depending on the
	 * value of {@code depthTest}.
	 */
	public static RenderLayer getQuads(boolean depthTest)
	{
		return depthTest ? QUADS : ESP_QUADS;
	}
	
	/**
	 * Returns either {@link #LINES} or {@link #ESP_LINES} depending on the
	 * value of {@code depthTest}.
	 */
	public static RenderLayer getLines(boolean depthTest)
	{
		return depthTest ? LINES : ESP_LINES;
	}
	
	/**
	 * Returns either {@link #LINE_STRIP} or {@link #ESP_LINE_STRIP} depending
	 * on the value of {@code depthTest}.
	 */
	public static RenderLayer getLineStrip(boolean depthTest)
	{
		return depthTest ? LINE_STRIP : ESP_LINE_STRIP;
	}
}
