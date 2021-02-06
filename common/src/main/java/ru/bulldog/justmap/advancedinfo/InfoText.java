package ru.bulldog.justmap.advancedinfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

import net.minecraft.client.Minecraft;

public abstract class InfoText {
	TextAlignment alignment;
	Component text;
	boolean fixed = false;
	boolean visible = true;
	int color;
	int offset;
	int offsetX;
	int offsetY;
	int x, y;
  
	public abstract void update();
	
	public InfoText(String text) {
		this(TextAlignment.LEFT, text, Colors.WHITE);
	}
  
	public InfoText(TextAlignment alignment, String text) {
		this(alignment, text, Colors.WHITE);
	}
  
	public InfoText(String text, int color) {
		this(TextAlignment.LEFT, text, color);
	}
	
	public InfoText(TextAlignment alignment, String text, int color) {
		this.alignment = alignment;
		this.text = new TextComponent(text);
		this.color = color;
	}
	
	public void draw(PoseStack matrixStack) {
		this.draw(matrixStack, x, y);
	}
	
	public void draw(PoseStack matrixStack, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		int width = minecraft.getWindow().getGuiScaledWidth();
		switch (alignment) {
			 case LEFT:
				RenderUtil.drawString(matrixStack, font, text.getString(), x, y, color);
			 break;
			 case CENTER:
				RenderUtil.drawBoundedString(matrixStack, text.getString(), x, y, 0, width - 2, color);
			 break;
			 case RIGHT:
				RenderUtil.drawRightAlignedString(matrixStack, text.getString(), x, y, color);
			 break;
		}
	}
	
	public InfoText setPos(int x, int y) {
		if (!fixed) this.fixed = true;
		this.x = x;
		this.y = y;		
		return this;
	}
	
	public InfoText setAlignment(TextAlignment alignment) {
		this.alignment = alignment;
		return this;
	}
	
	public InfoText setText(String text) {
		this.text = new TextComponent(text);
		return this;
	}
	
	public InfoText setColor(int color) {
		this.color = color;
		return this;
	}
	
	public InfoText setVisible(boolean visible) {
		if (this.visible != visible) {
			this.visible = visible;
		}
		return this;
	}
}
