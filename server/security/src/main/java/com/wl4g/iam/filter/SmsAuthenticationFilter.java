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

import static org.apache.shiro.web.util.WebUtils.getCleanParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wl4g.iam.authc.SmsAuthenticationToken;
import com.wl4g.iam.authc.ServerIamAuthenticationToken.RedirectInfo;
import com.wl4g.iam.core.annotation.IamFilter;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
import com.google.common.annotations.Beta;

/**
 * SMS authentication filter.
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0.0 2019-05-18
 * @since
 */
@IamFilter
@Beta
public class SmsAuthenticationFilter extends AbstractServerIamAuthenticationFilter<SmsAuthenticationToken> {
    final public static String NAME = "sms";

    @Override
    protected SmsAuthenticationToken doCreateToken(
            String remoteHost,
            RedirectInfo redirectInfo,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final String action = getCleanParam(request, config.getParam().getSmsActionName());
        final String principal = getCleanParam(request, config.getParam().getPrincipalName());
        final String smsCode = getCleanParam(request, config.getParam().getCredentialsName());
        final String algKind = getCleanParam(request, config.getParam().getSecretAlgKindName());
        final String clientSecret = getCleanParam(request, config.getParam().getClientSecretKeyName());
        return new SmsAuthenticationToken(CryptKind.of(algKind), clientSecret, remoteHost, action, principal, smsCode);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getUriMapping() {
        return URI_BASE_MAPPING + NAME;
    }

}