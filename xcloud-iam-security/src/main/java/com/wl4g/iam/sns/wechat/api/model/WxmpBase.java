/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.iam.sns.wechat.api.model;

import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;

/**
 * {@link WxmpBase}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpBase {

	private String errcode;
	private String errmsg;

	public WxmpBase() {
		super();
	}

	public WxmpBase(String errcode, String errmsg) {
		super();
		this.errcode = errcode;
		this.errmsg = errmsg;
	}

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
	}

	/**
	 * Checking the response status code for success.
	 * 
	 * @param resp
	 * @return
	 */
	public final static boolean isSuccess(WxmpBase resp) {
		return resp != null && "0".equalsIgnoreCase(resp.getErrcode());
	}

	/**
	 * Check whether the {@link WxmpBase} status code is the expected value
	 * 
	 * @param resp
	 * @param errcode
	 * @return
	 */
	public final static boolean eq(WxmpBase resp, String errcode) {
		return !isNull(resp) && valueOf(errcode).equalsIgnoreCase(resp.getErrcode());
	}

}
