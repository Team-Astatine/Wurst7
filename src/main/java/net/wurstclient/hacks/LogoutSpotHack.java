/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EspBoxSizeSetting;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;
import org.lwjgl.opengl.GL11;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SearchTags({"logout", "player out", "logout spot"})
public final class LogoutSpotHack extends Hack
	implements UpdateListener, RenderListener
{
	record Entry(UUID uuid, Vec3d position, Instant instant)
	{}
	
	private final EspBoxSizeSetting boxSize = new EspBoxSizeSetting(
		"\u00a7lAccurate\u00a7r mode shows the exact hitbox of each player.\n"
			+ "\u00a7lFancy\u00a7r mode shows slightly larger boxes that look better.");
	
	private Map<UUID, String> onlinePlayers = new HashMap<>();
	private Map<UUID, Vec3d> renderPlayers = new HashMap<>();
	private Map<UUID, String> lastPlayers = new HashMap<>();
	private final Map<UUID, Entry> logOutPlayers = new HashMap<>();
	
	public LogoutSpotHack()
	{
		super("LogOutSpot");
		setCategory(Category.RENDER);
		
		scheduler.scheduleWithFixedDelay(
			() -> logOutPlayers.entrySet().removeIf(entry -> Instant.now()
				.isAfter(entry.getValue().instant.plus(10, ChronoUnit.MINUTES)))
			
			, 0, 5, TimeUnit.MINUTES);
		
		addSetting(boxSize);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}
	
	/*
	 * 1. 전체 플레이어를 가져온다.
	 * 2. 플레이어 변화가 없으면 스킵한다.
	 * 3. 이전 플레이어(lastPlayers) 중 현재 온라인 플레이어(onlinePlayers)에 없는 플레이어를 찾는다.
	 * 단, 해당 플레이어가 렌더링되고 있는 경우(renderPlayers에 존재하면) 그 위치 정보로 logoutEntries에
	 * 추가한다.
	 * 4. logoutEntries에서 렌더링되지 않는 항목은 제거한다.
	 * 5. 마지막에 lastPlayers를 현재 onlinePlayers의 모든 UUID로 교체한다.
	 */
	@Override
	public void onUpdate()
	{
		// System.out.println("lastPlayers > " + lastPlayers);
		
		// 온라인 플레이어 목록 (네트워크 탭 리스트)
		onlinePlayers = MinecraftClient.getInstance().getNetworkHandler()
			.getPlayerList().stream()
			.collect(Collectors.toMap(entry -> entry.getProfile().getId(),
				entry -> entry.getProfile().getName()));
		// System.out.println("onlinePlayers > " + onlinePlayers);
		
		// 온라인 플레이어에 재접속한 경우, logOutPlayers에서 제거
		logOutPlayers.entrySet()
			.removeIf(entry -> onlinePlayers.containsKey(entry.getKey()));
		
		// 플레이어 수 변화가 없으면 업데이트를 스킵
		if(onlinePlayers.size() == lastPlayers.size())
		{
			// 현재 월드에서 렌더링되고 있는 플레이어 목록 (FakePlayer 제외)
			renderPlayers = MC.world.getPlayers().parallelStream()
				.filter(e -> !(e instanceof FakePlayerEntity))
				.collect(Collectors.toMap(Entity::getUuid, Entity::getPos));
			// System.out.println("renderPlayers > " + renderPlayers);
			return;
		}
		
		for(UUID uuid : lastPlayers.keySet())
		{
			if(!onlinePlayers.containsKey(uuid))
			{ // 서버에 없는 플레이어라면
				System.out.println(renderPlayers.get(uuid));
				Optional.ofNullable(renderPlayers.get(uuid))
					.ifPresent(pos -> logOutPlayers.put(uuid,
						new Entry(uuid, pos, Instant.now())));
			}
		}
		// System.out.println("logOutPlayers > " + logOutPlayers);
		
		// 마지막에 lastPlayers를 onlinePlayers의 모든 UUID로 갱신
		lastPlayers.clear();
		lastPlayers = onlinePlayers;
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		
		RegionPos region = RenderUtils.getCameraRegion();
		RenderUtils.applyRegionalRenderOffset(matrixStack, region);
		
		float extraSize = boxSize.getExtraSize();
		
		for(Entry entry : logOutPlayers.values())
		{
			matrixStack.push();
			
			Vec3d outPosition = entry.position().subtract(region.toVec3d());
			matrixStack.translate(outPosition.x, outPosition.y, outPosition.z);
			matrixStack.scale(1 + extraSize, 1 + extraSize, 1 + extraSize);
			
			Box logoutBox = new Box(-0.5, 0, -0.5, 0.5, 2, 0.5);
			
			// 채워진 박스 그리기 (투명도 0.1)
			RenderSystem.setShaderColor(0, 1, 1, 0.1F);
			RenderUtils.drawSolidBox(logoutBox, matrixStack); // drawFilledBox
																// 또는 해당하는 메서드
																// 사용
			
			// 윤곽선 박스 그리기 (투명도 0.75)
			RenderSystem.setShaderColor(0, 1, 1, 0.1F);
			RenderUtils.drawOutlinedBox(logoutBox, matrixStack);
			
			matrixStack.pop();
		}
		
		matrixStack.pop();
		
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
