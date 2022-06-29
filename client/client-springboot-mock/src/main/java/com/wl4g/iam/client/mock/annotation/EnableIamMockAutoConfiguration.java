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
package com.wl4g.iam.client.mock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.wl4g.iam.client.annotation.EnableIamClient;
import com.wl4g.iam.client.mock.config.MockIamAutoConfiguration;

/**
 * It is used for convenient mock debugging in the development phase. Note: it
 * is mutually exclusive with {@link EnableIamClient}
 *
 * @author James Wong <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-20
 * @since
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Import({ MockIamAutoConfiguration.class })
@EnableIamClient
public @interface EnableIamMockAutoConfiguration {

	/**
	 * Mock sample configuration location, default: application-mock.conf
	 * 
	 * @return
	 */
	String location() default DEFAULT_MOCK_CONF_LOCATION;

	/** Default IAM mock configuration location. */
	public static final String DEFAULT_MOCK_CONF_LOCATION = "application-mock.conf";

}