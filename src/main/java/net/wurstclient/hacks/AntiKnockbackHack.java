package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.KnockbackListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"anti knockback", "AntiVelocity", "anti velocity", "NoKnockback",
		"no knockback", "AntiKB", "anti kb"})
public final class AntiKnockbackHack extends Hack implements KnockbackListener
{
	private final SliderSetting hStrength =
			new SliderSetting("Horizontal Strength",
					"수평 방향 넉백 감소 정도.\n"
							+ "0% = 정상 넉백\n"
							+ "100% = 넉백 없음",
					1, 0, 1, 0.01, ValueDisplay.PERCENTAGE);

	private final SliderSetting vStrength =
			new SliderSetting("Vertical Strength",
					"수직 방향 넉백 감소 정도.\n"
							+ "0% = 정상 넉백\n"
							+ "100% = 넉백 없음",
					1, 0, 1, 0.01, ValueDisplay.PERCENTAGE);

	public AntiKnockbackHack()
	{
		super("AntiKnockback");
		setCategory(Category.COMBAT);
		addSetting(hStrength);
		addSetting(vStrength);
	}

	@Override
	protected void onEnable()
	{
		EVENTS.add(KnockbackListener.class, this);
	}

	@Override
	protected void onDisable()
	{
		EVENTS.remove(KnockbackListener.class, this);
	}

	@Override
	public void onKnockback(KnockbackEvent event)
	{
		double verticalMultiplier = 1 - vStrength.getValue();
		double horizontalMultiplier = 1 - hStrength.getValue();

		event.setX(event.getDefaultX() * horizontalMultiplier);
		event.setY(event.getDefaultY() * verticalMultiplier);
		event.setZ(event.getDefaultZ() * horizontalMultiplier);
	}
}