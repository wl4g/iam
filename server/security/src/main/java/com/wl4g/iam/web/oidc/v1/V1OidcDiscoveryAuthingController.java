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

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_AUTHORIZE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_CERTS;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_CHECK_SESSION_IFRAME;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_DEVICECODE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_END_SESSION;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_INTROSPECT;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_TOKEN;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_CORE_USERINFO;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_DISCOVERY_METADATA;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_REGISTRATION;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_ROOT;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import com.wl4g.iam.annotation.V1OidcDiscoveryController;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.ChallengeAlgorithmType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.JWSAlgorithmType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardClaimType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardClaims;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardDisplay;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardGrantType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardResponseType;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardScope;
import com.wl4g.iam.common.constant.V1OidcIAMConstants.StandardSubjectType;
import com.wl4g.iam.common.model.oidc.v1.V1MetadataEndpointModel;
import com.wl4g.iam.handler.oidc.v1.V1OidcAuthingHandler;
import com.wl4g.iam.web.oidc.BasedOidcAuthingController;

/**
 * IAM V1-OIDC authentication controller.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v3.0.0
 * @see https://openid.net/specs/openid-connect-core-1_0.html#AuthResponseValidation
 */
@SuppressWarnings("unused")
@V1OidcDiscoveryController
public class V1OidcDiscoveryAuthingController extends BasedOidcAuthingController {

    @Autowired
    private V1OidcAuthingHandler oidcAuthingHandler;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    /**
     * Provides OIDC metadata. See the spec at
     * 
     * @see https://openid.net/specs/openid-connect-core-1_0.html#SelfIssuedDiscovery
     * @see https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationResponse
     * @see https://login.microsoftonline.com/common/.well-known/openid-configuration
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_DISCOVERY_METADATA, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> metadata(UriComponentsBuilder uriBuilder, HttpServletRequest req) {
        log.info("called:metadata '{}' from '{}'", URI_IAM_OIDC_ENDPOINT_DISCOVERY_METADATA, req.getRemoteHost());

        String prefix = uriBuilder.replacePath(
                contextPath.concat(URI_IAM_OIDC_ENDPOINT_ROOT).concat("/").concat(trimToEmpty(getCurrentNamespaceLocal().get())))
                .build()
                .encode()
                .toUriString();
        // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
        // https://tools.ietf.org/html/rfc8414#section-2
        V1MetadataEndpointModel metadata = V1MetadataEndpointModel.builder()
                // REQUIRED
                .issuer(prefix)
                // REQUIRED
                .jwks_uri(prefix.concat(URI_IAM_OIDC_ENDPOINT_CORE_CERTS))
                // REQUIRED
                .authorization_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_CORE_AUTHORIZE))
                // REQUIRED
                .token_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_CORE_TOKEN))
                // RECOMMENDED
                .userinfo_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_CORE_USERINFO))
                // OPTIONAL
                .device_authorization_endpoint(URI_IAM_OIDC_ENDPOINT_CORE_DEVICECODE)
                // RECOMMENDED
                .introspection_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_CORE_INTROSPECT))
                // OPTIONAL
                .check_session_iframe(prefix.concat(URI_IAM_OIDC_ENDPOINT_CORE_CHECK_SESSION_IFRAME))
                // OPTIONAL
                .end_session_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_CORE_END_SESSION))
                // RECOMMENDED
                .registration_endpoint(prefix.concat(URI_IAM_OIDC_ENDPOINT_REGISTRATION))
                // REQUIRED
                .scopes_supported(StandardScope.getNames())
                // REQUIRED
                .response_types_supported(StandardResponseType.getNames())
                // OPTIONAL
                .grant_types_supported(StandardGrantType.getNames())
                // REQUIRED
                .subject_types_supported(StandardSubjectType.getNames())
                // REQUIRED
                .id_token_signing_alg_values_supported(JWSAlgorithmType.getNames())
                // REQUIRED
                .claims_supported(StandardClaims.getNames())
                // OPTIONAL
                .claim_types_supported(StandardClaimType.getNames())
                // OPTIONAL
                .display_values_supported(StandardDisplay.getNames())
                // OPTIONAL
                .service_documentation(config.getV1Oidc().getServiceDocumentation())
                // RECOMMENDED PKCE support advertised
                .code_challenge_methods_supported(ChallengeAlgorithmType.getNames())
                .build();
        return ResponseEntity.ok().body(metadata);
    }

}
