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
package com.wl4g.iam.filter;

import com.wl4g.iam.authc.WechatMpAuthenticationToken;
import com.wl4g.iam.core.annotation.IamFilter;
import com.wl4g.iam.core.authc.IamAuthenticationToken;

import static com.wl4g.infra.common.lang.Assert2.isInstanceOf;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_SNS_AUTHORIZED_INFO;
import static com.wl4g.iam.core.utils.IamSecurityHolder.bind;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * {@link WechatMpAuthenticationFilter}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019年7月8日
 * @since
 */
@IamFilter
public class WechatMpAuthenticationFilter extends Oauth2SnsAuthenticationFilter<WechatMpAuthenticationToken> {

    @Override
    public String getName() {
        return ProviderSupport.WECHATMP.getName();
    }

    @Override
    public String getUriMapping() {
        return URI_BASE_MAPPING + ProviderSupport.WECHATMP.getName();
    }

    @Override
    protected boolean enabled() {
        return true;
    }

    @Override
    protected IamAuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        // Gets sns wxmp token.
        IamAuthenticationToken token = super.createToken(request, response);
        isInstanceOf(WechatMpAuthenticationToken.class, token);

        // Bind sns authorization info, for wxmp step 2 login with account
        WechatMpAuthenticationToken wxmpToken = (WechatMpAuthenticationToken) token;
        bind(KEY_SNS_AUTHORIZED_INFO, wxmpToken.getSocial());

        return token;
    }

}