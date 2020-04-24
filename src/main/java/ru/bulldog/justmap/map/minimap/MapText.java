package ru.bulldog.justmap.map.minimap;

import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class MapText {
	  protected TextAlignment alignment = TextAlignment.CENTER;
	  protected String text;
	  protected int color = Colors.WHITE;
	  protected int x, y;
  
	  public static final MinecraftClient client = MinecraftClient.getInstance();
	  
	  public MapText(String text) {
		  this.text = text;
	  }
  
	  public MapText(TextAlignment alignment, String text) {
		  this.alignment = alignment;
		this.text = text;
	  }
  
	  public MapText(String text, int color) {
		  this.text = text;
		  this.color = color;
	  }
	  
	  public MapText(TextAlignment alignment, String text, int color) {
		  this.alignment = alignment;
		  this.text = text;
		  this.color = color;
	  }
	  
	  public void draw(MatrixStack matrixStack) {
		  TextRenderer textRenderer = client.textRenderer;
		
		  int width = client.getWindow().getScaledWidth();
		
		  switch (alignment) {
			 default:
			 case LEFT:
				 DrawHelper.DRAWER.drawString(matrixStack, textRenderer, text, x, y, color);
			 break;
			 case CENTER:
				 DrawHelper.drawBoundedString(matrixStack, textRenderer, text, x, y, 0, width - 2, color);
			 break;
			 case RIGHT:
				 DrawHelper.drawRightAlignedString(matrixStack, textRenderer, text, x, y, color);
			 break;
		  }
	  }
	  
	  public MapText setAlignment(TextAlignment alignment) {
		  this.alignment = alignment;
		  return this;
	  }
	  
	  public MapText setText(String text) {
		  this.text = text;
		  return this;
	  }
	  
	  public MapText setColor(int color) {
		  this.color = color;
		  return this;
	  }
}
