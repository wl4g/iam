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
package com.wl4g.iam.common.constant;

import static com.wl4g.infra.common.codec.Encodes.urlDecode;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.Beta;
import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.resource.ResourceUtils2;

import lombok.Getter;

/**
 * IAM V1-OIDC constants.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月13日
 * @since
 */
public abstract class V1OidcIAMConstants extends IAMConstants {

    /** endpoint URIs definitions. */
    public static final String URI_IAM_OIDC_ENDPOINT_NS_NAME = "realm"; // realm/namespace/tenant
    public static final String URI_IAM_OIDC_ENDPOINT_NS_DEFAULT = "default";
    public static final String URI_IAM_OIDC_ENDPOINT_ROOT = "/v1-oidc";
    public static final String URI_IAM_OIDC_ENDPOINT_ROOT_NS = URI_IAM_OIDC_ENDPOINT_ROOT + "/{" + URI_IAM_OIDC_ENDPOINT_NS_NAME
            + "}";

    // OpenID Connect Core.
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_PREFIX = URI_IAM_OIDC_ENDPOINT_ROOT_NS + "/connect";
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_CERTS = "/certs";
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_TOKEN = "/token";
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_AUTHORIZE = "/authorize";
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_USERINFO = "/userinfo";
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_DEVICECODE = "/devicecode";
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_INTROSPECT = "/introspect";
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_CHECK_SESSION_IFRAME = "/auth_status_iframe.html";
    public static final String URI_IAM_OIDC_ENDPOINT_CORE_END_SESSION = "/logout";

    // OpenID Connect Discovery.
    // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig
    public static final String URI_IAM_OIDC_ENDPOINT_DISCOVERY_PREFIX = URI_IAM_OIDC_ENDPOINT_ROOT_NS;
    public static final String URI_IAM_OIDC_ENDPOINT_DISCOVERY_METADATA = "/.well-known/openid-configuration";

    // OpenID Connect Dynamic Registration.
    public static final String URI_IAM_OIDC_ENDPOINT_REGISTRATION_PREFIX = URI_IAM_OIDC_ENDPOINT_ROOT_NS
            + "/clients-registrations";
    public static final String URI_IAM_OIDC_ENDPOINT_REGISTRATION = "/registration";

    /** token type definitions. */
    public static final String KEY_IAM_OIDC_TOKEN_TYPE_BEARER = "Bearer";

    /** cache key definitions. */
    public static final String CACHE_OIDC_PREFIX = CONF_PREFIX_IAM + "oidc:";
    public static final String CACHE_OIDC_AUTHCODE_PREFIX = CACHE_OIDC_PREFIX + "code:";
    public static final String CACHE_OIDC_ACCESSTOKEN_PREFIX = CACHE_OIDC_PREFIX + "ak:";
    public static final String CACHE_OIDC_REFRESHTOKEN_PREFIX = CACHE_OIDC_PREFIX + "rk:";
    public static final String CACHE_OIDC_DEVICECODE_PREFIX = CACHE_OIDC_PREFIX + "devicecode:";

    /** login theme definitions. */
    public static final String KEY_IAM_OIDC_LOGIN_THEMEM_BASIC = "BASIC";
    public static final String KEY_IAM_OIDC_LOGIN_THEMEM_BASIC_REALM_DEFAULT = "IAM OIDC Basic Realm";
    public static final String KEY_IAM_OIDC_LOGIN_THEMEM_IAM = "IAM";

    /** Code challenge digest algorithm definitions. */
    @Getter
    public static enum ChallengeAlgorithmType {
        plain(true, "none"), S256("SHA-256"), S384("SHA-384"), S512("SHA-512");

        private final boolean isDefault;
        private final String algName;

        private ChallengeAlgorithmType(String algName) {
            this(false, algName);
        }

        private ChallengeAlgorithmType(boolean isDefault, String algName) {
            this.isDefault = isDefault;
            this.algName = algName;
        }

        public static ChallengeAlgorithmType getDefault() {
            ChallengeAlgorithmType defaultValue = null;
            for (ChallengeAlgorithmType v : values()) {
                if (v.isDefault) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(ChallengeAlgorithmType.values()).stream().map(v -> v.name().toUpperCase()).collect(toList());
        }

        public static ChallengeAlgorithmType safeOf(String name) {
            for (ChallengeAlgorithmType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static String of(String name) {
            for (ChallengeAlgorithmType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v.getAlgName();
                }
            }
            throw new IllegalArgumentException(format("Invalid challenge digest alg alias name for '%s'", name));
        }
    }

    /**
     * Token(access token/refresh token/id token) signature digest algorithm
     * definitions.
     */
    @Getter
    public static enum TokenSignAlgorithmType {
        S256(true, "SHA-256"), S384("SHA-384"), S512("SHA-512");

        private final boolean isDefault;
        private final String algName;

        private TokenSignAlgorithmType(String algName) {
            this(false, algName);
        }

        private TokenSignAlgorithmType(boolean isDefault, String algName) {
            this.isDefault = isDefault;
            this.algName = algName;
        }

        public static TokenSignAlgorithmType getDefault() {
            TokenSignAlgorithmType defaultValue = null;
            for (TokenSignAlgorithmType v : values()) {
                if (v.isDefault) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(TokenSignAlgorithmType.values()).stream().map(v -> v.name().toUpperCase()).collect(toList());
        }

        public static TokenSignAlgorithmType safeOf(String name) {
            for (TokenSignAlgorithmType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static String of(String name) {
            for (TokenSignAlgorithmType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v.getAlgName();
                }
            }
            throw new IllegalArgumentException(format("Invalid token signature digest alg alias name for '%s'", name));
        }
    }

    /**
     * JWS algorithm definitions. see to: {@link com.nimbusds.jose.JWSAlgorithm}
     */
    @Getter
    public static enum JWSAlgorithmType {
        HS256, HS384, HS512, RS256(true), RS384, RS512, ES256, ES384, ES512, PS256, PS384, PS512, EdDSA;

        private final boolean isDefault;

        private JWSAlgorithmType() {
            this.isDefault = false;
        }

        private JWSAlgorithmType(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public static JWSAlgorithmType getDefault() {
            JWSAlgorithmType defaultValue = null;
            for (JWSAlgorithmType v : values()) {
                if (v.isDefault) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(JWSAlgorithmType.values()).stream().map(v -> v.name().toUpperCase()).collect(toList());
        }
    }

    /**
     * scope definitions.
     */
    @Getter
    public static enum StandardScope {
        openid(true), profile, email, address, phone;
        private final boolean isDefault;

        private StandardScope() {
            this.isDefault = false;
        }

        private StandardScope(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public static StandardScope getDefault() {
            StandardScope defaultValue = null;
            for (StandardScope v : values()) {
                if (v.isDefault) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(StandardScope.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }

        /**
         * Check if the this enumerated value is a subset of {@link scopeString}
         * to values.
         */
        public boolean containsIn(String scope) {
            Set<String> _scope = safeDecodeToParams(scope);
            return _scope.stream().anyMatch(s -> eqIgnCase(s, name()));
        }

        public static boolean isValid(String scope) {
            Set<String> _scope = safeDecodeToParams(scope);
            return CollectionUtils2.isSubCollection(_scope, getNames());
        }
    }

    /**
     * grant definitions. oauth2 supported two types of authorization flow,
     * typically referred to as "Client-side" and "Server-side". Use of implicit
     * grant is discouraged unless there is no other option available.
     */
    @Getter
    public static enum StandardGrantType {

        /**
         * Authorization code mode (i.e. first login to get code, then token)
         */
        authorization_code(true),

        /**
         * Simplified mode (passing token in redirect_uri Hash; Auth client
         * running in browsers, such as JS, Flash)
         */
        implicit,

        /**
         * Refresh access_token
         */
        refresh_token,

        /**
         * e.g to
         * see:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-on-behalf-of-flow
         */
        behalf("urn:ietf:params:oauth:grant-type:jwt-bearer"),

        /**
         * Password mode (Pass user name, password, get token directly)
         */
        @Deprecated
        password,

        /**
         * Client mode (no user, the user registers with the client, and the
         * client obtains resources from the server in its own name). </br>
         * </br>
         * 
         * E.g: WeChat public platform, the parameter
         * <font color=red>grant_type=client_credential</font> in get
         * access_token of the basic API (instead of
         * <font color=red>client_credentials)</font> see to:
         * https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140183
         */
        client_credentials,

        /**
         * Like the scan login polling implementation. </br>
         * </br>
         * for example: </br>
         * </br>
         * 
         * https://developers.google.com/identity/protocols/oauth2/limited-input-device
         * </br>
         * </br>
         * https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-device-code#device-authorization-response
         * </br>
         * </br>
         * https://www.oauth.com/playground/device-code.html </br>
         * 
         * @since oauth2.1
         */
        @Beta
        device_code("urn:ietf:params:oauth:grant-type:device_code"),

        /**
         * see:https://openid.net/2021/09/01/openid-connect-client-initiated-backchannel-authentication-ciba-core-is-now-a-final-specification/
         * see:https://openid.net/specs/openid-client-initiated-backchannel-authentication-core-1_0-final.html
         */
        @Beta
        ciba("urn:openid:params:grant-type:ciba");

        private final String value;
        private final boolean isDefault;

        private StandardGrantType() {
            this.value = name();
            this.isDefault = false;
        }

        private StandardGrantType(String value) {
            this.value = value;
            this.isDefault = false;
        }

        private StandardGrantType(boolean isDefault) {
            this.value = name();
            this.isDefault = isDefault;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public static StandardGrantType getDefault() {
            StandardGrantType defaultValue = null;
            for (StandardGrantType v : values()) {
                if (v.isDefault()) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(StandardGrantType.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }

        public static StandardGrantType safeOf(String name) {
            for (StandardGrantType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static StandardGrantType of(String name) {
            StandardGrantType result = safeOf(name);
            if (nonNull(result)) {
                return result;
            }
            throw new IllegalArgumentException(format("unsupported grant type for '%s'", name));
        }
    }

    /**
     * response type definitions. </br>
     * https://openid.net/specs/openid-connect-core-1_0.html#Authentication
     * </br>
     * https://openid.net/specs/openid-connect-core-1_0.html#AuthorizationExamples
     * </br>
     * 
     * <pre>
     *    code                 Authorization Code Flow
     *    id_token             Implicit Flow
     *    id_token token       Implicit Flow
     *    code id_token        Hybrid Flow
     *    code token           Hybrid Flow
     *    code id_token token  Hybrid Flow
     * </pre>
     * 
     * </br>
     * e.g to
     * see:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-implicit-grant-flow#prefer-the-auth-code-flow
     * </br>
     * </br>
     * Notice: With the plans for third party cookies to be removed from
     * browsers, the implicit grant flow is no longer a suitable authentication
     * method. The silent SSO features of the implicit flow do not work without
     * third party cookies, causing applications to break when they attempt to
     * get a new token. We strongly recommend that all new applications use the
     * authorization code flow that now supports single page apps in place of
     * the implicit flow, and that existing single page apps begin migrating to
     * the authorization code flow as well.
     */
    @Getter
    public static enum StandardResponseType {
        code("code"), id_token("id_token"), token("token"), code_id_token("code", "id_token"), code_token("code",
                "token"), id_token_token("id_token", "token"), code_id_token_token("code", "token", "id_token");

        private final List<String> values;

        private StandardResponseType(String... values) {
            this.values = asList(values);
        }

        /**
         * Check if the this enumerated collection of values is a subset of
         * {@link response_type} to values.
         */
        public boolean containsIn(String response_type) {
            StandardResponseType _responseType = StandardResponseType.of(response_type);
            return getValues().stream().allMatch(t1 -> _responseType.getValues().stream().anyMatch(t2 -> eqIgnCase(t1, t2)));
        }

        public static List<String> getNames() {
            return asList(StandardResponseType.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }

        public static StandardResponseType safeOf(String response_type) {
            Set<String> responseTypes = safeDecodeToParams(response_type);
            for (StandardResponseType v : values()) {
                if (responseTypes.stream()
                        .allMatch(responseType -> v.getValues().stream().anyMatch(t -> t.equalsIgnoreCase(responseType)))) {
                    return v;
                }
            }
            return null;
        }

        public static StandardResponseType of(String response_type) {
            StandardResponseType responseType = safeOf(response_type);
            if (nonNull(responseType)) {
                return responseType;
            }
            throw new IllegalArgumentException(format("unsupported response_type for '%s'", response_type));
        }

        public static boolean isValid(String response_type) {
            Set<String> responseType = safeDecodeToParams(response_type);
            return CollectionUtils2.isSubCollection(responseType, code_id_token_token.getValues());
        }

        public static boolean isAuthorizationCodeFlow(StandardResponseType responseType) {
            return responseType == code;
        }

        public static boolean isImplicitFlow(StandardResponseType responseType) {
            return responseType == id_token || responseType == id_token_token;
        }

        public static boolean isHybridFlow(StandardResponseType responseType) {
            return responseType == code_id_token || responseType == code_token || responseType == code_id_token_token;
        }
    }

    /**
     * response mode definitions. </br>
     * e.g to
     * see:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow#request-an-authorization-code
     * 
     * <pre>
     * RECOMMENDED. Specifies how the identity platform should return the requested token to your app. Supported values:
     *  - query: Default when requesting an access token. Provides the code as a query string parameter on your redirect URI. 
     *     The query parameter is not supported when requesting an ID token by using the implicit flow.
     *  - fragment: Default when requesting an ID token by using the implicit flow. Also supported if requesting only a code.
     *  - form_post: Executes a POST containing the code to your redirect URI. Supported when requesting a code.
     * </pre>
     */
    @Getter
    public static enum StandardResponseMode {
        /**
         * Default
         */
        query, fragment, form_post;

        public static List<String> getNames() {
            return asList(StandardResponseMode.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }

        public static StandardResponseMode safeOf(String name) {
            for (StandardResponseMode v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static StandardResponseMode of(String name) {
            StandardResponseMode result = safeOf(name);
            if (nonNull(result)) {
                return result;
            }
            throw new IllegalArgumentException(format("unsupported response_mode for '%s'", name));
        }
    }

    /**
     * prompt definitions. </br>
     * E.g to
     * see:https://docs.microsoft.com/zh-cn/azure/active-directory/develop/v2-oauth2-auth-code-flow#request-an-authorization-code
     * 
     * <pre>
     * OPTIONAL. Indicates the type of user interaction that is required. Valid values are login, none, consent, and select_account. Supported values:
     *  - prompt=login forces the user to enter their credentials on that request, negating single-sign on.
     *  - prompt=none is the opposite. It ensures that the user isn't presented with any interactive prompt. If the request can't be 
     *       completed silently by using single-sign on, the Microsoft identity platform returns an interaction_required error.
     *  - prompt=consent triggers the OAuth consent dialog after the user signs in, asking the user to grant permissions to the app.
     *  - prompt=select_account interrupts single sign-on providing account selection experience listing all the accounts either in 
     *       session or any remembered account or an option to choose to use a different account altogether.
     * </pre>
     * 
     * E.g to
     * see:https://developer.okta.com/docs/reference/api/oidc/#request-parameters
     */
    @Getter
    public static enum StandardPrompt {
        none(true), login, consent, select_account;

        private final boolean isDefault;

        private StandardPrompt() {
            this.isDefault = false;
        }

        private StandardPrompt(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public static StandardPrompt getDefault() {
            StandardPrompt defaultValue = null;
            for (StandardPrompt v : values()) {
                if (v.isDefault()) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(StandardPrompt.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }

        public static StandardPrompt safeOf(String name) {
            for (StandardPrompt v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static StandardPrompt of(String name) {
            StandardPrompt result = safeOf(name);
            if (nonNull(result)) {
                return result;
            }
            throw new IllegalArgumentException(format("unsupported prompt for '%s'", name));
        }

    }

    /**
     * subject definitions.
     */
    @Getter
    public static enum StandardSubjectType {
        PUBLIC(true), PAIRWISE;
        private final boolean isDefault;

        private StandardSubjectType() {
            this.isDefault = false;
        }

        private StandardSubjectType(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public static StandardSubjectType getDefault() {
            StandardSubjectType defaultValue = null;
            for (StandardSubjectType v : values()) {
                if (v.isDefault) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(StandardSubjectType.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }

        public static StandardSubjectType safeOf(String name) {
            for (StandardSubjectType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static StandardSubjectType of(String name) {
            StandardSubjectType result = safeOf(name);
            if (nonNull(result)) {
                return result;
            }
            throw new IllegalArgumentException(format("unsupported subject type for '%s'", name));
        }
    }

    /**
     * display definitions. </br>
     * https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest
     */
    @Getter
    public static enum StandardDisplay {
        page(true), popup, touch, wap;

        private final boolean isDefault;

        private StandardDisplay() {
            this.isDefault = false;
        }

        private StandardDisplay(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public static StandardDisplay getDefault() {
            StandardDisplay defaultValue = null;
            for (StandardDisplay v : values()) {
                if (v.isDefault) {
                    if (defaultValue != null) {
                        throw new IllegalStateException("There can only be one default value");
                    }
                    defaultValue = v;
                }
            }
            return defaultValue;
        }

        public static List<String> getNames() {
            return asList(StandardDisplay.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }

        public static StandardDisplay safeOf(String name) {
            for (StandardDisplay v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static StandardDisplay of(String name) {
            StandardDisplay result = safeOf(name);
            if (nonNull(result)) {
                return result;
            }
            throw new IllegalArgumentException(format("unsupported display for '%s'", name));
        }
    }

    public static final String TPL_IAM_OIDC_RESPONSE_MODE_FROM_POST_HTML = ResourceUtils2
            .getResourceString(V1OidcIAMConstants.class, "response_mode_from_post_html.tpl");

    /**
     * claims definitions.
     * <p>
     * https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
     * 
     * <pre>
     *   Member                 Type         Description
     *   sub                    string       Subject - Identifier for the End-User at the Issuer.
     *   name                   string       End-User's full name in displayable form including all name parts, possibly including titles and suffixes, ordered according to the End-User's locale and preferences.
     *   given_name             string       Given name(s) or first name(s) of the End-User. Note that in some cultures, people can have multiple given names; all can be present, with the names being separated by space characters.
     *   family_name            string       Surname(s) or last name(s) of the End-User. Note that in some cultures, people can have multiple family names or no family name; all can be present, with the names being separated by space characters.
     *   middle_name            string       Middle name(s) of the End-User. Note that in some cultures, people can have multiple middle names; all can be present, with the names being separated by space characters. Also note that in some cultures, middle names are not used.
     *   nickname               string       Casual name of the End-User that may or may not be the same as the given_name. For instance, a nickname value of Mike might be returned alongside a given_name value of Michael.
     *   preferred_username     string       Shorthand name by which the End-User wishes to be referred to at the RP, such as janedoe or j.doe. This value MAY be any valid JSON string including special characters such as @, /, or whitespace. The RP MUST NOT rely upon this value being unique, as discussed in Section 5.7.
     *   profile                string       URL of the End-User's profile page. The contents of this Web page SHOULD be about the End-User.
     *   picture                string       URL of the End-User's profile picture. This URL MUST refer to an image file (for example, a PNG, JPEG, or GIF image file), rather than to a Web page containing an image. Note that this URL SHOULD specifically reference a profile photo of the End-User suitable for displaying when describing the End-User, rather than an arbitrary photo taken by the End-User.
     *   website                string       URL of the End-User's Web page or blog. This Web page SHOULD contain information published by the End-User or an organization that the End-User is affiliated with.
     *   email                  string       End-User's preferred e-mail address. Its value MUST conform to the RFC 5322 [RFC5322] addr-spec syntax. The RP MUST NOT rely upon this value being unique, as discussed in Section 5.7.
     *   email_verified         boolean      True if the End-User's e-mail address has been verified; otherwise false. When this Claim Value is true, this means that the OP took affirmative steps to ensure that this e-mail address was controlled by the End-User at the time the verification was performed. The means by which an e-mail address is verified is context-specific, and dependent upon the trust framework or contractual agreements within which the parties are operating.
     *   gender                 string       End-User's gender. Values defined by this specification are female and male. Other values MAY be used when neither of the defined values are applicable.
     *   birthdate              string       End-User's birthday, represented as an ISO 8601:2004 [ISO8601‑2004] YYYY-MM-DD format. The year MAY be 0000, indicating that it is omitted. To represent only the year, YYYY format is allowed. Note that depending on the underlying platform's date related function, providing just year can result in varying month and day, so the implementers need to take this factor into account to correctly process the dates.
     *   zoneinfo               string       String from zoneinfo [zoneinfo] time zone database representing the End-User's time zone. For example, Europe/Paris or America/Los_Angeles.
     *   locale                 string       End-User's locale, represented as a BCP47 [RFC5646] language tag. This is typically an ISO 639-1 Alpha-2 [ISO639‑1] language code in lowercase and an ISO 3166-1 Alpha-2 [ISO3166‑1] country code in uppercase, separated by a dash. For example, en-US or fr-CA. As a compatibility note, some implementations have used an underscore as the separator rather than a dash, for example, en_US; Relying Parties MAY choose to accept this locale syntax as well.
     *   phone_number           string       End-User's preferred telephone number. E.164 [E.164] is RECOMMENDED as the format of this Claim, for example, +1 (425) 555-1212 or +56 (2) 687 2400. If the phone number contains an extension, it is RECOMMENDED that the extension be represented using the RFC 3966 [RFC3966] extension syntax, for example, +1 (604) 555-1234;ext=5678.
     *   phone_number_verified  boolean      True if the End-User's phone number has been verified; otherwise false. When this Claim Value is true, this means that the OP took affirmative steps to ensure that this phone number was controlled by the End-User at the time the verification was performed. The means by which a phone number is verified is context-specific, and dependent upon the trust framework or contractual agreements within which the parties are operating. When true, the phone_number Claim MUST be in E.164 format and any extensions MUST be represented in RFC 3966 format.
     *   address                JSON-Object  End-User's preferred postal address. The value of the address member is a JSON [RFC4627] structure containing some or all of the members defined in Section 5.1.1.
     *   updated_at             number       Time the End-User's information was last updated. Its value is a JSON number representing the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time.
     * </pre>
     * </p>
     */
    public static enum StandardClaims {
        /**
         * like is user_id
         */
        sub, name, given_name, family_name, nickname, preferred_username, gender, locale, birthdate, picture, zoneinfo, updated_at,
        /**
         * independent scoped attributes info
         */
        email, email_verified, address, phone_number, phone_number_verified;
        public static List<String> getNames() {
            return asList(StandardClaims.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }
    }

    public static final String KEY_IAM_OIDC_CLAIMS_EXT_NONCE = "nonce";
    public static final String KEY_IAM_OIDC_CLAIMS_EXT_AT_HASH = "at_hash";

    /**
     * claims type definitions. </br>
     * https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
     */
    @Getter
    public static enum StandardClaimType {
        normal/* , aggregated, distributed */;
        public static List<String> getNames() {
            return asList(StandardDisplay.values()).stream().map(v -> v.name().toLowerCase()).collect(toList());
        }

        public static StandardClaimType safeOf(String name) {
            for (StandardClaimType v : values()) {
                if (eqIgnCase(v.name(), name)) {
                    return v;
                }
            }
            return null;
        }

        public static StandardClaimType of(String name) {
            StandardClaimType result = safeOf(name);
            if (nonNull(result)) {
                return result;
            }
            throw new IllegalArgumentException(format("unsupported claim type for '%s'", name));
        }
    }

    /**
     * Safely decode string parameters into a collection separated by spaces.
     */
    public static Set<String> safeDecodeToParams(String paramString) {
        return isBlank(paramString) ? emptySet() : new LinkedHashSet<>(asList(StringUtils.split(urlDecode(paramString), " ")));
    }

}