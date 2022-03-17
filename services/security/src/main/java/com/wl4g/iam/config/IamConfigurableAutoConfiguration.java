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
package com.wl4g.iam.config;

import com.wl4g.iam.common.bean.ApplicationInfo;
import com.wl4g.iam.common.bean.SocialConnectInfo;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.common.subject.IamPrincipal.Parameter;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.core.exception.BindingConstraintsException;
import com.wl4g.iam.core.exception.IamException;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static java.lang.String.format;

import java.util.List;

/**
 * Based context configurable auto configuration.
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年3月24日
 * @since
 */
@ConditionalOnBean(IamServerMarkerConfiguration.Marker.class)
public class IamConfigurableAutoConfiguration {

    // Security components configuration

    @Bean
    @ConditionalOnMissingBean
    public ServerSecurityConfigurer serverSecurityConfigurer() {
        return new CheckImpledServerSecurityConfigurer();
    }

    /**
     * Check whether ServerSecurityConfigurer has been implemented.
     *
     * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
     * @version v1.0 2019-07-27
     * @since
     */
    public final static class CheckImpledServerSecurityConfigurer implements ServerSecurityConfigurer, InitializingBean {

        @Override
        public String decorateAuthenticateSuccessUrl(String successUrl, AuthenticationToken token, Subject subject,
                ServletRequest request, ServletResponse response) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String decorateAuthenticateFailureUrl(String loginUrl, AuthenticationToken token, Throwable ae,
                ServletRequest request, ServletResponse response) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ApplicationInfo getApplicationInfo(String applicationName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ApplicationInfo> findApplicationInfo(String... applicationNames) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IamPrincipal getIamUserDetail(Parameter parameter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isApplicationAccessAuthorized(String principal, String application) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends SocialConnectInfo> List<T> findSocialConnections(String principal, String provider) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void bindSocialConnection(SocialConnectInfo social) throws BindingConstraintsException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unbindSocialConnection(SocialConnectInfo social) throws BindingConstraintsException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            throw new IamException(format(
                    "\n\n=>\tWhen relying on IAM security module as a service, it is necessary to implements the '%s' interface!\n",
                    ServerSecurityConfigurer.class.getName()));
        }

    }

}