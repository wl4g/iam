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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.wl4g.LocalIamWeb;
import static com.wl4g.iam.common.constant.ConfigIAMConstants.KEY_IAM_CONFIG_PREFIX;
import com.wl4g.iam.config.properties.SnsProperties;
import com.wl4g.iam.sns.wechat.api.model.menu.WxmpButton;
import com.wl4g.iam.sns.wechat.api.model.menu.WxmpComplexButton;
import com.wl4g.iam.sns.wechat.api.model.menu.WxmpMenu;
import com.wl4g.iam.sns.wechat.api.model.menu.WxmpViewButton;

/**
 * {@link WechatMpApiOperatorTests}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LocalIamWeb.class, properties = { KEY_IAM_CONFIG_PREFIX + ".sns.wechat-mp.app-id=${APP_ID}",
		KEY_IAM_CONFIG_PREFIX + ".sns.wechat-mp.app-secret=${APP_SECRET}" })
@FixMethodOrder(MethodSorters.JVM)
public class WechatMpApiOperatorTests {

	@Autowired
	private SnsProperties snsConfig;

	@Autowired
	private WechatMpApiOperator operator;

	private WxmpMenu menu;

	@Before
	public void wrapWxmpMenu() {
		WxmpComplexButton btn1 = new WxmpComplexButton();
		btn1.setName("XCloud DevOps");
		WxmpViewButton btn11 = new WxmpViewButton();
		btn11.setName("菜单11");
		btn11.setType("view");
		btn11.setUrl("http://wl4g.com/mb-product-energy.html");
		WxmpViewButton btn12 = new WxmpViewButton();
		btn12.setName("菜单12");
		btn12.setType("view");
		btn12.setUrl("http://wl4g.com/mb-product-solution.html");
		WxmpViewButton btn13 = new WxmpViewButton();
		btn13.setName("菜单13");
		btn13.setType("view");
		btn13.setUrl("http://wl4g.com/mb-product-service.html");
		btn1.setSub_button(new WxmpViewButton[] { btn11, btn12, btn13 });

		WxmpComplexButton btn2 = new WxmpComplexButton();
		btn2.setName("关于我们");
		WxmpViewButton btn21 = new WxmpViewButton();
		btn21.setName("公司简介");
		btn21.setType("view");
		btn21.setUrl("http://wl4g.com/mb-about-introduce.html");
		WxmpViewButton btn22 = new WxmpViewButton();
		btn22.setName("联系我们");
		btn22.setType("view");
		btn22.setUrl("http://wl4g.com/mb-about-contact.html");
		WxmpViewButton btn23 = new WxmpViewButton();
		btn23.setName("最新动态");
		btn23.setType("view");
		btn23.setUrl("http://wl4g.com/mb-about-dynamic.html");
		btn2.setSub_button(new WxmpViewButton[] { btn21, btn22, btn23 });

		WxmpComplexButton btn3 = new WxmpComplexButton();
		btn3.setName("菜单3");
		WxmpViewButton btn31 = new WxmpViewButton();
		btn31.setName("菜单31");
		btn31.setType("view");
		// String redirect_uri =
		// "https://sso-services.wl4g.com/sso/sns/wechatmp/callback?which=client_auth";
		// 无需redirect_url参数，iam已支持自动检测使用默认值
		// String redirect_uri =
		// "https%3A%2F%2Fsso-services.wl4g.com%2Fsso%2Fsns%2Fwechatmp%2Fcallback%3Fwhich%3Dclient_auth%26state%3D1%26service%3Dmobile%26redirect_url%3Dhttps://m-services.wl4g.com/#/index";
		String redirect_uri = "https%3A%2F%2Fsso-services.wl4g.com%2Fsso%2Fsns%2Fwechatmp%2Fcallback%3Fwhich%3Dclient_auth%26state%3D1%26service%3Dmobile";
		btn31.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + snsConfig.getWechatMp().getAppId()
				+ "&redirect_uri=" + redirect_uri + "&response_type=code&scope=snsapi_userinfo#wechat_redirect");
		WxmpViewButton btn32 = new WxmpViewButton();
		btn32.setName("菜单32");
		btn32.setType("view");
		btn32.setUrl("http://wl4g.com/mb-wl4g-app.html");
		WxmpViewButton btn33 = new WxmpViewButton();
		btn33.setName("菜单33");
		btn33.setType("view");
		btn33.setUrl("http://wl4g.com/mb-ad-service.html");
		btn3.setSub_button(new WxmpViewButton[] { btn31, btn32, btn33 });

		menu = new WxmpMenu();
		menu.setButton(new WxmpButton[] { btn1, btn2, btn3 });
	}

	@Test
	public void createWxmpMenuCase1() {
		System.out.println("Creating wxmp menu... " + menu);
		System.out.println("Created wxmp menu result: " + operator.createWxmpMenu(menu));
	}
}
