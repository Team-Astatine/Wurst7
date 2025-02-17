/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.CmdUtils;

@DontBlock
public final class SetCheckboxCmd extends Command
{
	public SetCheckboxCmd()
	{
		super("setcheckbox",
			"Changes a checkbox setting of a feature. Allows you\n"
				+ "to toggle checkboxes through keybinds.",
			".setcheckbox <feature> <setting> (on|off)",
			".setcheckbox <feature> <setting> toggle");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 3)
			throw new CmdSyntaxError();
		
		Feature feature = CmdUtils.findFeature(args[0]);
		Setting setting = CmdUtils.findSetting(feature, args[1]);
		CheckboxSetting checkbox = getAsCheckbox(feature, setting);
		setChecked(checkbox, args[2]);
	}
	
	private CheckboxSetting getAsCheckbox(Feature feature, Setting setting)
		throws CmdError
	{
		if(!(setting instanceof CheckboxSetting))
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " is not a checkbox setting.");
		
		return (CheckboxSetting)setting;
	}
	
	private void setChecked(CheckboxSetting checkbox, String value)
		throws CmdSyntaxError
	{
		switch(value.toLowerCase())
		{
			case "on":
			checkbox.setChecked(true);
			break;
			
			case "off":
			checkbox.setChecked(false);
			break;
			
			case "toggle":
			checkbox.setChecked(!checkbox.isChecked());
			break;
			
			default:
			throw new CmdSyntaxError();
		}
	}
}
