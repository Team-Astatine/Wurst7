/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.massWhispering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.StringHelper;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.events.ChatOutputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.TextFieldSetting;
import net.wurstclient.util.ChatUtils;

@SearchTags({"mass whispering"})
@DontSaveState
public final class MassWhisperHack extends Hack
	implements UpdateListener, ChatOutputListener, ChatInputListener
{
	private static final Pattern ALLOWED_COMMANDS =
		Pattern.compile("^/+[a-zA-Z0-9_\\-]+$");
	
	private final TextFieldSetting commandSetting = new TextFieldSetting(
		"Command", "The command to use for whispering.\n" + "Examples: /w, /m",
		"/w", s -> s.length() < 64 && ALLOWED_COMMANDS.matcher(s).matches());
	
	private final SliderSetting delay = new SliderSetting("Delay",
		"The delay between each teleportation request.", 20, 1, 200, 1,
		ValueDisplay.INTEGER.withSuffix(" ticks").withLabel(1, "1 tick"));
	
	private final CheckboxSetting ignoreErrors =
		new CheckboxSetting("Ignore errors",
			"Whether to ignore messages from the server telling you that the"
				+ " message command isn't valid or that you don't have"
				+ " permission to use it.",
			false);
	
	private final Random random = new Random();
	private final ArrayList<String> players = new ArrayList<>();
	private final BlockingQueue<String> message = new LinkedBlockingDeque<>();
	
	private String command;
	private int timer;
	
	public MassWhisperHack()
	{
		super("MassWhispering");
		setCategory(Category.CHAT);
		addSetting(commandSetting);
		addSetting(delay);
		addSetting(ignoreErrors);
	}
	
	@Override
	protected void onEnable()
	{
		// reset state
		players.clear();
		timer = 0;
		
		// cache command in case the setting is changed mid-run
		command = commandSetting.getValue().substring(1);
		
		// collect player names
		String playerName = MC.getSession().getUsername();
		for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
		{
			String name = info.getProfile().getName();
			name = StringHelper.stripTextFormat(name);
			
			if(name.equalsIgnoreCase(playerName))
				continue;
			
			players.add(name);
		}
		
		Collections.shuffle(players, random);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(ChatInputListener.class, this);
		EVENTS.add(ChatOutputListener.class, this);
		
		if(players.isEmpty())
		{
			ChatUtils.error("Couldn't find any players.");
			setEnabled(false);
		}
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(ChatInputListener.class, this);
		EVENTS.remove(ChatOutputListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(timer > 0)
		{
			timer--;
			return;
		}
		
		if(message.isEmpty())
		{
			return;
		}
		
		players.forEach(player -> {
			String comment = "";
			try
			{
				comment = message.take();
			}catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			
			MC.getNetworkHandler().sendChatCommand(
				String.format("%s %s %s", command, player, comment));
		});
		
		timer = delay.getValueI() - 1;
	}
	
	@Override
	public void onSentMessage(ChatOutputEvent event)
	{
		message.offer(event.getOriginalMessage());
		event.cancel();
	}
	
	@Override
	public void onReceivedMessage(ChatInputEvent event)
	{
		if(message.contains("/help") || message.contains("permission"))
		{
			if(ignoreErrors.isChecked())
				return;
			
			event.cancel();
			ChatUtils.error("This server doesn't have a "
				+ command.toUpperCase() + " command.");
			setEnabled(false);
		}
	}
}
