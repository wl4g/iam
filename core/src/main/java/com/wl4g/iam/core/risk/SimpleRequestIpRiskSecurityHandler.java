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
package com.wl4g.iam.core.risk;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.web.WebUtils2.getHttpRemoteAddr;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.BEAN_SESSION_RESOURCE_MSG_BUNDLER;
import static org.apache.shiro.web.util.WebUtils.toHttp;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.UnavailableSecurityManagerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.infra.common.net.HostUtils;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.iam.common.i18n.SessionResourceMessageBundler;
import com.wl4g.iam.core.config.RiskSecurityProperties;
import com.wl4g.iam.core.utils.IamSecurityHolder;

/**
 * Simple risk checking of request IP filter.
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-06-14 v1.0.0
 * @see v1.0.0
 */
public class SimpleRequestIpRiskSecurityHandler implements RiskSecurityHandler {

    protected SmartLogger log = getLogger(getClass());

    @Resource(name = BEAN_SESSION_RESOURCE_MSG_BUNDLER)
    protected SessionResourceMessageBundler bundle;

    @Autowired
    protected RiskSecurityProperties config;

    @Override
    public RespBase<Object> inspecting(HttpServletRequest request, HttpServletResponse response) {
        // Skip checking request ip.
        if (!config.isCheckRequestIpSameLogin()) {
            return null;
        }

        // The authenticated, check whether the current request IP is the same
        // as the login.
        boolean authenticated = false;
        try {
            authenticated = IamSecurityHolder.getSubject().isAuthenticated();
        } catch (UnavailableSecurityManagerException e) {
            // Ignore when the subject has not been created.
        }
        if (authenticated) {
            String clientAddr = getHttpRemoteAddr(toHttp(request));
            String loginAddr = IamSecurityHolder.getSession().getHost();
            if (!HostUtils.isSameHost(loginAddr, clientAddr)) {
                log.warn("The request was rejected because the request addr: {} was inconsistent with the login {}: {}",
                        clientAddr, loginAddr);

                RespBase<Object> resp = RespBase.create();
                resp.setMessage(bundle.getMessage("SimpleRequestIpRiskSecurityFilter.noSame.requestAddr", clientAddr, loginAddr));
                return resp;
            }
        }

        return null;
    }

}
