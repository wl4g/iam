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
package com.wl4g.iam.authc.credential;

import javax.annotation.Resource;

import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.iam.common.constant.ServiceIAMConstants.BEAN_SESSION_RESOURCE_MSG_BUNDLER;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.iam.authc.credential.secure.IamCredentialsSecurer;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.configure.ServerSecurityCoprocessor;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.i18n.SessionResourceMessageBundler;
import com.wl4g.iam.verification.CompositeSecurityVerifierAdapter;

/**
 * IAM based matcher
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
public abstract class IamBasedMatcher extends SimpleCredentialsMatcher {

	final protected SmartLogger log = getLogger(getClass());

	/**
	 * IAM verification handler
	 */
	@Autowired
	protected CompositeSecurityVerifierAdapter verifier;

	/**
	 * Matcher configuration properties
	 */
	@Autowired
	protected IamProperties config;

	/**
	 * Using Distributed Cache to Ensure Concurrency Control under Multi-Node
	 */
	@Autowired
	protected IamCacheManager cacheManager;

	/**
	 * IAM credentials securer
	 */
	@Autowired
	protected IamCredentialsSecurer securer;

	/**
	 * IAM security Coprocessor
	 */
	@Autowired
	protected ServerSecurityCoprocessor coprocessor;

	/**
	 * Delegate message source.
	 */
	@Resource(name = BEAN_SESSION_RESOURCE_MSG_BUNDLER)
	protected SessionResourceMessageBundler bundle;

}