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
package com.wl4g.iam.common.bean.oidc;

import java.util.Date;
import java.util.List;

import com.wl4g.infra.core.bean.BaseBean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link OidcClient}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-26 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class OidcClient extends BaseBean {
    private static final long serialVersionUID = -4498270305726992089L;

    private String clientId;
    private String clientSecretsJson;

    private String registrationToken;
    private String jwksUri;
    private String jwksJson;
    private String clientName;
    private String clientType;

    private String envType;

    // Generic OpenID Connect Configuration

    /**
     * This enables standard Openid connect redirect based authentication with
     * authorization code. In terms of Openid connect or Oauth2 specifications,
     * this enable support of 'Authorization Code Flow' for this client.
     */
    private Integer standardFlowEnabled;

    /**
     * This enables standard Openid connect redirect based authentication
     * without authorization code. In terms of Openid connect or Oauth2
     * specifications, this enable support of 'Implicit Flow' for this client.
     */
    private Integer implicitFlowEnabled;

    /**
     * This enables support for DirectAccess Grants, which means that client has
     * access to username/password of userand exchange it directly with IAM
     * server for access_token. In terms of Oauth2 specification, this enable
     * support of 'Resource Owner Password Credentials Grant' for this client.
     */
    private Integer directAccessGrantsEnabled;

    /**
     * This enables oauth2 device code supported.
     */
    private Integer oauth2DeviceCodeEnabled;

    private String validRedirectUrisJson;
    private String adminUri;
    private String logoUri;
    private String policyUri;
    private String termsUri;
    private String validWebOriginUrisJson;
    private Integer backchannelLogoutEnabled;
    private String backchannelLogoutUri;

    // Fine Grain OpenID Connect Configuration
    private String accessTokenSignAlg;
    private String idTokenSignAlg;
    private String idTokenEncryptKeyMgtAlg;
    private String idTokenEncryptContentAlg;

    private Integer codeChallengeEnabled;
    private int codeChallengeExpirationSec;

    private Integer useRefreshTokenEnabled;
    private Integer useRefreshTokenForClientCredentialsGrantEnabled;

    private Integer accessTokenExpirationSec;
    private Integer refreshTokenExpirationSec;

    //
    // TMP fields.
    //

    private List<OidcMapper> mappers;

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    public static class ClientSecretInfo {
        private String secret;
        private String create_by;
        private Date create_at;
    }

}