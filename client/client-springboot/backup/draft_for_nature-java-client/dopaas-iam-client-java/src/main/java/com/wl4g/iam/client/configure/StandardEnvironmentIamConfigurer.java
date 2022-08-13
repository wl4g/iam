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
package com.wl4g.iam.client.configure;

import static java.lang.System.getenv;

/**
 * {@link StandardEnvironmentIamConfigurer}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020-09-04
 * @since
 */
public class StandardEnvironmentIamConfigurer implements IamConfigurer {

	@Override
	public String loadAppSecret(String appId) {
		return getenv(IAM_AUTHC_SIGN_APPSECRET_PREFIX.concat(appId));
	}

	/** IAM openapi validating for security token, environment key-prefix */
	public static final String IAM_AUTHC_SIGN_APPSECRET_PREFIX = "IAM_AUTHC_SIGN_APPSECRET_";

}
