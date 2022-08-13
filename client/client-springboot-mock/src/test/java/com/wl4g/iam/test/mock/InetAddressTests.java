/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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
package com.wl4g.iam.test.mock;

import java.net.InetAddress;

import com.google.common.net.InetAddresses;

public class InetAddressTests {

	public static void main(String[] args) throws Exception {
		System.out.println(InetAddresses.isInetAddress("127.0.0.1"));
		System.out.println(InetAddresses.isInetAddress("localhost"));
		System.out.println(InetAddress.getByName("localhost").getCanonicalHostName());
		System.out.println(InetAddress.getByName("localhost").getHostName());
		System.out.println(InetAddress.getByName("localhost").getHostAddress());
		System.out.println(new String(InetAddress.getByName("localhost").getAddress()));
	}

}
