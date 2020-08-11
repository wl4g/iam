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
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import com.wl4g.iam.test.annotation.EnableIamMockTest;
import com.wl4g.iam.test.annotation.EnableIamMockTest.IamMockOrganization;

/**
 * {@link IamExampleTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-08-10
 * @sine v1.0.0
 * @see
 */
@EnableIamMockTest(principalId = "${iam.example.principalId}", principal = "${iam.example.principal}", roles = "administrator,${iam.example.roles}", permissions = "home,${iam.example.permissions}", organizations = {
		@IamMockOrganization(type = "1", parent = "", name = "headquarters", areaId = "10001", organizationCode = "${iam.example.organization}") })
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class IamExampleTests {

	public static void main(String[] args) {
		SpringApplication.run(IamExampleTests.class, args);
	}

}