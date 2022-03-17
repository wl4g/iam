/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.iam.core.rpc;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.iam.common.constant.RpcContextIAMConstants.CURRENT_IAM_PRINCIPAL;
import static com.wl4g.iam.common.constant.RpcContextIAMConstants.CURRENT_IAM_PRINCIPAL_ID;
import static com.wl4g.iam.common.constant.RpcContextIAMConstants.CURRENT_IAM_PRINCIPAL_USER;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wl4g.infra.common.bridge.RpcContextHolderBridges;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.utils.IamSecurityHolder;

/**
 * {@link RpcContextIamSecurityAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-25
 * @sine v1.0
 * @see
 */
@ConditionalOnClass(name = RpcContextHolderBridges.rpcContextHolderClassName)
public class RpcContextIamSecurityAutoConfiguration {

    private final SmartLogger log = getLogger(getClass());

    @Bean
    public RpcContextSecurityHandlerInterceptor rpcContextSecurityHandlerInterceptor() {
        return new RpcContextSecurityHandlerInterceptor();
    }

    @Bean
    public RpcContextSecurityInterceptorMvcConfigurer rpcContextSecurityInterceptorMvcConfigurer(
            RpcContextSecurityHandlerInterceptor interceptor) {
        return new RpcContextSecurityInterceptorMvcConfigurer(interceptor);
    }

    class RpcContextSecurityInterceptorMvcConfigurer implements WebMvcConfigurer {
        private final RpcContextSecurityHandlerInterceptor interceptor;

        public RpcContextSecurityInterceptorMvcConfigurer(RpcContextSecurityHandlerInterceptor interceptor) {
            this.interceptor = notNullOf(interceptor, "interceptor");
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(interceptor).addPathPatterns("/**");
        }
    }

    class RpcContextSecurityHandlerInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // Bind iam current attributes to rpc context.
            if (RpcContextHolderBridges.hasRpcContextHolderClass()) { // Distributed(cluster)?
                try {
                    Subject subject = IamSecurityHolder.getSubject();
                    // The current authentication information needs to be set
                    // only when it has been authenticated.
                    if (subject.isAuthenticated()) {
                        IamPrincipal currentPrincipal = IamSecurityHolder.getPrincipalInfo();

                        RpcContextHolderBridges.invokeSet(false, CURRENT_IAM_PRINCIPAL_ID, currentPrincipal.getPrincipalId());
                        RpcContextHolderBridges.invokeSet(false, CURRENT_IAM_PRINCIPAL_USER, currentPrincipal.getName());

                        // Set to reference type for performance optimization.
                        RpcContextHolderBridges.invokeSetRef(false, CURRENT_IAM_PRINCIPAL, currentPrincipal);
                    }
                } catch (UnavailableSecurityManagerException e) {
                    // It may be a request that does not carry a session, such
                    // as getting a list of remote IAM sessions.
                    log.warn("Cannot set current IAM authentication info. {}", e.getLocalizedMessage());
                }
            }
            return true;
        }
    }

}
