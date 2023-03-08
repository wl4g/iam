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
package com.wl4g.iam.handler;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.BEAN_SESSION_RESOURCE_MSG_BUNDLER;

import javax.annotation.Resource;

import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.locks.JedisLockManager;
import com.wl4g.iam.common.i18n.SessionResourceMessageBundler;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.configure.ServerSecurityCoprocessor;
import com.wl4g.iam.core.cache.IamCacheManager;
import com.wl4g.iam.core.handler.AuthenticatingHandler;

/**
 * Abstract base iam authenticating handler.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
@SuppressWarnings("deprecation")
public abstract class AbstractAuthenticatingHandler implements AuthenticatingHandler, InitializingBean {
    final protected SmartLogger log = getLogger(getClass());

    /**
     * IAM server configuration properties
     */
    protected @Autowired IamProperties config;

    /**
     * IAM security context handler
     */
    protected @Autowired ServerSecurityConfigurer configurer;

    /**
     * IAM server security processor
     */
    protected @Autowired ServerSecurityCoprocessor coprocessor;

    /**
     * Key id generator
     */
    protected @Autowired SessionIdGenerator idGenerator;

    /**
     * Delegate message source.
     */
    protected @Resource(name = BEAN_SESSION_RESOURCE_MSG_BUNDLER) SessionResourceMessageBundler bundle;

    /**
     * Distributed locks.
     */
    protected @Autowired JedisLockManager lockManager;

    /**
     * Enhanced cache manager.
     */
    protected @Autowired IamCacheManager cacheManager;

    /**
     * Rest template
     */
    protected @Autowired RestTemplate restTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        Netty4ClientHttpRequestFactory factory = new Netty4ClientHttpRequestFactory();
        factory.setReadTimeout(10000);
        factory.setConnectTimeout(6000);
        factory.setMaxResponseSize(65535);
        // factory.setSslContext(sslContext);
        this.restTemplate = new RestTemplate(factory);
    }

}