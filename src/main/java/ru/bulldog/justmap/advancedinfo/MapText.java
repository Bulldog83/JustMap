package ru.bulldog.justmap.advancedinfo;

import ru.bulldog.justmap.enums.TextAlignment;

public class MapText extends InfoText {

	public MapText(TextAlignment alignment, String text) {
		super(alignment, text);
	}

	@Override
	public void updateOnTick() {}

}
