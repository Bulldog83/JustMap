package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.render.RenderUtil;

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
		MinecraftClient minecraft = MinecraftClient.getInstance();
		minecraft.getItemRenderer().renderInGuiWithOverrides(itemStack, posX, y - 5);
	}

	@Override
	public void update() {
		MinecraftClient minecraft = MinecraftClient.getInstance();
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
				itemString = this.getTranslation();
			}
			this.setText(itemString);
		}
	}

	private boolean isVisible() {
		if (!ClientSettings.showItems) return false;
		switch (slot) {
			case MAINHAND: return ClientSettings.showMainhand;
			case OFFHAND: return ClientSettings.showOffhand;
			case HEAD: return ClientSettings.showHead;
			case CHEST: return ClientSettings.showChest;
			case LEGS: return ClientSettings.showLegs;
			case FEET: return ClientSettings.showFeet;
		}

		return true;
	}

	private String getTranslation() {
		return this.itemStack.getItem().getName(itemStack).getString();
	}
}
