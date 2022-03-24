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
package com.wl4g.iam.config.properties;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notEmpty;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getFieldValues;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.util.Assert.isTrue;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.List;

import com.nimbusds.jose.JWSAlgorithm;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.SignAlgorithmSupported;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * IAM V1-OIDC configuration properties
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-17 v1.0.0
 * @since v1.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthResponseValidation
 */
@Getter
@Setter
@ToString
public class V1OidcProperties implements Serializable {
    private static final long serialVersionUID = -2694422471852860689L;

    private String defaultBasicRealmName = "IAM OIDC Realm";
    private String defaultJwksAlgName = "RS256";
    private String defaultJwksJsonResource = "classpath*:/credentials/oidc/jwks.json";

    // Generic OpenID Connect Configuration

    private boolean defaultStandardFlowEnabled = true;
    private boolean defaultImplicitFlowEnabled = false;
    private boolean defaultDirectAccessGrantsEnabled = false;

    // Fine Grain OpenID Connect Configuration

    private String defaultAccessTokenAlgName = "S256";
    private int defaultAccessTokenExpirationSeconds = 3600;
    private int defaultRefreshTokenExpirationSeconds = 3600 * 24;

    private String defaultIdTokenAlgName = "S256";
    private List<String> defaultIdTokenAlgSupported = asList(SignAlgorithmSupported.values()).stream().map(m -> m.name()).collect(
            toList());

    private List<String> defaultCodeChallengeMethodsSupported = asList(SignAlgorithmSupported.values()).stream()
            .map(m -> m.name())
            .collect(toList());
    private int defaultCodeExpirationSeconds = 10;

    /**
     * OIDC service documentation URI.
     */
    private String serviceDocumentation = "https://oidc.iam.wl4g.com/connect/service_documentation.html";

    public void setDefaultJwksAlgName(String jwksAlgName) {
        List<JWSAlgorithm> algorithms = safeList(
                getFieldValues(JWSAlgorithm.class, new int[] { Modifier.PRIVATE }, new String[] { "serialVersionUID" }));
        isTrue(algorithms.stream().map(alg -> alg.getName()).anyMatch(alg -> equalsIgnoreCase(alg, jwksAlgName)),
                format("Invalid jwks alg is '%s', but supported are: %s", jwksAlgName, algorithms.toString()));
        this.defaultJwksAlgName = jwksAlgName;
    }

    public void setDefaultAccessTokenAlgName(String defaultAccessTokenAlgName) {
        hasTextOf(defaultAccessTokenAlgName, "defaultAccessTokenAlgName");
        List<String> definitionAlgNames = asList(SignAlgorithmSupported.values()).stream().map(d -> d.name()).collect(toList());
        isTrue(definitionAlgNames.stream().anyMatch(d -> equalsIgnoreCase(d, defaultAccessTokenAlgName)),
                format("Invalid access token alg is '%s', but supported are: %s", defaultAccessTokenAlgName, definitionAlgNames));
        this.defaultAccessTokenAlgName = defaultAccessTokenAlgName;
    }

    public void setDefaultIdTokenAlgName(String defaultIdTokenAlgName) {
        hasTextOf(defaultIdTokenAlgName, "defaultIdTokenAlgName");
        List<String> definitionAlgNames = asList(SignAlgorithmSupported.values()).stream().map(d -> d.name()).collect(toList());
        isTrue(definitionAlgNames.stream().anyMatch(d -> equalsIgnoreCase(d, defaultIdTokenAlgName)),
                format("Invalid id token alg is '%s', but supported are: %s", defaultIdTokenAlgName, definitionAlgNames));
        this.defaultIdTokenAlgName = defaultIdTokenAlgName;
    }

    public void setDefaultIdTokenAlgSupported(List<String> idTokenAlgSupported) {
        List<String> definitionAlgNames = asList(SignAlgorithmSupported.values()).stream().map(d -> d.name()).collect(toList());
        for (String m : safeList(idTokenAlgSupported)) {
            if (!definitionAlgNames.stream().anyMatch(d -> equalsIgnoreCase(d, m))) {
                throw new IllegalArgumentException(
                        format("Invalid id token algs supported is '%s', but total supported are: %s", m, definitionAlgNames));
            }
        }
        this.defaultIdTokenAlgSupported = idTokenAlgSupported;
    }

    public void setDefaultAccessTokenExpirationSeconds(int accessTokenExpirationSeconds) {
        isTrue(accessTokenExpirationSeconds > 0, "defaultAccessTokenExpirationSeconds must >0");
        this.defaultAccessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    public void setDefaultRefreshTokenExpirationSeconds(int refreshTokenExpirationSeconds) {
        isTrue(refreshTokenExpirationSeconds > 0, "defaultRefreshTokenExpirationSeconds must >0");
        this.defaultRefreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public void setDefaultCodeChallengeMethodsSupported(List<String> codeChallengeMethodsSupported) {
        notEmpty(codeChallengeMethodsSupported, "defaultCodeChallengeMethodsSupported");
        List<String> definitionAlgNames = asList(SignAlgorithmSupported.values()).stream().map(d -> d.name()).collect(toList());
        for (String m : safeList(codeChallengeMethodsSupported)) {
            if (!definitionAlgNames.stream().anyMatch(d -> d.equalsIgnoreCase(m))) {
                throw new IllegalArgumentException(
                        format("Invalid codeCallenge methods is '%s', but supported are: %s", m, definitionAlgNames));
            }
        }
        this.defaultCodeChallengeMethodsSupported = codeChallengeMethodsSupported;
    }

    public void setDefaultCodeExpirationSeconds(int codeExpirationSeconds) {
        isTrue(codeExpirationSeconds > 0, "defaultCodeExpirationSeconds must >0");
        this.defaultCodeExpirationSeconds = codeExpirationSeconds;
    }

}