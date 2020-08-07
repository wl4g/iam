package com.wl4g.iam.sns.wechat.api.model;

import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;

/**
 * WechatMp APIs credentials bean of {@link WxmpAccessToken}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpAccessToken extends WxmpBase {

	/**
	 * Credential token value
	 */
	private String token;

	/**
	 * Effective time of voucher, unit: seconds
	 */
	private int expiresIn;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
	}

}