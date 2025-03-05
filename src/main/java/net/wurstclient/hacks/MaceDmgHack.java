/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PlayerAttacksEntityListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.ChatUtils;

import java.util.ArrayList;
import java.util.List;

@SearchTags({"mace dmg", "MaceDamage", "mace damage"})
public final class MaceDmgHack extends Hack
	implements PlayerAttacksEntityListener
{
	private final SliderSetting sqrtValue = new SliderSetting("Mace Sqrt Value",
		"description.wurst.setting.maceDmg.SqrtValue", 300, 1, 500, 1,
		SliderSetting.ValueDisplay.DECIMAL);
	
	private final CheckboxSetting debuggingButton = new CheckboxSetting(
		"Debugging", "description.wurst.setting.MaceDmg.Debugging", false);
	
	private List<Block> ignoreBlocks = new ArrayList<>();
	private int[] sqrtValues = {0, 0, 0, 1, 4, 10, 17, 27, 38, 51, 67, 84, 104,
		125, 148, 174, 201, 231, 262, 295, 331, 368, 408, 449, 492};
	/*
	 * 1, 0
	 * 2, 0
	 * 3, 1
	 * 4, 4
	 * 5, 10
	 * 6, 17
	 * 7, 27
	 * 8, 38
	 * 9, 51
	 * 10, 67
	 * 11, 84
	 * 12, 104
	 * 13, 125
	 * 14, 148
	 * 15, 174
	 * 16, 201
	 * 17, 231
	 * 18, 262
	 * 19, 295
	 * 20, 331
	 * 21, 368
	 * 22, 408
	 * 23, 449
	 * 24, 492
	 */
	
	public MaceDmgHack()
	{
		super("MaceDMG");
		setCategory(Category.COMBAT);
		
		addSetting(sqrtValue);
		addSetting(debuggingButton);
		
		ignoreBlocks.add(Blocks.AIR);
		ignoreBlocks.add(Blocks.WATER);
		ignoreBlocks.add(Blocks.TALL_GRASS);
		ignoreBlocks.add(Blocks.COBWEB);
		ignoreBlocks.add(Blocks.VOID_AIR);
		ignoreBlocks.add(Blocks.LAVA);
		ignoreBlocks.add(Blocks.GLASS);
		ignoreBlocks.add(Blocks.SHORT_GRASS);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(PlayerAttacksEntityListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(PlayerAttacksEntityListener.class, this);
	}
	
	@Override
	public void onPlayerAttacksEntity(Entity target)
	{
		if(!MC.player.getMainHandStack().isOf(Items.MACE))
			return;
		
		int higher = getMaximumHeight(MC.player);
		if(debuggingButton.isChecked())
		{
			ChatUtils.message(String.valueOf(higher));
			ChatUtils.message(String.valueOf(sqrtValue.getValue()));
		}
		
		if(higher < 1)
			return;
		else if(higher > 24)
			sqrtValue.setValue(500);
		else
			sqrtValue.setValue(sqrtValues[higher]);
		
		for(int i = 0; i < 4; i++)
			sendFakeY(0);
		sendFakeY(Math.sqrt(sqrtValue.getValue()));
		sendFakeY(0);
	}
	
	private int getMaximumHeight(ClientPlayerEntity player)
	{
		int MAX_HEIGHT = 500;
		int maxAvailableHeight = 0;
		
		for(int i = (int)player.getY(); i < MAX_HEIGHT; i++)
		{
			BlockPos blockPos =
				BlockPos.ofFloored(player.getX(), i, player.getZ());
			Block block = MinecraftClient.getInstance().world
				.getBlockState(blockPos).getBlock();
			
			if(ignoreBlocks.contains(block))
				maxAvailableHeight++;
			
			else
				break;
		}
		
		return maxAvailableHeight;
	}
	
	private void sendFakeY(double offset)
	{
		MC.player.networkHandler.sendPacket(
			new PositionAndOnGround(MC.player.getX(), MC.player.getY() + offset,
				MC.player.getZ(), false, MC.player.horizontalCollision));
	}
}
