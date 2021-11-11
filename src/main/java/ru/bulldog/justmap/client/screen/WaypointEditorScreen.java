package ru.bulldog.justmap.client.screen;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.widget.TitledButtonWidget;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerRange;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.Predicates;
import ru.bulldog.justmap.util.colors.Colors;

public class WaypointEditorScreen extends AbstractMapScreen {
	
	private static final Text TITLE = new TranslatableText(JustMap.MODID + ".gui.screen.waypoints_editor");

	private final Waypoint waypoint;
	
	private int colorIndex;
	private int iconIndex;
	private int showRange;

	private final static int SPACING = 2;
	private final static int PADDING = 10;
	private final static int ROW_HEIGHT = 20;
	
	private TitledButtonWidget<TextFieldWidget> nameField;
	private CheckboxWidget isHidden;
	private CheckboxWidget isTrackable;
	private CheckboxWidget isRenderable;
	private ButtonWidget prevColorButton, nextColorButton;
	private TextFieldWidget xField, yField, zField;
	private final Consumer<Waypoint> onSaveCallback;
	
	public WaypointEditorScreen(Waypoint waypoint, Screen parent, Consumer<Waypoint> onSaveCallback) {
		super(TITLE, parent);
		
		this.waypoint = waypoint;
		colorIndex = getColorIndex(waypoint.color);
		this.iconIndex = getIconIndex(waypoint.getIcon());
		this.onSaveCallback = onSaveCallback;
	}
	
	@Override
	public void init() {
		super.init();
		
		this.center = width / 2;
		
		int screenW = center > 480 ? center : width > 480 ? 480 : width;
	
		this.x = center - screenW / 2;
		this.y = 60;

		int row = ROW_HEIGHT + SPACING;

		int ex = x + PADDING;
		int ey = y;
		int ew = screenW - PADDING * 2;
		this.nameField = new TitledButtonWidget<>(textRenderer, new TextFieldWidget(textRenderer, 0, 0, ew - 30, 12, new LiteralText("Name")), ex, ey, ew, ROW_HEIGHT, "", lang("name").asString());
		this.nameField.changeFocus(true);
		this.nameField.widget.setMaxLength(48);
		this.nameField.widget.setText(waypoint.name);
		
		@SuppressWarnings("unchecked")
		List<Element> children = (List<Element>) children();
		children.add(nameField);

		Predicate<String> validNumber = (s) -> Predicates.or(s, Predicates.isInteger, Predicates.isEmpty, "-"::equals);
		
		ew = 60;
		int px = center - (ew * 3) / 2;
		
		ey += row;
		
		this.xField = new TextFieldWidget(textRenderer, px, ey, ew, ROW_HEIGHT, new LiteralText(""));
		this.xField.setTextPredicate(validNumber);
		this.xField.setMaxLength(7);
		this.xField.setText(waypoint.pos.getX() + "");
		
		this.yField = new TextFieldWidget(textRenderer, px + ew, ey, ew, ROW_HEIGHT, new LiteralText(""));
		this.yField.setTextPredicate(validNumber);
		this.yField.setMaxLength(7);
		this.yField.setText(waypoint.pos.getY() + "");
		
		this.zField = new TextFieldWidget(textRenderer, px + 2 * ew, ey, ew, ROW_HEIGHT, new LiteralText(""));
		this.zField.setTextPredicate(validNumber);
		this.zField.setMaxLength(7);
		this.zField.setText(waypoint.pos.getZ() + "");
		
		children.add(xField);
		children.add(yField);
		children.add(zField);
		
		ey += row;
		
		ew = 20;
		this.prevColorButton = new ButtonWidget(ex, ey, ew, ROW_HEIGHT, new LiteralText("<"), (b) -> cycleColor(-1));
		children.add(prevColorButton);
		
		this.nextColorButton = new ButtonWidget(x + screenW - ew - PADDING, ey, ew, ROW_HEIGHT, new LiteralText(">"), (b) -> cycleColor(1));
		children.add(nextColorButton);
		
		ey += row;

		ButtonWidget prevIconButton = new ButtonWidget(ex, ey, ew, ROW_HEIGHT, new LiteralText("<"), (b) -> cycleIcon(-1));
		children.add(prevIconButton);

		ButtonWidget nextIconButton = new ButtonWidget(x + screenW - ew - PADDING, ey, ew, ROW_HEIGHT, new LiteralText(">"), (b) -> cycleIcon(1));
		children.add(nextIconButton);
		
		ey += row * 1.5;
		
		int sliderW = (int) (screenW * 0.6);
		int elemX = width / 2 - sliderW / 2;
		
		this.isHidden = new CheckboxWidget(elemX, ey, ew, ROW_HEIGHT, lang("wp_hidden"), waypoint.hidden);
		this.isTrackable = new CheckboxWidget(elemX + 100, ey, ew, ROW_HEIGHT, lang("wp_tracking"), waypoint.tracking);
		this.isRenderable = new CheckboxWidget(elemX + 200, ey, ew, ROW_HEIGHT, lang("wp_render"), waypoint.render);
		children.add(isHidden);
		children.add(isTrackable);
		children.add(isRenderable);

		ey += row * 1.25;

		IntegerRange maxRangeConfig = JustMapClient.getConfig().getEntry("max_render_dist");
		final int SHOW_RANGE_MAX = maxRangeConfig.maxValue();
		this.showRange = waypoint.showRange;
		children.add(new SliderWidget(elemX, ey, sliderW, ROW_HEIGHT, LiteralText.EMPTY, (double) this.showRange / SHOW_RANGE_MAX) {
			{
				this.updateMessage();
			}

			@Override
			protected void updateMessage() {
				this.setMessage(new LiteralText(lang("wp_render_dist").getString() + WaypointEditorScreen.this.showRange));
			}

			@Override
			protected void applyValue() {
				WaypointEditorScreen.this.showRange = MathHelper.floor(MathHelper.clampedLerp(0, SHOW_RANGE_MAX, this.value));
			}
		});
		
		ew = 60;
		ey = height - (ROW_HEIGHT / 2 + 16);
		ButtonWidget saveButton = new ButtonWidget(center - ew - 2, ey, ew, ROW_HEIGHT, lang("save"), (b) -> {
			save();
			onClose();
		});
		children.add(saveButton);

		ButtonWidget cancelButton = new ButtonWidget(center + 2, ey, ew, ROW_HEIGHT, lang("cancel"), (b) -> onClose());
		children.add(cancelButton);
		
		this.setInitialFocus(nameField);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
		super.render(matrixStack, mouseX, mouseY, delta);
		String dimensionName = info == null ? lang("unknown").asString() : I18n.translate(info.getFirst());
		drawCenteredText(matrixStack, textRenderer, dimensionName, center, 15, Colors.WHITE);
	}
	
	private void cycleColor(int i) {
		this.colorIndex += i;
		if (colorIndex < 0) {
			this.colorIndex = Waypoint.WAYPOINT_COLORS.length - 1;
		} else if (colorIndex >= Waypoint.WAYPOINT_COLORS.length) {
			this.colorIndex = 0;
		}
	}	
	
	private void cycleIcon(int i) {
		this.iconIndex += i;
		if (iconIndex < 0) {
			this.iconIndex = Waypoint.amountIcons();
		} else if (iconIndex >= Waypoint.amountIcons()) {
			this.iconIndex = 0;
		}
	}
	
	private void save() {
		this.waypoint.name = nameField.widget.getText();		
		int color = Waypoint.WAYPOINT_COLORS[colorIndex];
		if(Waypoint.getIcon(iconIndex) != null) {
			this.waypoint.setIcon(Waypoint.getIcon(iconIndex), color);
		} else {
			this.waypoint.color = color;
		}
		this.waypoint.hidden = isHidden.isChecked();
		this.waypoint.tracking = isTrackable.isChecked();
		this.waypoint.render = isRenderable.isChecked();
		
		int xPos = (xField.getText().isEmpty() || xField.getText().equals("-")) ? 0 : Integer.parseInt(xField.getText());
		int yPos = (yField.getText().isEmpty() || yField.getText().equals("-")) ? 0 : Integer.parseInt(yField.getText());
		int zPos = (zField.getText().isEmpty() || zField.getText().equals("-")) ? 0 : Integer.parseInt(zField.getText());
		
		this.waypoint.pos = new BlockPos(xPos, yPos, zPos);

		this.waypoint.showRange = this.showRange;
		
		if (onSaveCallback != null) {
			this.onSaveCallback.accept(waypoint);
		}
		
		WaypointKeeper.getInstance().saveWaypoints();
	}
	
	@Override
	public void onClose() {
		this.client.setScreen(parent);
	}
	
	@Override
	public void renderForeground(MatrixStack matrixStack) {
		int x = prevColorButton.x + prevColorButton.getWidth() + 2;
		int y = prevColorButton.y + 3;
		int w = nextColorButton.x - x - 2;
		int h = 12;
		
		int col = Waypoint.WAYPOINT_COLORS[colorIndex];
		
		Icon icon;
		if (iconIndex > 0) {
			icon = Waypoint.getIcon(iconIndex);			
		} else {
			icon = Waypoint.getColoredIcon(col);
		}
		int ix = center - icon.getWidth() / 2;
		int iy = y + ROW_HEIGHT + (ROW_HEIGHT / 2 - icon.getHeight() / 2);
		int color = iconIndex > 0 ? icon.color : col;
		this.borderedRect(matrixStack, x, y, w, h, color, 2, 0xFFCCCCCC);
		icon.draw(ix, iy);
	}
	
	@Override
	public void tick() {}
	
	private void rect(MatrixStack matrixStack, int x, int y, int w, int h, int color) {
		fill(matrixStack, x, y, x + w, y + h, color);
	}
	
	private void borderedRect(MatrixStack matrixStack, int x, int y, int w, int h, int color, int border, int borderColor) {
		int hb = border >> 1;
		this.rect(matrixStack, x, y, w, h, borderColor);
		this.rect(matrixStack, x + hb, y + hb, w - border, h - border, color);
	}
	
	private int getColorIndex(int color) {
		for (int i = 0; i < Waypoint.WAYPOINT_COLORS.length; i++) {
			if (Waypoint.WAYPOINT_COLORS[i] == color) {
				return i;
			}
		}		
		return 0;
	}
	
	private int getIconIndex(Icon icon) {
		if (icon == null) return 0;
		return icon.key;
	}
	
	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		if (int_1 == GLFW.GLFW_KEY_ENTER) {
			this.save();
			this.onClose();
			return true;
		}		
		return super.keyPressed(int_1, int_2, int_3);
	}
}
