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
package com.wl4g.iam.core.filter;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.iam.core.config.AbstractIamProperties;
import com.wl4g.iam.core.config.AbstractIamProperties.ParamProperties;

/**
 * Interactive authentication processing filter for internal and application
 * services
 * <p>
 * {@link org.apache.shiro.web.filter.authz.HostFilter}
 *
 * @author James Wong <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月30日
 * @since
 */
public abstract class BasedInternalAuthenticationFilter extends AuthenticatingFilter implements IamAuthenticationFilter {
    protected final SmartLogger log = getLogger(getClass());

    protected final AbstractIamProperties<? extends ParamProperties> config;

    public BasedInternalAuthenticationFilter(AbstractIamProperties<? extends ParamProperties> config) {
        this.config = notNullOf(config, "iamProperties");
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return super.isAccessAllowed(request, response, mappedValue);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }

}