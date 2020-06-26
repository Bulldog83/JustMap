package ru.bulldog.justmap.advancedinfo;

import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class InfoText {
	  TextAlignment alignment = TextAlignment.CENTER;
	  Text text;
	  Identifier icon;
	  boolean fixed = false;
	  boolean visible = true;
	  int color = Colors.WHITE;
	  int x, y;
  
	  private static final MinecraftClient client = MinecraftClient.getInstance();
	  
	  public InfoText(String text) {
		  this.text = new LiteralText(text);
	  }
  
	  public InfoText(TextAlignment alignment, String text) {
		this.alignment = alignment;
		this.text = new LiteralText(text);
	  }
  
	  public InfoText(String text, int color) {
		  this.text = new LiteralText(text);
		  this.color = color;
	  }
	  
	  public InfoText(TextAlignment alignment, String text, int color) {
		  this.alignment = alignment;
		  this.text = new LiteralText(text);
		  this.color = color;
	  }
	  
	  public void draw(MatrixStack matrixStack) {
		  TextRenderer textRenderer = client.textRenderer;
		
		  int width = client.getWindow().getScaledWidth();
		
		  switch (alignment) {
			 case LEFT:
				 DrawHelper.DRAWER.drawStringWithShadow(matrixStack, textRenderer, text.getString(), x, y, color);
			 break;
			 case CENTER:
				 DrawHelper.drawBoundedString(matrixStack, text.getString(), x, y, 0, width - 2, color);
			 break;
			 case RIGHT:
				 DrawHelper.drawRightAlignedString(matrixStack, text.getString(), x, y, color);
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
		  this.text = new LiteralText(text);
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
