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
package com.wl4g.iam.web.oidc;

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.KEY_IAM_OIDC_TOKEN_TYPE_BEARER;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.TPL_IAM_OIDC_RESPONSE_MODE_FROM_POST_HTML;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_NS_DEFAULT;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_NS_NAME;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.replaceAll;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.wl4g.iam.web.BaseIamController;

import lombok.Getter;

/**
 * Based OIDC IAM authentication controller.
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18
 * @since v3.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html
 */
@Getter
public abstract class BasedOidcAuthingController extends BaseIamController {

    private final SecureRandom random = new SecureRandom();
    private final AntPathMatcher matcher = new AntPathMatcher("/");
    private final InheritableThreadLocal<String> currentNamespaceLocal = new InheritableThreadLocal<>();

    @ModelAttribute
    public void prepare(@PathVariable(name = URI_IAM_OIDC_ENDPOINT_NS_NAME) String namespace, HttpServletRequest req)
            throws Exception {
        namespace = isBlank(namespace) ? URI_IAM_OIDC_ENDPOINT_NS_DEFAULT : namespace;
        log.info("called:prepare oidc namespace for '{}' from '{}'", namespace, req.getRemoteHost());
        getCurrentNamespaceLocal().set(namespace);
    }

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
        responseHeaders.add("WWW-Authenticate", format("Basic realm=\"{}\"", config.getV1Oidc().getDefaultBasicRealmName()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body(
                "<html><body><h1>401 Unauthorized</h1>IAM V1 OIDC server</body></html>");
    }

    /**
     * https://datatracker.ietf.org/doc/html/rfc6749
     */
    protected ResponseEntity<?> wrapErrorRfc6749(String error, String error_description) {
        log.warn("wrap rfc6749 response. error={} error_description={}", error, error_description);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("error_description", replaceAll(error_description, " ", "+"));
        return ResponseEntity.badRequest().body(map);
    }

}
