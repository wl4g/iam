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

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.wl4g.component.core.web.convert.annotation.EnableHumanModelConvert;
import com.wl4g.component.core.web.method.mapping.annotation.EnableSmartHandlerMapping;
import com.wl4g.component.core.web.versions.annotation.EnableApiVersionManagement;
import com.wl4g.component.data.annotation.EnableComponentsData;
import com.wl4g.iam.annotation.EnableIamServer;

@EnableHumanModelConvert("com.wl4g.iam.web")
@EnableSmartHandlerMapping("com.wl4g.iam.web")
@EnableApiVersionManagement
@EnableIamServer
@MapperScan("com.wl4g.iam.data")
@EnableComponentsData
@SpringBootApplication
public class IamServer {

	public static void main(String[] args) {
		SpringApplication.run(IamServer.class, args);
	}

}