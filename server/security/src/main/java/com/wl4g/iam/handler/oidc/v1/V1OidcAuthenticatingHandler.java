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

import com.wl4g.iam.common.model.oidc.v1.V1AccessTokenInfo;
import com.wl4g.iam.common.model.oidc.v1.V1AuthorizationCodeInfo;
import com.wl4g.iam.common.model.oidc.v1.V1OidcUserClaims;

/**
 * {@link V1OidcAuthenticatingHandler}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v1.0.0
 */
public interface V1OidcAuthenticatingHandler {

    void putAccessToken(String accessToken, V1AccessTokenInfo accessTokenInfo);

    V1AccessTokenInfo loadAccessToken(String accessToken);

    void putRefreshToken(String refreshToken, String accessToken);

    String loadRefreshToken(String refreshToken);

    void putAuthorizationCode(String authorizationCode, V1AuthorizationCodeInfo authorizationCodeInfo);

    V1AuthorizationCodeInfo loadAuthorizationCode(String authorizationCode);

    V1OidcUserClaims getV1OidcUser(String loginName);

    boolean validate(V1OidcUserClaims user, String password);

}
