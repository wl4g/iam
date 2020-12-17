package com.wl4g.iam.sns.wechat.api.model.menu;

import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;

/**
 * Common button (sub button), here the definition of submenu is as follows:
 * menu item without submenu may be secondary menu item or primary menu without
 * secondary menu. This type of submenu item must contain three attributes:
 * type, name, and key.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpCommonButton extends WxmpButton {

	private String type;
	private String key;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
	}

}