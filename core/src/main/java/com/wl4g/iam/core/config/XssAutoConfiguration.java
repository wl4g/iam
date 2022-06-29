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
package com.wl4g.iam.core.config;

import static com.wl4g.iam.core.config.XssProperties.KEY_XSS_PREFIX;

import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.iam.core.security.xss.XssSecurityResolverInterceptor;
import com.wl4g.iam.core.security.xss.resolver.DefaultXssSecurityResolver;
import com.wl4g.iam.core.security.xss.resolver.XssSecurityResolver;

/**
 * XSS protection auto configuration.
 *
 * @author James Wong <983708408@qq.com>
 * @version v1.0 2020年05月06日
 * @since
 */
public class XssAutoConfiguration {

    //
    // X S S _ P R O T E C T I O N _ C O N F I G's.
    //

    @Bean
    @ConditionalOnProperty(name = KEY_XSS_PREFIX + ".enabled", matchIfMissing = true)
    @ConfigurationProperties(prefix = KEY_XSS_PREFIX)
    public XssProperties xssProperties() {
        return new XssProperties();
    }

    @Bean
    @ConditionalOnBean(XssProperties.class)
    public DefaultXssSecurityResolver defaultXssSecurityResolver() {
        return new DefaultXssSecurityResolver();
    }

    @Bean
    @ConditionalOnBean({ XssSecurityResolver.class })
    public XssSecurityResolverInterceptor xssSecurityResolverInterceptor(XssProperties config, XssSecurityResolver resolver) {
        return new XssSecurityResolverInterceptor(config, resolver);
    }

    @Bean
    @ConditionalOnBean(XssSecurityResolverInterceptor.class)
    public AspectJExpressionPointcutAdvisor xssSecurityResolverAspectJExpressionPointcutAdvisor(
            XssProperties config,
            XssSecurityResolverInterceptor advice) {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(config.getExpression());
        advisor.setAdvice(advice);
        return advisor;
    }

}