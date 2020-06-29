package ru.bulldog.justmap.advancedinfo;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import ru.bulldog.justmap.util.PosUtil;

public class CoordsInfo extends InfoText {

	public CoordsInfo(TextAlignment alignment, String text) {
		super(alignment, text);
	}

	@Override
	public void update() {
		this.setVisible(ClientParams.showPosition);
		if (visible) {
			this.setText(PosUtil.posToString(PosUtil.currentPos()));
		}
	}
}
