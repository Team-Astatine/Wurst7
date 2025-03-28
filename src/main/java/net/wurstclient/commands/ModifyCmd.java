/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.util.Arrays;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class ModifyCmd extends Command
{
	public ModifyCmd()
	{
		super("modify", "Allows you to modify NBT data of items.",
			".modify add <nbt_data>", ".modify set <nbt_data>",
			".modify remove <nbt_path>", "Use $ for colors, use $$ for $.", "",
			"Example:",
			".modify add {display:{Name:'{\"text\":\"$cRed Name\"}'}}",
			"(changes the item's name to \u00a7cRed Name\u00a7r)");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		ClientPlayerEntity player = MC.player;
		
		if(!player.getAbilities().creativeMode)
			throw new CmdError("Creative mode only.");
		
		if(args.length < 2)
			throw new CmdSyntaxError();
		
		ItemStack stack = player.getInventory().getSelectedStack();
		
		if(stack == null)
			throw new CmdError("You must hold an item in your main hand.");
		
		switch(args[0].toLowerCase())
		{
			case "add":
			add(stack, args);
			break;
			
			case "set":
			set(stack, args);
			break;
			
			case "remove":
			remove(stack, args);
			break;
			
			default:
			throw new CmdSyntaxError();
		}
		
		MC.player.networkHandler
			.sendPacket(new CreativeInventoryActionC2SPacket(
				36 + player.getInventory().getSelectedSlot(), stack));
		
		ChatUtils.message("Item modified.");
	}
	
	private void add(ItemStack stack, String[] args) throws CmdError
	{
		String nbtString =
			String.join(" ", Arrays.copyOfRange(args, 1, args.length))
				.replace("$", "\u00a7").replace("\u00a7\u00a7", "$");
		
		NbtCompound itemNbt = stack
			.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
			.copyNbt();
		
		try
		{
			NbtCompound parsedNbt = StringNbtReader.readCompound(nbtString);
			itemNbt.copyFrom(parsedNbt);
			stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(itemNbt));
			
		}catch(CommandSyntaxException e)
		{
			ChatUtils.message(e.getMessage());
			throw new CmdError("NBT data is invalid.");
		}
	}
	
	private void set(ItemStack stack, String[] args) throws CmdError
	{
		String nbt = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		nbt = nbt.replace("$", "\u00a7").replace("\u00a7\u00a7", "$");
		
		try
		{
			NbtCompound tag = StringNbtReader.readCompound(nbt);
			stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
			
		}catch(CommandSyntaxException e)
		{
			ChatUtils.message(e.getMessage());
			throw new CmdError("NBT data is invalid.");
		}
	}
	
	private void remove(ItemStack stack, String[] args) throws CmdException
	{
		if(args.length > 2)
			throw new CmdSyntaxError();
		
		NbtPath path = parseNbtPath(stack
			.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
			.copyNbt(), args[1]);
		
		if(path == null)
			throw new CmdError("The path does not exist.");
		
		path.base.remove(path.key);
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(path.base));
	}
	
	private NbtPath parseNbtPath(NbtCompound tag, String path)
	{
		String[] parts = path.split("\\.");
		
		NbtCompound base = tag;
		if(base == null)
			return null;
		
		for(int i = 0; i < parts.length - 1; i++)
		{
			String part = parts[i];
			
			if(!base.contains(part) || !(base.get(part) instanceof NbtCompound))
				return null;
			
			base = base.getCompound(part).orElse(new NbtCompound());
		}
		
		if(!base.contains(parts[parts.length - 1]))
			return null;
		
		return new NbtPath(base, parts[parts.length - 1]);
	}
	
	private static class NbtPath
	{
		public NbtCompound base;
		public String key;
		
		public NbtPath(NbtCompound base, String key)
		{
			this.base = base;
			this.key = key;
		}
	}
}
