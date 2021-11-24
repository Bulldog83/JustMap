package ru.bulldog.justmap.client.config;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import me.shedaniel.clothconfig2.impl.builders.FieldBuilder;
import net.minecraft.text.Text;

public class FloatSliderBuilder extends FieldBuilder<Float, FloatSliderEntry> {

	private Consumer<Float> saveConsumer = null;
	private Function<Float, Optional<Text[]>> tooltipSupplier = i -> Optional.empty();
	private final float value;
	private float max;
	private float min;
	private Function<Float, Text> textGetter = null;

	public FloatSliderBuilder(Text resetButtonKey, Text fieldNameKey, float value, float min, float max) {
		super(resetButtonKey, fieldNameKey);
		this.value = value;
		this.max = max;
		this.min = min;
	}

	public FloatSliderBuilder setErrorSupplier(Function<Float, Optional<Text>> errorSupplier) {
		this.errorSupplier = errorSupplier;
		return this;
	}

	public FloatSliderBuilder requireRestart() {
		requireRestart(true);
		return this;
	}

	public FloatSliderBuilder setTextGetter(Function<Float, Text> textGetter) {
		this.textGetter = textGetter;
		return this;
	}

	public FloatSliderBuilder setSaveConsumer(Consumer<Float> saveConsumer) {
		this.saveConsumer = saveConsumer;
		return this;
	}

	public FloatSliderBuilder setDefaultValue(Supplier<Float> defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public FloatSliderBuilder setDefaultValue(float defaultValue) {
		this.defaultValue = () -> defaultValue;
		return this;
	}

	public FloatSliderBuilder setTooltipSupplier(Function<Float, Optional<Text[]>> tooltipSupplier) {
		this.tooltipSupplier = tooltipSupplier;
		return this;
	}

	public FloatSliderBuilder setTooltipSupplier(Supplier<Optional<Text[]>> tooltipSupplier) {
		this.tooltipSupplier = i -> tooltipSupplier.get();
		return this;
	}

	public FloatSliderBuilder setTooltip(Optional<Text[]> tooltip) {
		this.tooltipSupplier = i -> tooltip;
		return this;
	}

	public FloatSliderBuilder setTooltip(Text... tooltip) {
		this.tooltipSupplier = i -> Optional.ofNullable(tooltip);
		return this;
	}

	public FloatSliderBuilder setMax(float max) {
		this.max = max;
		return this;
	}

	public FloatSliderBuilder setMin(float min) {
		this.min = min;
		return this;
	}

	@Override
	public FloatSliderEntry build() {
		@SuppressWarnings("deprecation")
		FloatSliderEntry entry = new FloatSliderEntry(getFieldNameKey(), min, max, value, getResetButtonKey(), defaultValue, saveConsumer, null, isRequireRestart());
		if (textGetter != null)
			entry.setTextGetter(textGetter);
		entry.setTooltipSupplier(() -> tooltipSupplier.apply(entry.getValue()));
		if (errorSupplier != null)
			entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
		return entry;
	}

}
