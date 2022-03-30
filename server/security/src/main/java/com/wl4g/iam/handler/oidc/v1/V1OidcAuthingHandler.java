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
package com.wl4g.iam.handler.oidc.v1;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1DeviceCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUserClaims;
import com.wl4g.iam.web.oidc.v1.V1OidcClientConfig;

/**
 * {@link V1OidcAuthingHandler}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v1.0.0
 */
public interface V1OidcAuthingHandler {

    // JWK configuration.

    V1OidcClientConfig.JWKConfig loadJWKConfig(@NotBlank String namespace);

    void clearJWKConfigCache(@Nullable String namespace);

    // OIDC client configuration.

    V1OidcClientConfig loadClientConfig(@NotBlank String clientId);

    void clearClientConfigCache();

    // Authorization code.

    void putAuthorizationCode(@NotBlank String authorizationCode, V1AuthorizationCodeInfo authorizationCodeInfo);

    V1AuthorizationCodeInfo loadAuthorizationCode(@NotBlank String authorizationCode);

    // Access token.

    void putAccessToken(@NotBlank String accessToken, V1AccessTokenInfo accessTokenInfo);

    V1AccessTokenInfo loadAccessToken(@NotBlank String accessToken);

    // Refresh token.

    void putRefreshToken(@NotBlank String refreshToken, V1AccessTokenInfo accessToken);

    V1AccessTokenInfo loadRefreshToken(@NotBlank String refreshToken, boolean remove);

    // Device code.

    V1DeviceCodeInfo loadDeviceCode(@NotBlank String deviceCode, boolean remove);

    void putDeviceCode(@NotBlank String deviceCode, V1DeviceCodeInfo deviceCodeInfo);

    // User claims.

    V1OidcUserClaims getV1OidcUserClaimsByUser(@NotBlank String username);

    V1OidcUserClaims getV1OidcUserClaimsByClientId(@NotBlank String clientId);

    boolean validate(V1OidcUserClaims user, String password);

}
