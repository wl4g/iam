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
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getFieldValues;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.springframework.util.Assert.isTrue;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.nimbusds.jose.JWSAlgorithm;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.CodeChallengeMethodsSupported;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.IdTokenDigestSupported;

/**
 * IAM V1-OIDC configuration properties
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-17 v1.0.0
 * @since v1.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthResponseValidation
 */
public class V1OidcProperties implements Serializable {
    private static final long serialVersionUID = -2694422471852860689L;

    private String wwwRealmName = "IAM OIDC Realm";

    private String idTokenDigestName = "SHA-256";

    private String jwsAlgorithmName = "RS256";

    private String jwksJsonResource = "classpath*:/credentials/oidc/jwks.json";

    private List<String> codeChallengeMethodsSupported = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(asList(CodeChallengeMethodsSupported.values()).stream().map(m -> m.name()).collect(toList()));
        }
    };

    private List<String> idTokenAlgSupported = new ArrayList<String>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(asList(IdTokenDigestSupported.values()).stream().map(m -> m.name()).collect(toList()));
        }
    };

    /**
     * OIDC access token expiration seconds.
     */
    private int accessTokenExpirationSeconds = 3600;

    /**
     * OIDC refresh token expiration seconds.
     */
    private int refreshTokenExpirationSeconds = 3600 * 24;

    /**
     * OIDC authorization code expiration seconds.
     */
    private int codeExpirationSeconds = 10;

    /**
     * OIDC service documentation URI.
     */
    private String serviceDocumentation = "https://oidc.iam.wl4g.com/connect/service_documentation.html";

    public String getWwwRealmName() {
        return wwwRealmName;
    }

    public void setWwwRealmName(String wwwRealmName) {
        this.wwwRealmName = wwwRealmName;
    }

    public String getIdTokenDigestName() {
        return idTokenDigestName;
    }

    public void setIdTokenDigestName(String idTokenDigestName) {
        List<IdTokenDigestSupported> digests = asList(IdTokenDigestSupported.values());
        isTrue(digests.stream().map(d -> d.name()).anyMatch(n -> n.equalsIgnoreCase(idTokenDigestName)),
                format("Invalid idToken digest for {}, The supported digests are: ", idTokenDigestName, digests));
        this.idTokenDigestName = idTokenDigestName;
    }

    public String getJwsAlgorithmName() {
        return jwsAlgorithmName;
    }

    public void setJwsAlgorithmName(String jwsAlgorithmName) {
        List<JWSAlgorithm> algorithms = safeList(
                getFieldValues(JWSAlgorithm.class, new int[] { Modifier.PRIVATE }, new String[] { "serialVersionUID" }));
        isTrue(algorithms.stream().map(alg -> alg.getName()).anyMatch(alg -> equalsIgnoreCase(alg, jwsAlgorithmName)),
                format("Invalid idToken digest for {}, The supported algorithms are: %s", jwsAlgorithmName, algorithms));
        this.jwsAlgorithmName = jwsAlgorithmName;
    }

    public String getJwksJsonResource() {
        return jwksJsonResource;
    }

    public void setJwksJsonResource(String jwksJsonResource) {
        this.jwksJsonResource = jwksJsonResource;
    }

    public List<String> getCodeChallengeMethodsSupported() {
        return codeChallengeMethodsSupported;
    }

    public void setCodeChallengeMethodsSupported(List<String> codeChallengeMethodsSupported) {
        List<String> definitionAlgNames = asList(CodeChallengeMethodsSupported.values()).stream().map(d -> d.name()).collect(
                toList());
        for (String m : safeList(codeChallengeMethodsSupported)) {
            if (!definitionAlgNames.stream().allMatch(d -> d.equalsIgnoreCase(m))) {
                throw new IllegalArgumentException(format(
                        "Invalid codeCallengeMethods digest for {}, The supported digests alg are: ", m, definitionAlgNames));
            }
        }
        this.codeChallengeMethodsSupported = codeChallengeMethodsSupported;
    }

    public List<String> getIdTokenAlgSupported() {
        return idTokenAlgSupported;
    }

    public void setIdTokenAlgSupported(List<String> idTokenAlgSupported) {
        List<String> definitionAlgNames = asList(IdTokenDigestSupported.values()).stream().map(d -> d.name()).collect(toList());
        for (String m : safeList(idTokenAlgSupported)) {
            if (!definitionAlgNames.stream().allMatch(d -> d.equalsIgnoreCase(m))) {
                throw new IllegalArgumentException(
                        format("Invalid idToken digest for {}, The supported digests alg are: ", m, definitionAlgNames));
            }
        }
        this.idTokenAlgSupported = idTokenAlgSupported;
    }

    public int getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    public void setAccessTokenExpirationSeconds(int accessTokenExpirationSeconds) {
        isTrue(accessTokenExpirationSeconds > 0, "accessTokenExpirationSeconds must >0");
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    public int getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationSeconds;
    }

    public void setRefreshTokenExpirationSeconds(int refreshTokenExpirationSeconds) {
        isTrue(refreshTokenExpirationSeconds > 0, "refreshTokenExpirationSeconds must >0");
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public int getCodeExpirationSeconds() {
        return codeExpirationSeconds;
    }

    public void setCodeExpirationSeconds(int codeExpirationSeconds) {
        isTrue(codeExpirationSeconds > 0, "codeExpirationSeconds must >0");
        this.codeExpirationSeconds = codeExpirationSeconds;
    }

    public String getServiceDocumentation() {
        return serviceDocumentation;
    }

    public void setService_documentation(String serviceDocumentation) {
        this.serviceDocumentation = serviceDocumentation;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
    }

}