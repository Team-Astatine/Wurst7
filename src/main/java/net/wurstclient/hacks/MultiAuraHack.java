/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
// 추가
// 추가
import net.minecraft.util.Hand;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.PauseAttackOnContainersSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.SwingHandSetting.SwingHand;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"multi aura", "ForceField", "force field"})
public final class MultiAuraHack extends Hack implements UpdateListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private AttackSpeedSliderSetting speed = new AttackSpeedSliderSetting();
	
	private final SliderSetting fov =
		new SliderSetting("FOV", 360, 30, 360, 10, ValueDisplay.DEGREES);
	
	private final SwingHandSetting swingHand = new SwingHandSetting(
		SwingHandSetting.genericCombatDescription(this), SwingHand.CLIENT);
	
	private final PauseAttackOnContainersSetting pauseOnContainers =
		new PauseAttackOnContainersSetting(false);
	
	private final EntityFilterList entityFilters =
		EntityFilterList.genericCombat();
	
	public MultiAuraHack()
	{
		super("MultiAura");
		setCategory(Category.COMBAT);
		
		addSetting(range);
		addSetting(speed);
		addSetting(fov);
		addSetting(swingHand);
		addSetting(pauseOnContainers);
		
		entityFilters.forEach(this::addSetting);
	}
	
	@Override
	protected void onEnable()
	{
		// 기존 코드 유지
		WURST.getHax().aimAssistHack.setEnabled(false);
		WURST.getHax().clickAuraHack.setEnabled(false);
		WURST.getHax().crystalAuraHack.setEnabled(false);
		WURST.getHax().fightBotHack.setEnabled(false);
		WURST.getHax().killauraLegitHack.setEnabled(false);
		WURST.getHax().killauraHack.setEnabled(false);
		WURST.getHax().protectHack.setEnabled(false);
		WURST.getHax().tpAuraHack.setEnabled(false);
		WURST.getHax().triggerBotHack.setEnabled(false);
		
		speed.resetTimer();
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		boolean isMainHandHoldingMace =
			player.getMainHandStack().isOf(Items.MACE);
		
		int setAttackSpeed = isMainHandHoldingMace ? 40 : 0;
		speed.setAttackSpeed(setAttackSpeed);
		
		speed.updateTimer();
		if(!speed.isTimeToAttack())
			return;
		
		if(pauseOnContainers.shouldPause())
			return;
		
		Stream<Entity> stream = EntityUtils.getAttackableEntities();
		double rangeSq = Math.pow(range.getValue(), 2);
		stream = stream.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);
		
		if(fov.getValue() < 360.0)
			stream = stream.filter(e -> RotationUtils.getAngleToLookVec(
				e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
		
		stream = entityFilters.applyTo(stream);
		
		ArrayList<Entity> entities =
			stream.collect(Collectors.toCollection(ArrayList::new));
		if(entities.isEmpty())
			return;
		
		WURST.getHax().autoSwordHack.setSlot(entities.get(0));
		
		// RegistryEntry<Enchantment> windBurstEntry = null;
		// boolean isWindBurstMace = false;
		//
		// if(isMainHandHoldingMace)
		// {
		//
		// // ***** 수정: RegistryEntry<Enchantment>로 변환 *****
		// windBurstEntry = MinecraftClient.getInstance().world
		// .getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT)
		// .getEntry(Enchantments.WIND_BURST.getValue()) // 올바른 RegistryKey
		// // 추출
		// .orElse(null);
		//
		// isWindBurstMace = isMainHandHoldingMace && windBurstEntry != null
		// && EnchantmentHelper.getLevel(windBurstEntry,
		// player.getMainHandStack()) > 0;
		// }
		
		// 공격 로직
		for(Entity entity : entities)
		{
			if(entity.getUuid().equals(AUTHOR_PLAYER))
				return;
			
			if(swingHand.getSelected() != SwingHand.OFF)
			{
				RotationUtils
					.getNeededRotations(entity.getBoundingBox().getCenter())
					.sendPlayerLookPacket();
			}
			
			MC.interactionManager.attackEntity(MC.player, entity);
		}
		
		// if(!entities.isEmpty() && isWindBurstMace)
		// {
		// MC.player.networkHandler.sendPacket(
		// new PlayerMoveC2SPacket.PositionAndOnGround(MC.player.getX(),
		// MC.player.getY(), MC.player.getZ(), true,
		// MC.player.horizontalCollision));
		// }
		
		if(swingHand.getSelected() != SwingHand.OFF)
			swingHand.swing(Hand.MAIN_HAND);
		
		speed.resetTimer();
	}
}
