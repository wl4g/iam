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
package com.wl4g.iam.handler;

import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.core.constants.IAMDevOpsConstants.BEAN_DELEGATE_MSG_SOURCE;

import javax.annotation.Resource;

import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.support.concurrent.locks.JedisLockManager;
import com.wl4g.iam.common.cache.IamCacheManager;
import com.wl4g.iam.common.handler.AuthenticatingHandler;
import com.wl4g.iam.common.i18n.SessionDelegateMessageBundle;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.configure.ServerSecurityCoprocessor;

/**
 * Abstract base iam authenticating handler.
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
public abstract class AbstractAuthenticatingHandler implements AuthenticatingHandler, InitializingBean {
	final protected SmartLogger log = getLogger(getClass());

	/**
	 * IAM server configuration properties
	 */
	@Autowired
	protected IamProperties config;

	/**
	 * IAM security context handler
	 */
	@Autowired
	protected ServerSecurityConfigurer configurer;

	/**
	 * IAM server security processor
	 */
	@Autowired
	protected ServerSecurityCoprocessor coprocessor;

	/**
	 * Key id generator
	 */
	@Autowired
	protected SessionIdGenerator idGenerator;

	/**
	 * Rest template
	 */
	@Autowired
	protected RestTemplate restTemplate;

	/**
	 * Delegate message source.
	 */
	@Resource(name = BEAN_DELEGATE_MSG_SOURCE)
	protected SessionDelegateMessageBundle bundle;

	/**
	 * Distributed locks.
	 */
	@Autowired
	protected JedisLockManager lockManager;

	/**
	 * Enhanced cache manager.
	 */
	@Autowired
	protected IamCacheManager cacheManager;

	@Override
	public void afterPropertiesSet() throws Exception {
	}

}