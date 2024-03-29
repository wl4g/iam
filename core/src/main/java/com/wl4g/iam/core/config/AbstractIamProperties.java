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
package com.wl4g.iam.core.config;

import static com.wl4g.iam.core.config.CorsProperties.CorsRule.DEFAULT_CORS_ALLOW_HEADER_PREFIX;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.invokeMethod;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.unmodifiableMap;
import static java.util.Locale.US;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ReflectionUtils.getAllDeclaredMethods;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.wl4g.iam.core.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.core.web.servlet.IamCookie;
import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.collection.UniqueList;
import com.wl4g.infra.common.log.SmartLogger;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * IAM abstract configuration properties.
 *
 * @author James Wong
 * @version v1.0.0 2018-09-22
 * @since
 */
@Getter
@Setter
@ToString
public abstract class AbstractIamProperties<P extends ParamProperties> implements InitializingBean, Serializable {
    private static final long serialVersionUID = -5858422822181237865L;

    protected final SmartLogger log = getLogger(getClass());

    /**
     * Spring boot environment.
     */
    @Autowired
    protected Environment environment;

    /**
     * External custom filter chain pattern matching
     */
    private Map<String, String> filterChain = new SafeFilterChainMap();

    /**
     * Session cache configuration properties.
     */
    private CacheProperties cache = new CacheProperties();

    /**
     * Cookie configuration properties.
     */
    private CookieProperties cookie = new CookieProperties();

    /**
     * Session configuration properties.
     */
    private SessionProperties session = new SessionProperties();

    /**
     * Cipher request parameter properties.
     */
    private CipherProperties cipher = new CipherProperties();

    /**
     * Domain security configuration properties.
     */
    private WebSecurityProperties security = new WebSecurityProperties();

    /**
     * HTTP client configuration properties.
     */
    private HttpClientProperties http = new HttpClientProperties();

    /**
     * Application name. e.g. http://host:port/{serviceName}/shiro-cas
     */
    private String serviceName;

    /**
     * Redirect to login URI.</br>
     * e.g. </br>
     * In IAM-Client: {iam-web-uri}/authenticator </br>
     * In IAM-Server: {iam-web-uri}/view/login.html
     *
     * @return
     */
    protected abstract String getLoginUri();

    /**
     * Success URI.
     *
     * @return
     */
    protected abstract String getSuccessUri();

    /**
     * Unauthorized(403) page URI.
     *
     * @return
     */
    protected abstract String getUnauthorizedUri();

    /**
     * IAM parameters configuration properties.
     *
     * @param param
     */
    public abstract P getParam();

    public abstract void setParam(P param);

    @Override
    public void afterPropertiesSet() throws Exception {
        // Apply default properties. (if necessary)
        applyDefaultPropertiesSet();

        // Validation.
        validation();

        // Unmodifiable filter chains.(for safety)
        setFilterChain(unmodifiableMap(getFilterChain()));
    }

    /**
     * Gets spring application name.
     * 
     * @return
     */
    public String getSpringApplicationName() {
        return environment.getProperty("spring.application.name");
    }

    /**
     * Apply default property.
     */
    protected void applyDefaultPropertiesSet() {
        // Sets IAM client defaults serviceName.
        if (isBlank(getServiceName())) {
            setServiceName(getSpringApplicationName());
        }

        // Sets IAM client defaults filter-chains.
        applyBuildinRequiredFilterChains(getFilterChain());

        // Sets IAM client defaults cache configuraton.
        if (isBlank(getCache().getPrefix())) {
            // The cache key distinguishes different environments of the same
            // application, so as to ensure that, for example, redis is shared
            // in the development and test environment, resulting in data
            // pollution.
            String appName = environment.getRequiredProperty("spring.application.name");
            String appEnv = environment.getRequiredProperty("spring.profiles.active");
            getCache().setPrefix(appName.concat(":").concat(appEnv));
        }

    }

    /**
     * Apply build-in required filter chains.
     * 
     * @param pattern
     *            url pattern.
     */
    protected void applyBuildinRequiredFilterChains(Map<String, String> chains) {
        // Adds requires xsrf request rules.
        getFilterChain().putIfAbsent(XsrfProperties.DEFAULT_XSRF_BASE_PATTERN, "anon");
    }

    /**
     * Validation.
     */
    protected void validation() {
        hasTextOf(getServiceName(), "serviceName");
        hasTextOf(getLoginUri(), "loginUri");
        hasTextOf(getSuccessUri(), "successUri");
        hasTextOf(getUnauthorizedUri(), "unauthorizedUri");

        // Check customization parameters name(Mutually exclusive with system
        // parameters built in Iam).

        // Gets other built-in parameters.
        Set<String> builtinParamValues = new HashSet<String>(16);
        for (Method m : getAllDeclaredMethods(getParam().getClass())) {
            if (String.class.isAssignableFrom(m.getReturnType())) {
                builtinParamValues.add(valueOf(invokeMethod(m, getParam())).toUpperCase(US));
            }
        }
        getParam().getCustomParams().forEach(p -> {
            boolean exist = builtinParamValues.contains(valueOf(p).toUpperCase(US));
            isTrue(!exist, "Iam custom parameter name: '%s' that conflict with iam built-in parameters", p);
        });

    }

    /**
     * Session cache configuration properties
     *
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0
     * @date 2018年11月29日
     * @since
     */
    public class CacheProperties implements Serializable {
        private static final long serialVersionUID = 5246530494860631770L;

        /**
         * IAM cache prefix.</br>
         * Note: The cache key distinguishes different environments of the same
         * application, so as to ensure that, for example, redis is shared in
         * the development and test environment, resulting in data pollution.
         */
        private String prefix;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            hasTextOf(prefix, "iamCachePrefix");
            this.prefix = prefix;
        }

    }

    /**
     * Cookie configuration properties
     *
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0
     * @date 2018年11月29日
     * @since
     */
    public class CookieProperties extends IamCookie implements Serializable {

        private static final long serialVersionUID = 918554077474485700L;

        @Override
        public String getName() {
            if (isBlank(super.getName())) {
                setName("_" + getSpringApplicationName());
            }
            hasText(super.getName(), "Cookie name must not be empty.");
            return super.getName();
        }

        /**
         * Specification capitalizes cookie names
         */
        public void setName(String name) {
            super.setName(name.toUpperCase(US));
        }

    }

    /**
     * Session configuration properties
     *
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0
     * @date 2018年11月29日
     * @since
     */
    public class SessionProperties implements Serializable {
        private static final long serialVersionUID = -2694422471812860689L;

        /**
         * Session timeout in milliseconds
         */
        private Long globalSessionTimeout = 1800_000L;

        /**
         * Clean up invalid sessions on a regular basis, and clean up isolated
         * sessions caused by users closing browsers directly
         */
        private Long sessionValidationInterval = 360_000L;

        /**
         * {@link org.apache.shiro.web.session.mgt.DefaultWebSessionManager#setSessionIdUrlRewritingEnabled}
         * EG:http://domain/project/index;JSESSIONID=e5cdc1582aa849a8b36aa4d161e5cd97
         */
        private boolean urlRewriting = false;

        /**
         * When request remember is enabled, it indicates that the address
         * previously remembered will be jumped after successful login, rather
         * than the default home page, except for cas-client requests (which is
         * the application callback address at this time).
         */
        private boolean enableRequestRemember = true;

        /**
         * Whether to enable accesstoken enhanced verification.
         */
        private boolean enableAccessTokenValidity = true;

        public Long getGlobalSessionTimeout() {
            return globalSessionTimeout;
        }

        public void setGlobalSessionTimeout(Long globalSessionTimeout) {
            this.globalSessionTimeout = globalSessionTimeout;
        }

        public Long getSessionValidationInterval() {
            return sessionValidationInterval;
        }

        public void setSessionValidationInterval(Long sessionValidationInterval) {
            this.sessionValidationInterval = sessionValidationInterval;
        }

        public boolean isUrlRewriting() {
            return urlRewriting;
        }

        public void setUrlRewriting(boolean urlRewriting) {
            this.urlRewriting = urlRewriting;
        }

        public boolean isEnableRequestRemember() {
            return enableRequestRemember;
        }

        public void setEnableRequestRemember(boolean enableRequestRemember) {
            this.enableRequestRemember = enableRequestRemember;
        }

        public boolean isEnableAccessTokenValidity() {
            return enableAccessTokenValidity;
        }

        public void setEnableAccessTokenValidity(boolean enableAccessTokenValidity) {
            this.enableAccessTokenValidity = enableAccessTokenValidity;
        }

    }

    /**
     * IAM parameters configuration properties. </br>
     * </br>
     * Note: why not use springmvc to automatically map to beans directly, but
     * define parameter names here? This design is mainly for security and
     * flexibility, and security is very important for the certification center.
     * Therefore, not only the certification mechanism is designed to be very
     * secure, but also the parameters submitted during certification are
     * customized (confused parameter names), In this way, even if the attacker
     * grabs the packet, it will increase their cracking workload.
     * 
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0
     * @date 2018年11月29日
     * @since
     */
    public static class ParamProperties implements Serializable {
        private static final long serialVersionUID = 3258460473777285504L;

        /**
         * This is the version number parameter name of the Iam API.
         */
        private String version = "version";

        /**
         * This SID session is used if the parameter contains the "SID"
         * parameter name.
         */
        private String sid = "__sid";

        /**
         * Save SID to cookie, use this parameter name in browser mode.
         */
        private String sidSaveCookie = "__cookie";

        /**
         * Account parameter name at login time of account password.
         */
        private String principalName = "principal";

        /**
         * Authentication parameter for logout force
         */
        private String logoutForced = "forced";

        /**
         * Authentication parameter for application name
         */
        private String application = "application";

        /**
         * Authentication parameter for grant ticket name
         */
        private String grantTicket = "gt";

        /**
         * Authentication URL parameter for callback redirectURL name
         */
        private String redirectUrl = "redirect_url";

        /**
         * Authentication or authorization processing internal interface custom
         * parameters name.
         */
        private List<String> customParams = new UniqueList<String>(new ArrayList<>()) {
            {
                add("iam-x-type");
                add("iam-x-token");
                add("iam-x-id");
            }
        };

        /**
         * {@link com.wl4g.iam.authc.ServerIamAuthenticationToken.RedirectInfo#useFallbackRedirect}
         */
        private String useFallbackRedirect = "fallbackRedirect";

        /**
         * Name of 'which' parameter of SNS OAuth authentication API
         */
        private String which = "which";

        /**
         * Name of 'state' parameter of SNS OAuth authentication API
         */
        private String state = "state";

        /**
         * SNS callback redirection refresh URL parameter name
         */
        private String refreshUrl = "refresh_url";

        /**
         * SNS callback redirection, whether to use the parameter name of the
         * agent page
         */
        private String agent = "agent";

        /**
         * Number name of resource owner 'authorizers' for SNS OAuth secondary
         * authentication API
         */
        private String authorizers = "authorizers";

        /**
         * Number name of resource owner 'secondAuthCode' for SNS OAuth
         * secondary authentication API
         */
        private String secondaryAuthCode = "secondaryAuthCode";

        /**
         * Number name of resource owner 'funcId' for SNS OAuth secondary
         * authentication API
         */
        private String funcId = "function";

        /**
         * Internationalized language parameter name
         */
        private String i18nLang = "lang";

        // --- [Client's secret & signature. ---

        /**
         * When the client submits the authentication request, it needs to carry
         * the public key (hexadecimal string) generated by itself. In the next
         * step, the server will return (e.g, the secretKey of AES/DES3) as the
         * message encryption key of the later service sensitive api.
         * 
         * @see next-step: {@link #dataCipherKeyName}
         */
        private String clientSecretKeyName = "clientSecretKey";

        /**
         * When the authentication is successful, the access token is generated.
         * It is used to enhance the session based verification logic
         * (originally the idea comes from JWT). In fact, it is a signature of
         * hmacSHA1("signKey", sessionid + umid). Verification logic: the
         * signature value calculated by the server is equal to the signature
         * value submitted by the client, that is, the verification passes.
         * 
         * @see {@link com.wl4g.iam.common.constant.FastCasIAMConstants.IAMDevOpsConstants#KEY_ACCESSTOKEN_SIGN_NAME}
         * @see {@link com.wl4g.iam.core.mgt.IamSubjectFactory#assertRequestSignTokenValidity}
         * @see prev-step:{@link #dataCipherKeyName}
         */
        private String accessTokenName = "accessToken";

        /**
         * When the client authentication is successful, the server will respond
         * encrypted to the {@link #dataCipherKeyName} (using the
         * {@link #clientSecretKeyName} encryption in the previous step).
         * 
         * @see prev-step: {@link #clientSecretKeyName}
         * @see next-step: {@link #accessTokenName}
         */
        private String dataCipherKeyName = "dataCipherKey";

        // --- Client's secret & signature.] ---

        // --- [Authentication info. ---

        /**
         * Authorizes permits information attribute name.
         */
        private String authzPermitsName = "authzPermits";

        // --- Authentication info.] ---

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getSidSaveCookie() {
            return sidSaveCookie;
        }

        public void setSidSaveCookie(String sidSaveCookie) {
            this.sidSaveCookie = sidSaveCookie;
        }

        public String getPrincipalName() {
            return principalName;
        }

        public void setPrincipalName(String loginUsername) {
            this.principalName = loginUsername;
        }

        public String getLogoutForced() {
            return logoutForced;
        }

        public void setLogoutForced(String logoutForced) {
            this.logoutForced = logoutForced;
        }

        public String getApplication() {
            return application;
        }

        public void setApplication(String application) {
            this.application = application;
        }

        public String getGrantTicket() {
            return grantTicket;
        }

        public void setGrantTicket(String grantTicket) {
            this.grantTicket = grantTicket;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        public List<String> getCustomParams() {
            return customParams;
        }

        public void setCustomParams(List<String> customeParams) {
            if (!isEmpty(customeParams)) {
                this.customParams.addAll(customeParams);
            }
        }

        public String getUseFallbackRedirect() {
            return useFallbackRedirect;
        }

        public void setUseFallbackRedirect(String useFallbackRedirect) {
            this.useFallbackRedirect = useFallbackRedirect;
        }

        public String getWhich() {
            return which;
        }

        public void setWhich(String which) {
            this.which = which;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getRefreshUrl() {
            return refreshUrl;
        }

        public void setRefreshUrl(String refreshUrl) {
            this.refreshUrl = refreshUrl;
        }

        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        public String getAuthorizers() {
            return authorizers;
        }

        public void setAuthorizers(String authorizers) {
            this.authorizers = authorizers;
        }

        public String getSecondaryAuthCode() {
            return secondaryAuthCode;
        }

        public void setSecondaryAuthCode(String secondAuthCode) {
            this.secondaryAuthCode = secondAuthCode;
        }

        public String getFuncId() {
            return funcId;
        }

        public void setFuncId(String funcId) {
            this.funcId = funcId;
        }

        public String getI18nLang() {
            return i18nLang;
        }

        public void setI18nLang(String locale) {
            this.i18nLang = locale;
        }

        public String getClientSecretKeyName() {
            return clientSecretKeyName;
        }

        public void setClientSecretKeyName(String clientSecretKeyName) {
            this.clientSecretKeyName = clientSecretKeyName;
        }

        public String getAccessTokenName() {
            return accessTokenName;
        }

        public void setAccessTokenName(String accessTokenName) {
            this.accessTokenName = accessTokenName;
        }

        public String getDataCipherKeyName() {
            return dataCipherKeyName;
        }

        public void setDataCipherKeyName(String dataCipherKeyName) {
            this.dataCipherKeyName = dataCipherKeyName;
        }

        public String getAuthzPermitsName() {
            return authzPermitsName;
        }

        public void setAuthzPermitsName(String authzPermitsName) {
            this.authzPermitsName = authzPermitsName;
        }

    }

    /**
     * Cipher request parameters configuration properties.
     * 
     * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
     * @version 2020年3月28日 v1.0.0
     * @see
     */
    public static class CipherProperties implements Serializable {

        final private static long serialVersionUID = -5701992202765239835L;

        /**
         * Note: the fixed prefix in the request header where the encryption
         * parameter name is defined.
         * 
         * @see {@link #setCipherParameterHeader(List)}
         */
        final public static String CIPHER_HEADER_PREFIX = DEFAULT_CORS_ALLOW_HEADER_PREFIX + "-Cipher-";

        /**
         * Enable data encryption request or not.
         */
        private boolean enableDataCipher = true;

        /**
         * @see {@link #setCipherParameterHeader(List)}
         */
        private Set<String> cipherParameterHeader = new HashSet<String>() {
            private static final long serialVersionUID = 7117402728828798467L;
            {
                // Some commonly used encryption parameter name definitions.
                add(CIPHER_HEADER_PREFIX + "encryptedMobilePhone");
                add(CIPHER_HEADER_PREFIX + "encryptedCredentials");
                add(CIPHER_HEADER_PREFIX + "encryptedBankcardNumber");
            }
        };

        /**
         * Cipher request headder name case sensitive.
         */
        private boolean isCaseSensitive = false;

        public boolean isEnableDataCipher() {
            return enableDataCipher;
        }

        public void setEnableDataCipher(boolean enableDataCipher) {
            this.enableDataCipher = enableDataCipher;
        }

        public Set<String> getCipherParameterHeader() {
            return cipherParameterHeader;
        }

        public CipherProperties setCipherParameterHeader(Set<String> cipherParameterHeader) {
            if (!CollectionUtils.isEmpty(cipherParameterHeader)) {
                for (String param : cipherParameterHeader) {
                    if (!this.cipherParameterHeader.contains(param)) {
                        this.cipherParameterHeader.add(CIPHER_HEADER_PREFIX + param);
                    }
                }
            }
            return this;
        }

        public boolean isCaseSensitive() {
            return isCaseSensitive;
        }

        public CipherProperties setCaseSensitive(boolean isCaseSensitive) {
            this.isCaseSensitive = isCaseSensitive;
            return this;
        }

    }

    /**
     * Web security and risk configuration properties.
     * 
     * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
     * @version 2020年6月20日 v1.0.0
     * @see
     */
    @Getter
    @Setter
    @ToString
    public static class WebSecurityProperties implements Serializable {
        private static final long serialVersionUID = -5701992202115239835L;

        /**
         * Activation web HSTS on profiles environment
         */
        private List<String> hstsOnProfilesActive = new ArrayList<String>(4) {
            private static final long serialVersionUID = 3114838319180606668L;
            {
                add("pro");
                add("prod");
                add("production");
            }
        };
    }

    /**
     * IAM HTTP client configuration properties.
     */
    @Getter
    @Setter
    @ToString
    public static class HttpClientProperties implements Serializable {
        private static final long serialVersionUID = -7274589405956650664L;
        /** Max Idle time of connections */
        private int maxIdleConnections = 200;
        /** The keep alive default is 5 minutes. */
        private long keepAliveDuration = 5;
        /** The connect timeout default is 10 seconds. */
        private long connectTimeout = 3 * 1000L;
        /** The read timeout default is 10 seconds. */
        private long readTimeout = 6 * 1000L;
        /** The write timeout default is 10 seconds. */
        private long writeTimeout = 6 * 1000L;
    }

    /**
     * IAM HTTP proxy.
     */
    @Getter
    @Setter
    @ToString
    public static final class IamHttpProxy {
        private boolean enabled = false;
        private String type = "http";
        private String address = "127.0.0.1";
        private int port = 8118;

        public final Proxy toProxy() {
            for (Proxy.Type t : Proxy.Type.values()) {
                if (eqIgnCase(t.name(), type)) {
                    return new Proxy(t, InetSocketAddress.createUnresolved(address, port));
                }
            }
            throw new IllegalArgumentException(format("Unsupported proxy type '%s'", type));
        }
    }

    /**
     * When a which request connects to a social network (requesting oauth2
     * authorization), the type of destination operation (e.g. login,
     * registration binding)
     *
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0
     * @date 2019年1月7日
     * @since
     */
    public static enum Which {

        /**
         * Used when authorizing login using social accounts.(<font color=red>It
         * operates on the PC side)</font>
         */
        LOGIN,

        /**
         * Used for binding social accounts.(<font color=red>It operates on the
         * PC side)</font>
         */
        BIND,

        /**
         * Used for UnBinding social accounts.(<font color=red>It operates on
         * the PC side)</font>
         */
        UNBIND,

        /**
         * Used when authorizing using social service provider client, for
         * example, authorized login on the client of public platform such as
         * WeChat, QQ, Facebook.(<font color=red>It operates on the Mobile
         * side</font>)
         */
        CLIENT_AUTH,

        /**
         * Used for secondary authentication(SNS mode validation)
         */
        SECOND_AUTH;

        /**
         * Converter string to {@link Which}
         *
         * @param which
         * @return
         */
        public static Which of(String which) {
            Which wh = safeOf(which);
            if (wh == null) {
                throw new IllegalArgumentException(String.format("Illegal which '%s'", which));
            }
            return wh;
        }

        /**
         * Safe converter string to {@link Which}
         *
         * @param which
         * @return
         */
        public static Which safeOf(String which) {
            for (Which t : values()) {
                if (String.valueOf(which).equalsIgnoreCase(t.name())) {
                    return t;
                }
            }
            return null;
        }

    }

    /**
     * {@link IamVersion}
     * 
     * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
     * @version 2020年3月29日 v1.0.0
     * @see
     */
    public static enum IamVersion {

        V2_0_0("v2.0.0");

        private String version;

        private IamVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public static IamVersion safeOf(String version) {
            for (IamVersion v : values()) {
                if (String.valueOf(version).equalsIgnoreCase(v.name())
                        || String.valueOf(version).equalsIgnoreCase(v.getVersion())) {
                    return v;
                }
            }
            return null;
        }

    }

    /**
     * {@link SafeFilterChainMap}
     *
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0 2020年5月25日
     * @since
     */
    public static class SafeFilterChainMap extends LinkedHashMap<String, String> {
        private static final long serialVersionUID = 8580940081413814344L;

        final protected SmartLogger log = getLogger(getClass());

        @Override
        public String put(String pattern, String chain) {
            String res = super.putIfAbsent(pattern, chain);
            if (!isBlank(res)) { // Overwrite not allowed
                log.warn("Ignore set already pattern filter chain: {} => {}", pattern, chain);
            }
            return res;
        }

        @Override
        public void putAll(Map<? extends String, ? extends String> m) {
            if (!CollectionUtils2.isEmpty(m)) {
                m.forEach((k, v) -> put(k, v));
            }
        }

        @Override
        public boolean remove(Object pattern, Object chain) {
            throw new UnsupportedOperationException("Unsupported remove default filter-chains.");
        }

    }

    /**
     * Default view access base URI
     */
    final public static String DEFAULT_VIEW_BASE_URI = "/view";

    /**
     * Default view index page URI.
     */
    public static final String DEFAULT_VIEW_INDEX_URI = DEFAULT_VIEW_BASE_URI + "/index.html";

    /**
     * Default view 403 page URI.
     */
    public static final String DEFAULT_VIEW_403_URI = DEFAULT_VIEW_BASE_URI + "/403.html";

    /**
     * Default view binding page URI.
     */
    public static final String DEFAULT_VIEW_BINDING_URI = DEFAULT_VIEW_BASE_URI + "/binding.html";

}