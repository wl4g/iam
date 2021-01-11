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
package com.wl4g;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.wl4g.component.data.annotation.EnableComponentDBConfiguration;
//import com.wl4g.component.rpc.springcloud.feign.proxy.annotation.EnableFeignProviderProxies;

/**
 * {@link IamData}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-14
 * @sine v1.0
 * @see
 */
// @EnableFeignProviderProxies("com.wl4g.iam.data")
// @RibbonClient(name = "ribbon-iam-data")
@EnableComponentDBConfiguration("com.wl4g.iam.data")
@SpringBootApplication
public class IamData {

	public static void main(String[] args) {
		SpringApplication.run(IamData.class, args);
	}

}