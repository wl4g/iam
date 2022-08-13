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
package com.wl4g.iam.sns.handler;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.iam.common.constant.IAMConstants.CONF_PREFIX_IAM_SECURITY_SNS;
import static com.wl4g.infra.common.lang.Assert2.notEmpty;
import static com.wl4g.infra.core.web.BaseController.REDIRECT_PREFIX;
import static java.lang.String.format;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.config.properties.SnsProperties;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.core.config.AbstractIamProperties.Which;
import com.wl4g.iam.sns.OAuth2ApiBindingFactory;

/**
 * Login SNS handler
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019年2月24日
 * @since
 */
public class LoginSnsHandler extends AbstractSnsHandler {

    public LoginSnsHandler(IamProperties config, SnsProperties snsConfig, OAuth2ApiBindingFactory connectFactory,
            ServerSecurityConfigurer context) {
        super(config, snsConfig, connectFactory, context);
    }

    @Override
    public String doGetAuthorizingUrl(Which which, String provider, String state, Map<String, String> connectParams) {
        // Connecting
        String authorizingUrl = super.doGetAuthorizingUrl(which, provider, state, connectParams);

        // Save connect parameters
        saveOauth2ConnectParameters(provider, state, connectParams);

        return REDIRECT_PREFIX + authorizingUrl;
    }

    @Override
    protected void checkConnectParameters(String provider, String state, Map<String, String> connectParams) {
        super.checkConnectParameters(provider, state, connectParams);

        // Check connect parameters.
        notEmpty(connectParams, format(
                "Connect parameters is invalid or expired for state='%s', You can try setting '%s' to increase the timeout.",
                state, CONF_PREFIX_IAM_SECURITY_SNS.concat(".oauth2ConnectExpireMs")));

        // PC-side browsers use agent redirection(QQ,sina)
        // hasText(connectParams.get(config.getParam().getAgent()),
        // format("Parmam '%s' value is required",
        // config.getParam().getAgent()));
    }

    @Override
    protected void checkCallbackParameters(String provider, String state, String code, Map<String, String> connectParams) {
        hasText(state, format("State '%s' is invalid or expired", state));
        super.checkCallbackParameters(provider, state, code, connectParams);
    }

    @Override
    protected String postCallbackResponse(
            String provider,
            String callbackId,
            Map<String, String> connectParams,
            HttpServletRequest request) {
        return super.getLoginSubmitUrl(provider, callbackId, request);
    }

    @Override
    public Which which() {
        return Which.LOGIN;
    }

}