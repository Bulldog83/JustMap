package old_files.justmap.advancedinfo;

import old_files.justmap.util.DataUtil;
import ru.bulldog.justmap.client.config.ClientSettings;
import old_files.justmap.enums.TextAlignment;
import old_files.justmap.util.PosUtil;

public class CoordsInfo extends InfoText {

	public CoordsInfo(TextAlignment alignment, String text) {
		super(alignment, text);
	}

	@Override
	public void update() {
		this.setVisible(ClientSettings.showPosition);
		if (visible) {
			this.setText(PosUtil.posToString(DataUtil.currentPos()));
		}
	}
}
