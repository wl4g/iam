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
package com.wl4g.iam.client.handler.authc;

import com.wl4g.iam.client.configure.IamConfigurer;
import com.wl4g.iam.client.core.HttpIamRequest;
import com.wl4g.iam.client.core.HttpIamResponse;
import com.wl4g.iam.client.handler.AbstractWebHandler;

/**
 * {@link FastCasWebHandler}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2020-09-02
 * @since
 */
public class FastCasWebHandler extends AbstractWebHandler {

	public FastCasWebHandler(IamConfigurer configurer) {
		super(configurer);
	}

	@Override
	public boolean preHandle(HttpIamRequest request, HttpIamResponse response, Object handler) throws Exception {
		return super.preHandle(request, response, handler);
	}

}
