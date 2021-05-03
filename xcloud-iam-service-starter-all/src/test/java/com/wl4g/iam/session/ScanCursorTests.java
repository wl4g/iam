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
package com.wl4g.iam.session;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.iam.common.constant.ServiceIAMConstants.CACHE_SESSION;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.junit4.SpringRunner;

import com.wl4g.StandaloneIam;
import com.wl4g.component.common.serialize.ProtostuffUtils;
import com.wl4g.component.support.cache.jedis.JedisClientFactoryBean;
import com.wl4g.component.support.cache.jedis.ScanCursor;
import com.wl4g.iam.core.session.IamSession;

import redis.clients.jedis.ScanParams;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StandaloneIam.class)
@FixMethodOrder(MethodSorters.JVM)
public class ScanCursorTests {

	@Autowired
	@Lazy
	private JedisClientFactoryBean factory;

	@Test
	public void test1() throws Exception {
		byte[] data = factory.getObject().get("iam_server:iam:session:id:1c315080e64b4731b011a14551a54c92".getBytes(UTF_8));
		System.out.println("IamSession: " + ProtostuffUtils.deserialize(data, IamSession.class));
	}

	@Test
	public void test2() throws Exception {
		byte[] match = ("iam_server" + CACHE_SESSION + "*").getBytes(UTF_8);
		ScanParams params = new ScanParams().count(200).match(match);

		ScanCursor<IamSession> sc = new ScanCursor<IamSession>(factory.getObject(), null, params) {
		}.open();
		System.out.println("ScanResult: " + sc);
		while (sc.hasNext()) {
			System.out.println("IamSession: " + sc.next());
		}

	}

}