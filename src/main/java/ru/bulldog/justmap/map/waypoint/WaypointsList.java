package ru.bulldog.justmap.map.waypoint;

import com.mojang.datafixers.util.Pair;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.screen.MapScreen;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.RandomUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

public class WaypointsList extends MapScreen {
	private static class Entry implements Element {
		private MinecraftClient minecraft;
	
		private int x, y, width, height;
		
		private ButtonWidget editButton;
		private ButtonWidget deleteButton;
		private ButtonWidget tpButton;
		private Waypoint waypoint;
	
		public Entry(WaypointsList wayPointListEditor, int x, int y, int width, int height, Waypoint waypoint) {
			this.width = width;
			this.height = height + 2;
			this.waypoint = waypoint;
			
			this.minecraft = DataUtil.getMinecraft();
			
			this.editButton = new ButtonWidget(0, 0, 40, height, wayPointListEditor.lang("edit"), (b) -> wayPointListEditor.edit(waypoint));
			this.deleteButton = new ButtonWidget(0, 0, 40, height, wayPointListEditor.lang("delete"), (b) -> wayPointListEditor.delete(waypoint));
			this.tpButton = new ButtonWidget(0, 0, 40, height, wayPointListEditor.lang("teleport"), (b) -> wayPointListEditor.teleport(waypoint));
			
			this.setPosition(x, y);
		}
	
		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
			
			this.rightAlign(deleteButton, x + width - 2);
			this.rightAlign(editButton, deleteButton);
			
			this.editButton.y = y + 1;
			this.deleteButton.y = y + 1;
			
			if (tpButton != null) {
				this.rightAlign(tpButton, editButton);
				this.tpButton.y = y + 1;
			}
		}		
		
		public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
			TextRenderer font = minecraft.textRenderer;
			
			boolean hover = isMouseOver(mouseX, mouseY);
			int bgColor = hover ? 0x88AAAAAA : 0x88333333;
			fill(matrixStack, x, y, x + width, y + height, bgColor);
			
			int iconSize = height - 2;
			Icon icon = waypoint.getIcon();
			if (icon != null) {
				icon.draw(matrixStack, x, y + 1, iconSize, iconSize);
			} else {
				RenderUtil.drawDiamond(x, y + 1, iconSize, iconSize, waypoint.color);
			}
			
			int stringY = y + 7;			
			int nameX = x + iconSize + 2;

			RenderUtil.DRAWER.drawStringWithShadow(matrixStack, font, waypoint.name, nameX, stringY, Colors.WHITE);
			
			int posX = tpButton.x - 5;
			RenderUtil.drawRightAlignedString(matrixStack, waypoint.pos.toShortString(), posX, stringY, Colors.WHITE);
			
			if (RuleUtil.allowTeleportation()) {
				tpButton.render(matrixStack, mouseX, mouseY, delta);
			}
			editButton.render(matrixStack, mouseX, mouseY, delta);
			deleteButton.render(matrixStack, mouseX, mouseY, delta);
		}
	
		@Override
		public boolean mouseClicked(double double_1, double double_2, int int_1) {
			return editButton.mouseClicked(double_1, double_2, int_1) ||
				   deleteButton.mouseClicked(double_1, double_2, int_1) ||
				   tpButton.mouseClicked(double_1, double_2, int_1);
		}
	
		@Override
		public boolean mouseReleased(double double_1, double double_2, int int_1) {
			return editButton.mouseReleased(double_1, double_2, int_1) ||
				   deleteButton.mouseReleased(double_1, double_2, int_1) ||
				   tpButton.mouseReleased(double_1, double_2, int_1);
		}
	
		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}
	
		private void rightAlign(ButtonWidget toAlign, ButtonWidget from) {
			toAlign.x = from.x - toAlign.getWidth() - 1;
		}
	
		private void rightAlign(ButtonWidget toAlign, int right) {
			toAlign.x = right - toAlign.getWidth();
		}
	}
	
	private static final Text TITLE = new TranslatableText(JustMap.MODID + ".gui.screen.waypoints_list");
	
	private WaypointKeeper keeper = WaypointKeeper.getInstance();
	private Identifier currentDim;
	private int currentDimIndex = 0;
	private List<Identifier> dimensions;
	private List<Waypoint> wayPoints;
	private List<Entry> entries = new ArrayList<>();

	private int scrollAmount = 0;
	private int maxScroll = 0;
	private int screenWidth;
	
	private ButtonWidget prevDimensionButton, nextDimensionButton;
	private ButtonWidget addButton, closeButton;
	
	public WaypointsList(Screen parent) {
		super(TITLE, parent);
		
		this.dimensions = keeper.getDimensions();
		
		Identifier overworld = DimensionType.OVERWORLD_REGISTRY_KEY.getValue();
		Identifier nether = DimensionType.THE_NETHER_REGISTRY_KEY.getValue();
		Identifier theEnd = DimensionType.THE_END_REGISTRY_KEY.getValue();
		if (!dimensions.contains(overworld)) {
			dimensions.add(overworld);
		}		
		if (!dimensions.contains(nether)) {
			dimensions.add(nether);
		}		
		if (!dimensions.contains(theEnd)) {
			dimensions.add(theEnd);
		}
	}

	@Override
	protected void init() {
		this.center = width / 2;		
		this.screenWidth = center > 480 ? center : width > 480 ? 480 : width;
		this.x = center - screenWidth / 2;		
		this.prevDimensionButton = new ButtonWidget(x + 10, 6, 20, 20, new LiteralText("<"), (b) -> cycleDimension(-1));
		this.nextDimensionButton = new ButtonWidget(x + screenWidth - 30, 6, 20, 20, new LiteralText(">"), (b) -> cycleDimension(1));		
		this.addButton = new ButtonWidget(center - 62, height - 26, 60, 20, lang("create"), (b) -> add());
		this.closeButton = new ButtonWidget(center + 2, height - 26, 60, 20, lang("close"), (b) -> onClose());
		this.currentDim = client.world.getDimensionRegistryKey().getValue();
		this.currentDimIndex = getDimIndex(currentDim);
		
		reset();
	}
	
	private void createEntries() {
		entries.clear();
	
		int y = 40;
		for (Waypoint wp : wayPoints) {
			Entry entry = new Entry(this, x + 10, scrollAmount + y, screenWidth - 20, 20, wp);
			entries.add(entry);
			
			y += entry.height;
		}
	}
	
	private void updateEntries() {
		int y = 40;
		for (Entry entry : entries) {
			entry.setPosition(x + 10, scrollAmount + y);
			y += entry.height;
		}
	}
	
	
	private void cycleDimension(int i) {
		currentDimIndex += i;
		if (currentDimIndex >= dimensions.size()) {
			currentDimIndex = 0;
		}
		else if (currentDimIndex < 0) {
			currentDimIndex = dimensions.size() - 1;
		}
		
		currentDim = dimensions.get(currentDimIndex);
		reset();
	}
	
	private int getDimIndex(Identifier dim) {
		return dimensions.indexOf(dim);
	}
	
	public void reset() {
		this.info = this.getDimensionInfo(currentDim);
		
		this.wayPoints = keeper.getWaypoints(currentDim, false);
		createEntries();
		this.maxScroll = wayPoints.size() * 20;
		
		this.children.clear();
		this.children.addAll(entries);
		this.children.add(addButton);
		this.children.add(closeButton);
		this.children.add(prevDimensionButton);
		this.children.add(nextDimensionButton);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
		super.render(matrixStack, mouseX, mouseY, delta);
		
		entries.forEach(e -> e.render(matrixStack, mouseX, mouseY, delta));
		
		String dimensionName = info == null ? lang("unknown").getString() : I18n.translate(info.getFirst());
		this.drawCenteredString(matrixStack, textRenderer, dimensionName, center, 15, Colors.WHITE);
		this.drawScrollBar();
	}
	
	private Pair<String, Identifier> getDimensionInfo(Identifier dim) {
		String key = "unknown";
		if (dim != null) {
			 key = dim.toString();
		}		
		return DIMENSION_INFO.getOrDefault(key, null);
	}
	
	private void drawScrollBar() {}
	
	private void edit(Waypoint waypoint) {
		client.openScreen(new WaypointEditor(waypoint, this, null));
	}
	
	private void add() {
		Waypoint waypoint = new Waypoint();
		waypoint.dimension = currentDim;
		waypoint.color = RandomUtil.getElement(Waypoint.WAYPOINT_COLORS);
		waypoint.pos = client.player.getBlockPos();
		waypoint.name = "Waypoint";
		
		client.openScreen(new WaypointEditor(waypoint, this, keeper::addNew));
	}
	
	private void delete(Waypoint waypoint) {
		keeper.remove(waypoint);
		keeper.saveWaypoints();
		reset();
	}
	
	public void teleport(Waypoint waypoint) {
		if (!client.world.getDimensionRegistryKey().getValue().equals(currentDim)) return;
		int y = waypoint.pos.getY() > 0 ? waypoint.pos.getY() : (Dimension.isNether(client.world.getDimensionRegistryKey()) ? 128 : 64);
		this.client.player.sendChatMessage("/tp " + this.client.player.getName().asString() + " " + waypoint.pos.getX() + " " + y + " " + waypoint.pos.getZ());
		this.onClose();
	}
	
	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		scrollAmount = MathUtil.clamp(scrollAmount + (int) (double_3 * 12), -maxScroll + 80, 0);
		updateEntries();
		
		return true;
	}
	
	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		if (int_1 == GLFW.GLFW_KEY_U) {
			this.onClose();
			return true;
		}		
		return super.keyPressed(int_1, int_2, int_3);
	}
}
