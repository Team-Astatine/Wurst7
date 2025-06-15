package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"spam", "spamming", "chat", "flood", "chat flood"})
public final class ChatFloodHack extends Hack 
    implements UpdateListener
{
    private final SliderSetting delay = new SliderSetting("Delay",
    "The delay between send Chatting.", 20, 1, 200, 1,
    ValueDisplay.INTEGER.withSuffix(" ticks").withLabel(1, "1 tick"));

    public ChatFloodHack() {
        super("ChatFlood");
        setCategory(Category.CHAT);
        addSetting(delay);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }
    

    @Override
    protected void onDisable() {
        // TODO Auto-generated method stub
        super.onDisable();
    }

    @Override
    public void onUpdate() {
        // TODO Auto-generated method stub
        
    }    
}
