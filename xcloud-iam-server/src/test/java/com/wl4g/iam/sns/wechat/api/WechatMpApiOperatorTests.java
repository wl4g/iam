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

import com.wl4g.IamServer;
import com.wl4g.iam.sns.wechat.api.model.menu.WxmpButton;
import com.wl4g.iam.sns.wechat.api.model.menu.WxmpCommonButton;
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
@SpringBootTest(classes = IamServer.class)
@FixMethodOrder(MethodSorters.JVM)
public class WechatMpApiOperatorTests {

	public static final String WXMP_APPID = "<yourAppId>";
	public static final String WXMP_APPSECRET = "<yourAppSecret>";

	@Autowired
	private WechatMpApiOperator operator;

	private WxmpMenu menu;

	@Before
	public void wrapWxmpMenu() {
		WxmpCommonButton btn1 = new WxmpCommonButton();
		btn1.setName("最新资讯");
		btn1.setType("click");
		btn1.setKey("latest_news");

		WxmpViewButton btn2 = new WxmpViewButton();
		btn2.setName("我的报告");
		btn2.setType("view");
		btn2.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WXMP_APPID
				+ "&redirect_uri=<yourServiceUri>&response_type=code&scope=snsapi_base&state=1#wechat_redirect");

		WxmpViewButton btn21 = new WxmpViewButton();
		btn21.setName("联系我们");
		btn21.setType("view");
		btn21.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WXMP_APPID
				+ "&redirect_uri=<yourServiceUri>&response_type=code&scope=snsapi_base&state=2#wechat_redirect");

		WxmpViewButton btn22 = new WxmpViewButton();
		btn22.setName("服务产品");
		btn22.setType("view");
		btn22.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WXMP_APPID
				+ "&redirect_uri=<yourServiceUri>&response_type=code&scope=snsapi_base&state=3#wechat_redirect");

		WxmpComplexButton mainBtn1 = new WxmpComplexButton();
		mainBtn1.setName("关于我们");
		mainBtn1.setSub_button(new WxmpViewButton[] { btn21, btn22 });

		WxmpViewButton btn31 = new WxmpViewButton();
		btn31.setName("我的订单");
		btn31.setType("view");
		btn31.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WXMP_APPID
				+ "&redirect_uri=<yourServiceUri>&response_type=code&scope=snsapi_base&state=4#wechat_redirect");

		WxmpViewButton btn32 = new WxmpViewButton();
		btn32.setName("我的报告");
		btn32.setType("view");
		btn32.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WXMP_APPID
				+ "&redirect_uri=<yourServiceUri>&response_type=code&scope=snsapi_base&state=5#wechat_redirect");

		WxmpComplexButton mainBtn2 = new WxmpComplexButton();
		mainBtn2.setName("会员专属");
		mainBtn2.setSub_button(new WxmpViewButton[] { btn31, btn32 });

		menu = new WxmpMenu();
		menu.setButton(new WxmpButton[] { btn1, btn2 });
		// All have secondary sub menu items
		// menu.setButton(new WxmpButton[] { mainBtn1, mainBtn2 });
	}

	@Test
	public void createWxmpMenu() {
		System.out.println("Creating wxmp menu... " + menu);
		System.out.println("Create wxmp menu result: " + operator.createWxmpMenu(menu));
	}

}
