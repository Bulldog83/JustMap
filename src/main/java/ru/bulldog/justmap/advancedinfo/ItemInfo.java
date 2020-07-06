package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.RenderUtil;

public class ItemInfo extends InfoText {

	private final EquipmentSlot slot;
	private ItemStack itemStack;
	
	public ItemInfo(EquipmentSlot slot) {
		super("Item");
		this.slot = slot;
		this.offsetX = 18;
	}

	@Override
	public void draw(MatrixStack matrix) {
		super.draw(matrix);
		int posX;
		switch (alignment) {
			case RIGHT:
				posX = x + offset;
				break;
			case CENTER:
				int textWidth = RenderUtil.getWidth(text);
				posX = x - textWidth / 2 - offsetX;
				break;
			default:
				posX = x - offsetX;	
		}
		minecraft.getItemRenderer().renderInGuiWithOverrides(itemStack, posX, y - 5);
	}

	@Override
	public void update() {
		if (minecraft.player == null) {
			this.setVisible(false);
			return;
		}		
		this.itemStack = minecraft.player.getEquippedStack(slot);		
		this.setVisible(this.isVisible() && !this.itemStack.isEmpty());
		if (visible) {
			String itemString;
			if (this.itemStack.isDamageable()) {
				int maxDamage = this.itemStack.getMaxDamage();
				int damage = maxDamage - this.itemStack.getDamage();
				itemString = String.format("%d/%d", damage, maxDamage);
			} else if (this.itemStack.isStackable()) {
				itemString = String.format("%d", this.itemStack.getCount());
			} else {
				itemString = I18n.translate(this.itemStack.getTranslationKey());
			}
			this.setText(itemString);
		}
	}
	
	private boolean isVisible() {
		if (!ClientParams.showItems) return false;
		switch (slot) {
			case MAINHAND: return ClientParams.showMainhand;
			case OFFHAND: return ClientParams.showOffhand;
			case HEAD: return ClientParams.showHead;
			case CHEST: return ClientParams.showChest;
			case LEGS: return ClientParams.showLegs;
			case FEET: return ClientParams.showFeet;
		}
		
		return true;
	}
}
