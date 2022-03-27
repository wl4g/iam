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
package com.wl4g.iam.web.oidc.v1;

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_JWK_DEFAULT_RESOURCE;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.wl4g.iam.common.bean.oidc.OidcClient;
import com.wl4g.iam.config.properties.V1OidcProperties;
import com.wl4g.iam.config.properties.V1OidcProperties.DefaultProtocolProperties;
import com.wl4g.iam.core.exception.IamException;
import com.wl4g.iam.core.exception.OidcException;
import com.wl4g.iam.handler.oidc.v1.DefaultV1OidcAuthenticatingHandler;
import com.wl4g.infra.common.resource.StreamResource;
import com.wl4g.infra.common.resource.resolver.ClassPathResourcePatternResolver;
import com.wl4g.infra.core.utils.bean.BeanCopierUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link V1OidcClientConfig}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-25 v3.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
public class V1OidcClientConfig extends DefaultProtocolProperties {
    private static final long serialVersionUID = 4776976002803043619L;

    // Generic OpenID Connect Configuration

    private final String clientId;
    private String clientName;
    private String clientType;

    private List<String> validRedirectUris;
    private String adminUri;
    private String logoUri;
    private String policyUri;
    private String termsUri;
    private List<String> validWebOriginUris;

    private boolean backchannelLogoutEnabled;
    private String backchannelLogoutUri;

    // Advanced Settings

    private boolean codeChallengeEnabled;

    // Credentials Information

    private List<OidcClient.ClientSecretInfo> clientSecrets;
    private JWKConfig jwkConfig;

    public static V1OidcClientConfig newInstance(String clientId, V1OidcProperties config) {
        V1OidcClientConfig that = new V1OidcClientConfig(clientId);
        BeanCopierUtils.mapper(config.getDefaultProtocolProperties(), that);
        try {
            that.initJWKConfig();
        } catch (JOSEException e) {
            throw new OidcException(format("Failed to init jwks for clientId='%s'", clientId), e);
        }
        return that;
    }

    private V1OidcClientConfig(String clientId) {
        this.clientId = hasTextOf(clientId, "clientId");
    }

    private void initJWKConfig() throws JOSEException {
        // TODO use from DB config
        JWKSet jwkSet = DEFAULT_JWKSET;
        JWK key = jwkSet.getKeys().get(0);
        JWSSigner signer = new RSASSASigner((RSAKey) key);
        JWKSet pubJWKSet = jwkSet.toPublicJWKSet();
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.parse(getJwksSignAlg())).keyID(key.getKeyID()).build();
        this.jwkConfig = new JWKConfig(signer, pubJWKSet, jwsHeader);
    }

    private static final JWKSet loadJWKSetDefault0() {
        ClassPathResourcePatternResolver resolver = new ClassPathResourcePatternResolver(
                Thread.currentThread().getContextClassLoader());
        Set<StreamResource> resources;
        try {
            resources = resolver.getResources(URI_IAM_OIDC_JWK_DEFAULT_RESOURCE);
        } catch (IOException e1) {
            throw new IllegalStateException("Failed to load default JWKS resources.", e1);
        }
        if (isEmpty(resources)) {
            throw new IamException(format("Not found default JWKS resources: %s", URI_IAM_OIDC_JWK_DEFAULT_RESOURCE));
        }
        StreamResource res = resources.iterator().next();
        if (resources.size() > 1) {
            getLogger(DefaultV1OidcAuthenticatingHandler.class)
                    .warn(format("[WARNNING] Found multi jwks resources %s by %s, Using the first one by default %s", resources,
                            URI_IAM_OIDC_JWK_DEFAULT_RESOURCE, res));
        }
        try (InputStream in = res.getInputStream()) {
            JWKSet jwkset = JWKSet.load(in);
            // TODO Deep safety processes.
            return new JWKSet(unmodifiableList(jwkset.getKeys()), unmodifiableMap(jwkset.getAdditionalMembers()));
        } catch (IOException | ParseException e) {
            throw new IllegalStateException("Failed to load default JWKS resources.", e);
        }
    }

    @Getter
    @ToString
    @SuperBuilder
    @AllArgsConstructor
    public static final class JWKConfig {
        private final JWSSigner signer;
        private final JWKSet pubJWKSet;
        private final JWSHeader jwsHeader;
    }

    public static final JWKSet DEFAULT_JWKSET = loadJWKSetDefault0();

}
