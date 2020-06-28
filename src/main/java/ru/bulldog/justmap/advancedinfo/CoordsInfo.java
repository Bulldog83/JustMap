package ru.bulldog.justmap.advancedinfo;

import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import ru.bulldog.justmap.util.PosUtil;

public class CoordsInfo extends InfoText {

	public CoordsInfo(TextAlignment alignment, String text) {
		super(alignment, text);
	}

	@Override
	public void update() {
		this.setText(PosUtil.posToString(PosUtil.currentPos()));		
	}
}
