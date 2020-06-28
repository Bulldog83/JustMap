package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

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
		minecraft.getItemRenderer().renderInGuiWithOverrides(itemStack, x - offsetX, y - 5);
	}

	@Override
	public void update() {
		if (minecraft.player == null) return;
		
		this.itemStack = minecraft.player.getEquippedStack(slot);
		
		this.visible = !this.itemStack.isEmpty();
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

}
