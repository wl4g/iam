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

import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_API_V2_BASE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_LOGIN_BASE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_RCM_BASE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_SNS_BASE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_IAM_SERVER_VERIFY_BASE;
import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT;
import static com.wl4g.iam.core.utils.IamAuthenticatingUtils.correctAuthenticaitorURI;
import static com.wl4g.infra.common.web.WebUtils2.cleanURI;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.Assert.hasText;

import java.util.Map;

import com.wl4g.iam.core.config.AbstractIamProperties;
import com.wl4g.iam.filter.ServerInternalAuthenticationFilter;
import com.wl4g.iam.sns.web.GenericOauth2SnsController;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * IAM server properties
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2019年1月4日
 * @since
 */
@Getter
@Setter
@ToString
public class IamProperties extends AbstractIamProperties<ServerParamProperties> {

    private static final long serialVersionUID = -5858422822181237865L;

    /**
     * Login page URI
     */
    private String loginUri = DEFAULT_VIEW_LOGIN_URI;

    /**
     * Login success redirection to end-point service name. </br>
     *
     * <pre>
     * umc-manager@http://localhost:14048
     * </pre>
     */
    private String successService = EMPTY;

    /**
     * Login success redirection to end-point.(Must be back-end server URI)
     * </br>
     *
     * <pre>
     * umc-manager@http://localhost:14048
     * </pre>
     */
    private String successUri = "http://localhost:8080";

    /**
     * Unauthorized(403) page URI
     */
    private String unauthorizedUri = DEFAULT_VIEW_403_URI;

    /**
     * (SNS)Binding page URI
     */
    private String bindingUri = DEFAULT_VIEW_BINDING_URI;

    /**
     * Matcher configuration properties.
     */
    private MatcherProperties matcher = new MatcherProperties();

    /**
     * Ticket configuration properties.
     */
    private TicketProperties ticket = new TicketProperties();

    /**
     * IAM server parameters configuration properties.
     */
    private ServerParamProperties param = new ServerParamProperties();

    /**
     * IAM server API configuration properties.
     */
    private ApiProperties api = new ApiProperties();

    /**
     * IAM server OIDC configuration properties.
     */
    private V1OidcProperties v1Oidc = new V1OidcProperties();

    public void setLoginUri(String loginUri) {
        this.loginUri = cleanURI(loginUri);
    }

    public void setSuccessEndpoint(String successEndpoint) {
        hasText(successEndpoint, "Success endpoint must not be empty.");
        this.successService = successEndpoint.split("@")[0];
        this.successUri = cleanURI(correctAuthenticaitorURI(successEndpoint.split("@")[1]));
    }

    /**
     * Situation1: http://myapp.domain.com/myapp/xxx/list?id=1 Situation1:
     * /view/index.html ===> http://myapp.domain.com/myapp/authenticator?id=1
     * <p>
     * Implementing the IAM-CAS protocol: When successful login, you must
     * redirect to the back-end server URI of IAM-CAS-Client. (Note: URI of
     * front-end pages can not be used directly).
     *
     * @see {@link com.wl4g.devops.iam.client.filter.AuthenticatorAuthenticationFilter}
     * @see {@link com.wl4g.iam.filter.AuthenticatorAuthenticationFilter#determineSuccessUrl()}
     */
    @Override
    public String getSuccessUri() {
        return successUri;
    }

    @Override
    public String getUnauthorizedUri() {
        return unauthorizedUri;
    }

    @Override
    protected void validation() {
        super.validation();
        hasText(getSuccessService(), "Success service must not be empty.");
        hasText(getSuccessUri(), "SuccessUri must not be empty, e.g. http://localhost:14041");
    }

    @Override
    protected void applyDefaultPropertiesSet() {
        super.applyDefaultPropertiesSet();

        // Default success endPoint.
        if (isBlank(getSuccessService())) {
            setSuccessEndpoint(environment.getProperty("spring.application.name") + "@" + DEFAULT_VIEW_INDEX_URI);
        }
    }

    /**
     * Adds build-in requires filter chains to IAM server. </br>
     * For example: {@link GenericOauth2SnsController#connect} </br>
     */
    @Override
    protected void applyBuildinRequiredFilterChains(Map<String, String> chains) {
        // Default view access files request allowed rules.
        chains.put(DEFAULT_VIEW_BASE_URI + "/**", "anon");
        // Default Iam-JSSDK controller allowed rules.
        chains.put(DEFAULT_JSSDK_BASE_URI + "/**", "anon");
        // SNS authenticator controller allowed rules.
        chains.put(URI_IAM_SERVER_SNS_BASE + "/**", "anon");
        // Login authenticator controller allowed rules.
        chains.put(URI_IAM_SERVER_LOGIN_BASE + "/**", "anon");
        // Verify(CAPTCHA/SMS) authenticator controller rules.
        chains.put(URI_IAM_SERVER_VERIFY_BASE + "/**", "anon");
        // RCM(Simple risk control) controller allowed rules.
        chains.put(URI_IAM_SERVER_RCM_BASE + "/**", "anon");
        // V2 API controller rules.
        chains.put(URI_IAM_SERVER_API_V2_BASE + "/**", ServerInternalAuthenticationFilter.NAME);
        // V1 OIDC controller allowed rules.
        chains.put(URI_IAM_OIDC_ENDPOINT + "/**", "anon");
    }

    /**
     * Default Iam-JSSDK loader path
     */
    public static final String DEFAULT_JSSDK_LOCATION = "classpath*:/iam-jssdk-webapps";

    /**
     * Default Iam-JSSDK base URI.
     */
    public static final String DEFAULT_JSSDK_BASE_URI = "/iam-jssdk";

    /**
     * Default view login URI.
     */
    public static final String DEFAULT_VIEW_LOGIN_URI = DEFAULT_VIEW_BASE_URI + "/login.html";

}