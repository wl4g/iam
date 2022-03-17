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
package com.wl4g.iam.sns.wechat;

import static com.wl4g.infra.common.serialize.JacksonUtils.getDefaultObjectMapper;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.wl4g.iam.sns.wechat.api.model.WxmpAccessToken;

/**
 * {@link WxmpBaseTests} 
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpBaseTests {

	public static void main(String[] args) {
		String json = "{\"access_token\":\"36_CL4KLq-geWM_buOd8DPnp_i5_m-ou3up28iLYlIk5IWya5-gOc7xg5ffJ6qZ4mkEZ9oYxPBDYU8tUzRU3vAV7bYKau5wi4xQL0uh26-ks6E-R1LZ14S9K2RMszWa12xzvI3W-BPiC3WmAU8SGPKjAAALUP\",\"expires_in\":7200}";

		WxmpAccessToken token = parseJSON(json, WxmpAccessToken.class);
		System.out.println(token);

		getDefaultObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		token = parseJSON(json, WxmpAccessToken.class);
		System.out.println(token);

	}

}
