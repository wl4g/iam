package com.wl4g.iam.sns.wechat.api.model.menu;

import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;

/**
 * {@link WxmpButton} </br>
 * WeChat public platform (official account) menu button base class
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpButton {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
	}

}