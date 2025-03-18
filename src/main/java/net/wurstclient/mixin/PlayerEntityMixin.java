/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.player.PlayerEntity;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.HackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin
{
	@ModifyReturnValue(method = "getEntityInteractionRange", at = @At("RETURN"))
	private double modifyEntityInteractionRange(double original)
	{
		HackList hax = WurstClient.INSTANCE.getHax();
		if(hax != null && hax.reachHack.isEnabled())
		{
			return hax.reachHack.getReachDistance();
		}
		
		return original;
	}
	
	@ModifyReturnValue(method = "getBlockInteractionRange", at = @At("RETURN"))
	private double modifyBlockInteractionRange(double original)
	{
		HackList hax = WurstClient.INSTANCE.getHax();
		if(hax != null && hax.reachHack.isEnabled())
		{
			return hax.reachHack.getReachDistance();
		}
		
		return original;
	}
}
