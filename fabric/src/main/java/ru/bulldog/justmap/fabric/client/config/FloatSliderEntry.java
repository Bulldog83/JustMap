package ru.bulldog.justmap.fabric.client.config;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FloatSliderEntry extends TooltipListEntry<Float> {

	protected Slider sliderWidget;
	protected Button resetButton;
	protected AtomicDouble value;
	protected final float orginial;
	private float minimum, maximum;
	private final Consumer<Float> saveConsumer;
	private final Supplier<Float> defaultValue;
	private Function<Float, Component> textGetter = value -> new TextComponent(String.format("Value: %.1f", value));
	private final List<GuiEventListener> widgets;
	private final Font font;
	
	@Deprecated
	public FloatSliderEntry(Component fieldName, float minimum, float maximum, float value, Component resetButtonKey, Supplier<Float> defaultValue, Consumer<Float> saveConsumer) {
		this(fieldName, minimum, maximum, value, resetButtonKey, defaultValue, saveConsumer, null);
	}
	
	@Deprecated
	public FloatSliderEntry(Component fieldName, float minimum, float maximum, float value, Component resetButtonKey, Supplier<Float> defaultValue, Consumer<Float> saveConsumer, Supplier<Optional<Component[]>> tooltipSupplier) {
		this(fieldName, minimum, maximum, value, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, false);
	}
	
	@Deprecated
	public FloatSliderEntry(Component fieldName, float minimum, float maximum, float value, Component resetButtonKey, Supplier<Float> defaultValue, Consumer<Float> saveConsumer, Supplier<Optional<Component[]>> tooltipSupplier, boolean requiresRestart) {
		super(fieldName, tooltipSupplier, requiresRestart);
		Minecraft client = Minecraft.getInstance();		
		this.font = client.font;
		this.orginial = value;
		this.defaultValue = defaultValue;
		this.value = new AtomicDouble(value);
		this.saveConsumer = saveConsumer;
		this.maximum = maximum;
		this.minimum = minimum;
		this.sliderWidget = new Slider(0, 0, 152, 20, ((double) this.value.get() - minimum) / Math.abs(maximum - minimum));
		int width = font.width(resetButtonKey);
		this.resetButton = new Button(0, 0, width + 6, 20, resetButtonKey, widget -> {
			setValue(defaultValue.get());
		});
		this.sliderWidget.setMessage(textGetter.apply((float) FloatSliderEntry.this.value.get()));
		this.widgets = Lists.newArrayList(sliderWidget, resetButton);
	}
	
	@Override
	public void save() {
		if (saveConsumer != null)
			saveConsumer.accept(getValue());
	}
	
	public Function<Float, Component> getTextGetter() {
		return textGetter;
	}
	
	public FloatSliderEntry setTextGetter(Function<Float, Component> textGetter) {
		this.textGetter = textGetter;
		this.sliderWidget.setMessage(textGetter.apply((float) FloatSliderEntry.this.value.get()));
		return this;
	}
	
	@Override
	public Float getValue() {
		return (float) value.get();
	}
	
	@Deprecated
	public void setValue(double value) {
		sliderWidget.setValue((Mth.clamp(value, minimum, maximum) - minimum) / (double) Math.abs(maximum - minimum));
		this.value.set(Math.min(Math.max(value, minimum), maximum));
		sliderWidget.updateMessage();
	}
	
	@Override
	public boolean isEdited() {
		return super.isEdited() || getValue() != orginial;
	}
	
	@Override
	public Optional<Float> getDefaultValue() {
		return defaultValue == null ? Optional.empty() : Optional.ofNullable(defaultValue.get());
	}
	
	@Override
	public List<? extends GuiEventListener> children() {
		return widgets;
	}
	
	public FloatSliderEntry setMaximum(float maximum) {
		this.maximum = maximum;
		return this;
	}
	
	public FloatSliderEntry setMinimum(float minimum) {
		this.minimum = minimum;
		return this;
	}
	
	@Override
	public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		super.render(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
		Window window = Minecraft.getInstance().getWindow();
		this.resetButton.active = isEditable() && getDefaultValue().isPresent() && defaultValue.get() != value.get();
		this.resetButton.y = y;
		this.sliderWidget.active = isEditable();
		this.sliderWidget.y = y;
		Component displayedFieldName = getDisplayedFieldName();
		if (font.isBidirectional()) {
			font.drawShadow(matrices, displayedFieldName, window.getGuiScaledWidth() - x - font.width(displayedFieldName), y + 5, getPreferredTextColor());
			this.resetButton.x = x;
			this.sliderWidget.x = x + resetButton.getWidth() + 1;
		} else {
			font.drawShadow(matrices, displayedFieldName, x, y + 5, getPreferredTextColor());
			this.resetButton.x = x + entryWidth - resetButton.getWidth();
			this.sliderWidget.x = x + entryWidth - 150;
		}
		this.sliderWidget.setWidth(150 - resetButton.getWidth() - 2);
		resetButton.render(matrices, mouseX, mouseY, delta);
		sliderWidget.render(matrices, mouseX, mouseY, delta);
	}
	
	private class Slider extends AbstractSliderButton {
		protected Slider(int x, int y, int width, int height, double value) {
			super(x, y, width, height, NarratorChatListener.NO_TITLE, value);
		}
		
		@Override
		public void updateMessage() {
			setMessage(textGetter.apply((float) FloatSliderEntry.this.value.get()));
		}
		
		@Override
		protected void applyValue() {
			float val = Math.round(value * 100) / 100F;
			FloatSliderEntry.this.value.set(minimum + Math.abs(maximum - minimum) * val);
		}
		
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (!isEditable())
				return false;
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (!isEditable())
				return false;
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
		
		public void setValue(double value) {
			this.value = value;
		}
	}

}
