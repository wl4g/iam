package com.wl4g.iam.sns.wechat.api.model.menu;

import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;

/**
 * The complex button (parent button) contains the first level menu of secondary
 * menu items. This type of menu item has two properties: name and sub_ Button,
 * and sub_ Button is an array of submenu items
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpComplexButton extends WxmpButton {

	private WxmpButton[] sub_button;

	public WxmpButton[] getSub_button() {
		return sub_button;
	}

	public void setSub_button(WxmpButton[] sub_button) {
		this.sub_button = sub_button;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
	}

}