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
package com.wl4g.iam.authc;

import org.apache.shiro.authc.HostAuthenticationToken;

import com.wl4g.iam.core.authc.IamAuthenticationToken;

/**
 * Logout authentication token
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月19日
 * @since
 */
public class LogoutAuthenticationToken implements IamAuthenticationToken, HostAuthenticationToken {

    private static final long serialVersionUID = 8587329689973009598L;

    /**
     * Principal currently exiting
     */
    final private String principal;

    /**
     * Remote client host address
     */
    final private String remoteHost;

    public LogoutAuthenticationToken(final String principal, final String remoteHost) {
        /**
         * @see {@link com.wl4g.devops.iam.filter.LogoutAuthenticationFilter#doCreateToken()}:MARK1
         */
        // hasTextOf(principal, "principal");
        this.principal = principal;
        // hasTextOf(remoteHost, "remoteHost");
        this.remoteHost = remoteHost;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public String getHost() {
        return remoteHost;
    }

}