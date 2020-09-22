package ru.bulldog.justmap.client.config;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;

import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

public class FloatSliderEntry extends TooltipListEntry<Float> {

	protected Slider sliderWidget;
	protected ButtonWidget resetButton;
	protected AtomicDouble value;
	protected final float orginial;
	private float minimum, maximum;
	private Consumer<Float> saveConsumer;
	private Supplier<Float> defaultValue;
	private Function<Float, String> textGetter = value -> String.format("Value: %.1f", value);
	private List<Element> widgets;	
	private TextRenderer textRenderer;
	
	@Deprecated
	public FloatSliderEntry(String fieldName, float minimum, float maximum, float value, String resetButtonKey, Supplier<Float> defaultValue, Consumer<Float> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier, boolean requiresRestart) {
		super(fieldName, tooltipSupplier, requiresRestart);
		MinecraftClient client = MinecraftClient.getInstance();		
		this.textRenderer = client.textRenderer;
		this.orginial = value;
		this.defaultValue = defaultValue;
		this.value = new AtomicDouble(value);
		this.saveConsumer = saveConsumer;
		this.maximum = maximum;
		this.minimum = minimum;
		this.sliderWidget = new Slider(0, 0, 152, 20, ((double) this.value.get() - minimum) / Math.abs(maximum - minimum));
		String resetString = I18n.translate(resetButtonKey);
		int width = textRenderer.getStringWidth(resetString);
		this.resetButton = new ButtonWidget(0, 0, width + 6, 20, resetString, widget -> {
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
	
	public Function<Float, String> getTextGetter() {
		return textGetter;
	}
	
	public FloatSliderEntry setTextGetter(Function<Float, String> textGetter) {
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
		sliderWidget.setValue((MathHelper.clamp(value, minimum, maximum) - minimum) / (double) Math.abs(maximum - minimum));
		this.value.set(Math.min(Math.max(value, minimum), maximum));
		sliderWidget.updateMessage();
	}
	
	@Override
	public boolean isEditable() {
		return super.isEditable() || getValue() != orginial;
	}
	
	@Override
	public Optional<Float> getDefaultValue() {
		return defaultValue == null ? Optional.empty() : Optional.ofNullable(defaultValue.get());
	}
	
	@Override
	public List<? extends Element> children() {
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
	public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		super.render(index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
		Window window = MinecraftClient.getInstance().getWindow();
		this.resetButton.active = isEditable() && getDefaultValue().isPresent() && defaultValue.get() != value.get();
		this.resetButton.y = y;
		this.sliderWidget.active = isEditable();
		this.sliderWidget.y = y;
		String displayedFieldName = this.getFieldName();
		if (textRenderer.isRightToLeft()) {
			textRenderer.drawWithShadow(displayedFieldName, window.getScaledWidth() - x - textRenderer.getStringWidth(displayedFieldName), y + 5, getPreferredTextColor());
			this.resetButton.x = x;
			this.sliderWidget.x = x + resetButton.getWidth() + 1;
		} else {
			textRenderer.drawWithShadow(displayedFieldName, x, y + 5, getPreferredTextColor());
			this.resetButton.x = x + entryWidth - resetButton.getWidth();
			this.sliderWidget.x = x + entryWidth - 150;
		}
		this.sliderWidget.setWidth(150 - resetButton.getWidth() - 2);
		this.resetButton.render(mouseX, mouseY, delta);
		this.sliderWidget.render(mouseX, mouseY, delta);
	}
	
	private class Slider extends SliderWidget {
		protected Slider(int x, int y, int width, int height, double value) {
			super(x, y, width, height, value);
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
			if (!isEditable()) return false;
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (!isEditable()) return false;
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
		
		public void setValue(double value) {
			this.value = value;
		}
	}

}
