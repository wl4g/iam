package com.wl4g.iam.sns.wechat.api.model.menu;

import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;

/**
 * {@link WxmpMenu} bean. The menu object contains multiple menu items (up to
 * 3). These menu items can be sub menu items (Level 1 menu without secondary
 * menu) or parent menu items (menu items containing secondary menu)
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpMenu {

	private WxmpButton[] button;

	public WxmpButton[] getButton() {
		return button;
	}

	public void setButton(WxmpButton[] button) {
		this.button = button;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
	}

}