package ru.bulldog.justmap.map.minimap;

import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class MapText {
	  protected TextAlignment alignment = TextAlignment.CENTER;
	  protected Text text;
	  protected int color = Colors.WHITE;
	  protected int x, y;
  
	  public static final MinecraftClient client = MinecraftClient.getInstance();
	  
	  public MapText(String text) {
		  this.text = new LiteralText(text);
	  }
  
	  public MapText(TextAlignment alignment, String text) {
		this.alignment = alignment;
		this.text = new LiteralText(text);
	  }
  
	  public MapText(String text, int color) {
		  this.text = new LiteralText(text);
		  this.color = color;
	  }
	  
	  public MapText(TextAlignment alignment, String text, int color) {
		  this.alignment = alignment;
		  this.text = new LiteralText(text);
		  this.color = color;
	  }
	  
	  public void draw(MatrixStack matrixStack) {
		  TextRenderer textRenderer = client.textRenderer;
		
		  int width = client.getWindow().getScaledWidth();
		
		  switch (alignment) {
			 default:
			 case LEFT:
				 DrawHelper.DRAWER.drawStringWithShadow(matrixStack, textRenderer, text.getString(), x, y, color);
			 break;
			 case CENTER:
				 DrawHelper.drawBoundedString(text.getString(), x, y, 0, width - 2, color);
			 break;
			 case RIGHT:
				 DrawHelper.drawRightAlignedString(text.getString(), x, y, color);
			 break;
		  }
	  }
	  
	  public MapText setAlignment(TextAlignment alignment) {
		  this.alignment = alignment;
		  return this;
	  }
	  
	  public MapText setText(String text) {
		  this.text = new LiteralText(text);
		  return this;
	  }
	  
	  public MapText setColor(int color) {
		  this.color = color;
		  return this;
	  }
}
