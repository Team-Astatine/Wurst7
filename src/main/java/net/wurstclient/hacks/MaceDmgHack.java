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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PlayerAttacksEntityListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.util.ChatUtils;

@SearchTags({"mace dmg", "MaceDamage", "mace damage"})
public final class MaceDmgHack extends Hack
	implements PlayerAttacksEntityListener
{
	public MaceDmgHack()
	{
		super("MaceDMG");
		setCategory(Category.COMBAT);
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

		for(int i = 0; i < 4; i++)
			sendFakeY(0);
		sendFakeY(Math.sqrt(300));
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
			
			if(block == Blocks.AIR || block == Blocks.VOID_AIR
				|| block == Blocks.WATER || block == Blocks.LAVA
				|| block == Blocks.TALL_GRASS || block == Blocks.GLASS
				|| block == Blocks.COBWEB || block == Blocks.SHORT_GRASS)
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
