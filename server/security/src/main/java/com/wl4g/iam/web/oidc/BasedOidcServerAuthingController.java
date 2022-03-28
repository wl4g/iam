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
package com.wl4g.iam.web.oidc;

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_TOKEN_TYPE_BEARER;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.TPL_IAM_OIDC_RESPONSE_MODE_FROM_POST_HTML;
import static com.wl4g.infra.common.codec.Encodes.urlDecode;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.wl4g.iam.web.BaseIamController;

/**
 * Based OIDC IAM authentication controller.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18
 * @since v3.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html
 */
public abstract class BasedOidcServerAuthingController extends BaseIamController {

    protected final SecureRandom random = new SecureRandom();

    protected byte[] doDigestHash(String digestAlgName, String plaintext) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(digestAlgName);
        digest.reset();
        digest.update(plaintext.getBytes(StandardCharsets.UTF_8));
        return digest.digest();
    }

    protected String toDetermineAccessToken(String authorizationHeader, String accessTokenParameter) {
        if (!isBlank(accessTokenParameter)) {
            return accessTokenParameter;
        } else if (startsWith(authorizationHeader, KEY_IAM_OIDC_TOKEN_TYPE_BEARER.concat(" "))) {
            return authorizationHeader.substring(KEY_IAM_OIDC_TOKEN_TYPE_BEARER.length() + 1);
        }
        return null;
    }

    protected Set<String> toSpaceSeparatedParams(String param) {
        return isBlank(param) ? emptySet() : new HashSet<>(asList(urlDecode(param).split(" ")));
    }

    protected ResponseEntity<String> wrapResponseFromPost(
            @NotBlank String actionUrl,
            @Nullable String state,
            @Nullable String code,
            @Nullable String id_token,
            @Nullable String access_token) {
        hasTextOf(actionUrl, "actionUrl");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_HTML);
        String fromPostHtml = format(TPL_IAM_OIDC_RESPONSE_MODE_FROM_POST_HTML, trimToEmpty(actionUrl), trimToEmpty(state),
                trimToEmpty(code), trimToEmpty(id_token), trimToEmpty(access_token));
        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).body(fromPostHtml);
    }

    protected ResponseEntity<String> wrapUnauthentication() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_HTML);
        responseHeaders.add("WWW-Authenticate",
                format("Basic realm=\"{}\"", config.getV1Oidc().getDefaultProtocolProperties().getBasicRealmName()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body(
                "<html><body><h1>401 Unauthorized</h1>IAM V1 OIDC server</body></html>");
    }

    /**
     * https://datatracker.ietf.org/doc/html/rfc6749
     */
    protected ResponseEntity<?> wrapErrorRFC6749(String error, String error_description) {
        log.warn("Wrap rfc6749 error={} error_description={}", error, error_description);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("error_description", error_description);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
    }

}
