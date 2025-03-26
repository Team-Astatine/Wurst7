/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

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
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RenderUtils;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SearchTags({"logout", "player out", "logout spot"})
public final class LogoutSpotHack extends Hack
	implements UpdateListener, RenderListener
{
	private final ColorSetting color =
		new ColorSetting("Box Color", Color.WHITE);
	
	private record Entry(UUID uuid, Vec3d position, Instant instant)
	{}
	
	private Map<UUID, String> onlinePlayers = new HashMap<>();
	private Map<UUID, Vec3d> renderPlayers = new HashMap<>();
	private Map<UUID, String> lastPlayers = new HashMap<>();
	private final Map<UUID, Entry> logOutPlayers = new HashMap<>();
	
	public LogoutSpotHack()
	{
		super("LogOutSpot");
		setCategory(Category.RENDER);
		
		addSetting(color);
		
		scheduler.scheduleWithFixedDelay(
			() -> logOutPlayers.entrySet()
				.removeIf(entry -> Instant.now().isAfter(
					entry.getValue().instant.plus(10, ChronoUnit.MINUTES))),
			0, 5, TimeUnit.MINUTES);
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
	
	/**
	 * 1. {@link MinecraftClient#getInstance()} 서버 전체 PlayerList 를 가져온다.
	 * 2. lastPlayerList 와 전체 PlayerList 가 다름이 없다면 SKIP.
	 * 3. lastPlayerList 와 신규 PlayerList 가 다르고, 시야에 Rendering 되는 Player 라면,
	 * logoutEntries 에 추가한다.
	 * 4. logoutEntries 에 시야에 Rendering 되지 않는 Player 는 제거한다.
	 * 5. lastPlayerList 를 신규 PlayerList 로 변경한다.
	 */
	@Override
	public void onUpdate()
	{
		// 온라인 플레이어 목록 (네트워크 탭 리스트)
		onlinePlayers = MinecraftClient.getInstance().getNetworkHandler()
			.getPlayerList().stream()
			.collect(Collectors.toMap(entry -> entry.getProfile().getId(),
				entry -> entry.getProfile().getName()));
		
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
		
		// 마지막에 lastPlayers를 onlinePlayers의 모든 UUID로 갱신
		lastPlayers.clear();
		lastPlayers = onlinePlayers;
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(logOutPlayers.isEmpty())
			return;
		
		List<Box> logoutPlayerPositionBox = new ArrayList<>();
		for(Entry entry : logOutPlayers.values())
		{
			Vec3d targetExitPosition = entry.position();
			logoutPlayerPositionBox
				.add(new Box(targetExitPosition.x - 0.5, targetExitPosition.y,
					targetExitPosition.z - 0.5, targetExitPosition.x + 0.5,
					targetExitPosition.y + 2, targetExitPosition.z + 0.5));
		}
		
		RenderUtils.drawSolidBoxes(matrixStack, logoutPlayerPositionBox,
			color.getColorI(0x80), false);
	}
}
