/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;

@SearchTags({"cord", "position"})
public final class CurrentPositionHack extends Hack
{
	
	private int lastBlockX = Integer.MIN_VALUE;
	private int lastBlockZ = Integer.MIN_VALUE;
	private String cachedRender = "";
	
	public CurrentPositionHack()
	{
		super("CurrentPosition");
		setCategory(Category.COMBAT);
	}
	
	@Override
	public String getRenderName()
	{
		ClientPlayerEntity player = MC.player;
		if(player == null)
		{
			return getName();
		}
		
		int blockX = player.getBlockX();
		int blockZ = player.getBlockZ();
		
		if(blockX != lastBlockX || blockZ != lastBlockZ)
		{
			String keyPath =
				player.getWorld().getRegistryKey().getValue().getPath();
			
			int targetX, targetZ;
			if("overworld".equals(keyPath))
			{
				targetX = blockX / 8;
				targetZ = blockZ / 8;
			}else
			{
				targetX = blockX * 8;
				targetZ = blockZ * 8;
			}
			
			cachedRender = String.format(" [X{%+d} Z{%+d}]", targetX, targetZ);
			
			lastBlockX = blockX;
			lastBlockZ = blockZ;
		}
		
		return getName() + cachedRender;
	}
	
	@Override
	protected void onEnable()
	{}
	
	@Override
	protected void onDisable()
	{}
}
