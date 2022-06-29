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
package com.wl4g.iam.client.handler;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.iam.client.configure.IamConfigurer;

/**
 * {@link AbstractWebHandler}
 *
 * @author James Wong <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-02
 * @since
 */
public abstract class AbstractWebHandler implements WebIamHandler {

	/** {@link SmartLogger} */
	protected SmartLogger log = getLogger(getClass());

	/** {@link IamConfigurer} */
	protected final IamConfigurer configurer;

	public AbstractWebHandler(IamConfigurer configurer) {
		notNullOf(configurer, "configurer");
		this.configurer = configurer;
	}

}
