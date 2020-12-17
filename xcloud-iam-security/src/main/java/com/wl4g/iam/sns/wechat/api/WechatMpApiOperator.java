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
package com.wl4g.iam.sns.wechat.api;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Collections.singletonMap;
import static java.util.Objects.nonNull;

import java.util.HashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.config.properties.SnsProperties;
import com.wl4g.iam.sns.exception.InvalidAccessTokenException;
import com.wl4g.iam.sns.wechat.api.model.WxmpAccessToken;
import com.wl4g.iam.sns.wechat.api.model.WxmpBase;
import com.wl4g.iam.sns.wechat.api.model.menu.WxmpMenu;

/**
 * {@link WechatMpApiOperator}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-07
 * @since
 */
public class WechatMpApiOperator implements InitializingBean {

	protected SmartLogger log = getLogger(getClass());

	/** {@link IamProperties} */
	protected final IamProperties config;

	/** {@link SnsProperties} */
	protected final SnsProperties snsConfig;

	protected RestTemplate restTemplate;

	public WechatMpApiOperator(IamProperties config, SnsProperties snsConfig) {
		notNullOf(config, "config");
		notNullOf(snsConfig, "snsConfig");
		this.config = config;
		this.snsConfig = snsConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Customization configuration
		this.restTemplate = new RestTemplate();
		// MappingJackson2HttpMessageConverter converter = new
		// MappingJackson2HttpMessageConverter();
		// converter.getObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
		// false);
		// this.restTemplate.setMessageConverters(singletonList(converter));
	}

	/**
	 * Create refresh WechatMp menus
	 * 
	 * @param menu
	 * @return
	 */
	public boolean createWxmpMenu(WxmpMenu menu) {
		// Gets access_token
		WxmpAccessToken token = getWxmpAccessToken(snsConfig.getWechatMp().getAppId(), snsConfig.getWechatMp().getAppSecret());
		log.info("Got wxmp token: {}", token);
		if (nonNull(token)) {
			// Create wxmp menus
			WxmpBase res = restTemplate
					.postForObject(DEFAULT_MENU_CREATE_URI, menu, WxmpBase.class, singletonMap("ACCESS_TOKEN", token.getToken()));
			log.info("Create wxmp menus result: {}", res);
			return WxmpBase.isSuccess(res);
		}

		return false;
	}

	/**
	 * Gets WechatMp access_token
	 * 
	 * @param appId
	 *            WechatMp app ID
	 * @param appSecret
	 *            WechatMp app secret key
	 * @return
	 * @throws InvalidAccessTokenException
	 */
	public WxmpAccessToken getWxmpAccessToken(String appId, String appSecret) throws InvalidAccessTokenException {
		ResponseEntity<WxmpAccessToken> resp = restTemplate.getForEntity(DEFAULT_ACCESSTOKEN_URI, WxmpAccessToken.class,
				new HashMap<String, String>() {
					private static final long serialVersionUID = 1L;
					{
						put("APPID", appId);
						put("APPSECRET", appSecret);
					}
				});

		if (nonNull(resp)) {
			WxmpAccessToken token = resp.getBody();
			if (nonNull(token) && WxmpBase.isSuccess(token)) {
				return resp.getBody();
			}
		}

		throw new InvalidAccessTokenException("wechatmp", resp.toString());
	}

	/**
	 * Gets wechatmp access token URL, (HTTP.GET) Limited to 200(times/day)
	 */
	private final static String DEFAULT_ACCESSTOKEN_URI = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={APPID}&secret={APPSECRET}";

	/**
	 * Menu creation (HTTP.POST) limit 100(times/day) </br>
	 * 
	 * @see https://developers.weixin.qq.com/doc/offiaccount/Custom_Menus/Creating_Custom-Defined_Menu.html
	 */
	private final static String DEFAULT_MENU_CREATE_URI = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token={ACCESS_TOKEN}";

}
