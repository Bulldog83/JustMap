package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public abstract class InfoText {
	TextAlignment alignment;
	Text text;
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
		this.text = new LiteralText(text);
		this.color = color;
	}
	
	public void draw(MatrixStack matrixStack) {
		this.draw(matrixStack, x, y);
	}
	
	public void draw(MatrixStack matrixStack, int x, int y) {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		TextRenderer textRenderer = minecraft.textRenderer;
		int width = minecraft.getWindow().getScaledWidth();
		switch (alignment) {
			 case LEFT:
				RenderUtil.drawStringWithShadow(matrixStack, textRenderer, text.getString(), x, y, color);
			 break;
			 case CENTER:
				RenderUtil.drawBoundedString(matrixStack, text.getString(), x, y, 0, width - 2, color);
			 break;
			 case RIGHT:
				RenderUtil.drawRightAlignedString(matrixStack, text.getString(), x, y, color);
			 break;
		}
	}
	
	public void setPos(int x, int y) {
		if (!fixed) this.fixed = true;
		this.x = x;
		this.y = y;
    }
	
	public InfoText setAlignment(TextAlignment alignment) {
		this.alignment = alignment;
		return this;
	}
	
	public void setText(String text) {
		this.text = new LiteralText(text);
    }
	
	public InfoText setColor(int color) {
		this.color = color;
		return this;
	}
	
	public void setVisible(boolean visible) {
		if (this.visible != visible) {
			this.visible = visible;
		}
    }
}
