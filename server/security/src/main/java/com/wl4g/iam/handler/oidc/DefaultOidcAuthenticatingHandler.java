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
package com.wl4g.iam.handler.oidc;

import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.iam.common.constant.OidcIAMConstants;
import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.handler.AbstractAuthenticatingHandler;
import com.wl4g.infra.support.cache.jedis.JedisService;

/**
 * {@link DefaultOidcAuthenticatingHandler}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v1.0.0
 */
public class DefaultOidcAuthenticatingHandler extends AbstractAuthenticatingHandler implements OidcAuthenticatingHandler {

    private @Autowired JedisService jedisService;

    @Override
    public void putAccessToken(String authorizationBearer, V1AccessTokenInfo accessToken) {
        jedisService.setObjectAsJson(buildAccessTokenKey(authorizationBearer), accessToken,
                config.getOidc().getTokenExpirationSeconds());
    }

    @Override
    public V1AccessTokenInfo loadAccessToken(String authorizationBearer) {
        return jedisService.getObjectAsJson(buildAccessTokenKey(authorizationBearer), V1AccessTokenInfo.class);
    }

    @Override
    public void putAuthorizationCode(String authorizationCode, V1AccessTokenInfo accessToken) {
        jedisService.setObjectAsJson(buildAuthorizationCodeKey(authorizationCode), accessToken,
                config.getOidc().getCodeExpirationSeconds());
    }

    @Override
    public V1AuthorizationCodeInfo loadAuthorizationCode(String authorizationCode) {
        return jedisService.getObjectAsJson(buildAuthorizationCodeKey(authorizationCode), V1AuthorizationCodeInfo.class);
    }

    private String buildAccessTokenKey(String authorizationBearer) {
        return OidcIAMConstants.CACHE_OIDC_ACCESSTOKEN_PREFIX.concat(authorizationBearer);
    }

    private String buildAuthorizationCodeKey(String authorizationCode) {
        return OidcIAMConstants.CACHE_OIDC_AUTHCODE_PREFIX.concat(authorizationCode);
    }

}
