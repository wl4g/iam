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
package com.wl4g.iam.sns;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.isInstanceOf;
import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.notEmpty;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Charsets;
import com.wl4g.iam.config.properties.SnsProperties.AbstractSocialProperties;
import com.wl4g.iam.core.cache.IamCache;
import com.wl4g.iam.core.config.AbstractIamConfiguration.IamDynamicProxySelector;
import com.wl4g.iam.core.exception.SnsApiBindingException;
import com.wl4g.iam.sns.support.OAuth2GrantType;
import com.wl4g.iam.sns.support.OAuth2ResponseType;
import com.wl4g.iam.sns.support.Oauth2AccessToken;
import com.wl4g.iam.sns.support.Oauth2OpenId;
import com.wl4g.iam.sns.support.Oauth2UserProfile;
import com.wl4g.infra.common.web.WebUtils2;

/**
 * Abstract generic based social networking connection binding implement
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2019年1月7日
 * @since
 */
public abstract class GenericOAuth2ApiBinding<C extends AbstractSocialProperties, T extends Oauth2AccessToken, O extends Oauth2OpenId, U extends Oauth2UserProfile>
        implements OAuth2ApiBinding<T, O, U>, InitializingBean {
    public static final String DEFAULT_CACHE_NAME = "social_";

    public static final String DEFAULT_PARAM_CLIENT_ID = "client_id";
    public static final String DEFAULT_PARAM_CLIENT_SECRET = "client_secret";
    public static final String DEFAULT_PARAM_AUTH_CODE = "code";
    public static final String DEFAULT_PARAM_REDIRECT_URI = "redirect_uri";
    public static final String DEFAULT_PARAM_STATE = "state";
    public static final String DEFAULT_PARAM_SCOPE = "scope";
    public static final String DEFAULT_PARAM_ACCESS_TOKEN = "access_token";
    public static final String DEFAULT_PARAM_OPEN_ID = "openid";
    public static final String DEFAULT_PARAM_GRANT_TYPE = "grant_type";
    public static final String DEFAULT_PARAM_RESPONSE_TYPE = "response_type";

    protected final Logger log = getLogger(getClass());

    private @Autowired IamDynamicProxySelector proxySelector;
    protected final C config;
    protected final RestTemplate restTemplate;
    protected final CacheManager cacheManager;
    protected final IamCache cache;

    public GenericOAuth2ApiBinding(C config, RestTemplate restTemplate, CacheManager cacheManager) {
        this.config = notNullOf(config, "config");
        this.restTemplate = notNullOf(restTemplate, "restTemplate");
        this.cacheManager = notNullOf(cacheManager, "cacheManager");
        Object cacheObject = cacheManager.getCache(DEFAULT_CACHE_NAME);
        this.cache = (IamCache) isInstanceOf(IamCache.class, notNullOf(cacheObject, "cacheObject"));

        // Additional request common headers.
        restTemplate.getInterceptors().add((request, body, execution) -> {
            additionalCommonHeaders(request.getHeaders());
            return execution.execute(request, body);
        });
    }

    //
    // P R E _ O A U T H 2 _ M E T H O D
    //

    @Override
    public void afterPropertiesSet() throws Exception {
        if (config.getProxy().isEnabled()) {
            if (!isBlank(getAccessTokenUriEndpoint())) {
                proxySelector.register(URI.create(getAccessTokenUriEndpoint()), config.getProxy().toProxy());
            }
            if (!isBlank(getOpenIdUriEndpoint())) {
                proxySelector.register(URI.create(getOpenIdUriEndpoint()), config.getProxy().toProxy());
            }
            if (!isBlank(getUserInfoUriEndpoint())) {
                proxySelector.register(URI.create(getUserInfoUriEndpoint()), config.getProxy().toProxy());
            }
        }
    }

    @Override
    public String getAuthorizeCodeUrl(String state, Map<String, String> queryParams) {
        Map<String, String> parameters = createRequestParameters();

        // Client ID
        hasText(config.getAppId(), "'appId' is empty, please check the configure");
        parameters.put(DEFAULT_PARAM_CLIENT_ID, config.getAppId());

        // State
        String stateVal = StringUtils.hasText(state) ? state : state();
        parameters.put(DEFAULT_PARAM_STATE, stateVal);

        // Scope
        if (StringUtils.hasText(scope())) {
            parameters.put(DEFAULT_PARAM_SCOPE, scope());
        }

        // Redirect URL
        hasText(config.getRedirectUrl(), "'redirect_url' is empty, please check the configure");
        // Extra query parameters
        String redirectUri = config.getRedirectUrl();
        if (queryParams != null && !queryParams.isEmpty()) {
            redirectUri = WebUtils2.applyQueryURL(redirectUri, queryParams);
        }
        parameters.put(DEFAULT_PARAM_REDIRECT_URI, decorateRedirectUri(redirectUri));

        // Response type
        OAuth2ResponseType rt = (responseType() != null) ? responseType() : OAuth2ResponseType.getDefault();
        parameters.put(DEFAULT_PARAM_RESPONSE_TYPE, rt.name().toLowerCase());

        // Post process
        postGetAuthorizationCodeUrl(parameters);

        String url = toParametersUrl(getAuthorizationCodeUriEndpoint(), parameters);
        log.debug("authorization url='{}'", url);

        return url.toString();
    }

    //
    // A P I _ M E T H O D
    //

    @SuppressWarnings("unchecked")
    @Override
    public T getAccessToken(String code) {
        Map<String, String> parameters = createRequestParameters();

        hasText(config.getAppId(), "'appId' is empty, please check the configure");
        parameters.put(DEFAULT_PARAM_CLIENT_ID, config.getAppId());

        hasText(config.getAppSecret(), "'appSecret' is empty, please check the configure");
        parameters.put(DEFAULT_PARAM_CLIENT_SECRET, config.getAppSecret());

        // Consistent with the previous getAuthorizeCodeUrl step
        hasText(config.getRedirectUrl(), "'redirect_url' is empty, please check the configure");
        parameters.put(DEFAULT_PARAM_REDIRECT_URI, config.getRedirectUrl());

        // grant_type
        OAuth2GrantType gt = (grantType() != null) ? grantType() : OAuth2GrantType.getDefault();
        parameters.put(DEFAULT_PARAM_GRANT_TYPE, gt.name().toLowerCase());

        if (gt == OAuth2GrantType.AUTHORIZATION_CODE) {
            isTrue(StringUtils.hasText(code), "'code' is empty, please check the configure");
            parameters.put(DEFAULT_PARAM_AUTH_CODE, code);
        }

        // Post process
        postGetAccessTokenUrl(parameters);

        String url = toParametersUrl(getAccessTokenUriEndpoint(), parameters);
        log.info("access_token url='{}'", url);

        // Execution request.
        RequestEntity<Object> entity = new RequestEntity<>(additionalAccessTokenHeaders(new LinkedMultiValueMap<>()),
                HttpMethod.GET, URI.create(url));
        ResponseEntity<String> resp = restTemplate.exchange(entity, String.class);
        if (nonNull(resp) && resp.getStatusCode() == HttpStatus.OK) {
            String accessTokenStr = resp.getBody();
            hasText(accessTokenStr, "OAuth2 response openId empty");
            accessTokenStr = new String(accessTokenStr.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
            log.debug("Resp accessToken='{}'", accessTokenStr);
            return (T) ((Oauth2AccessToken) createResponseMessage(1)).build(accessTokenStr).validate(resp);
        }

        throw new SnsApiBindingException(format("Failed to receiving OAuth2 accessToken of - %s", resp));
    }

    @SuppressWarnings("unchecked")
    @Override
    public O getUserOpenId(T accessToken) {
        Map<String, String> parameters = createRequestParameters();

        notNull(accessToken, "'accessToken' is empty, please check the configure");
        hasText(accessToken.accessToken(), "'accessToken' is empty, please check the configure");
        parameters.put(DEFAULT_PARAM_ACCESS_TOKEN, accessToken.accessToken());

        // Post process
        postGetOpenIdUrl(parameters);

        String url = toParametersUrl(getOpenIdUriEndpoint(), parameters);
        log.info("openid url='{}'", url);

        // Execution request.
        RequestEntity<Object> entity = new RequestEntity<>(
                additionalOpenIdHeaders(new LinkedMultiValueMap<>(), accessToken.accessToken()), HttpMethod.GET, URI.create(url));
        ResponseEntity<String> resp = restTemplate.exchange(entity, String.class);
        if (nonNull(resp) && resp.getStatusCode() == HttpStatus.OK) {
            String openIdStr = resp.getBody();
            hasText(openIdStr, "OAuth2 response openId empty");
            openIdStr = new String(openIdStr.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
            log.debug("Resp openid='{}'", openIdStr);
            return (O) ((Oauth2OpenId) createResponseMessage(2)).build(openIdStr).validate(resp);
        }

        throw new SnsApiBindingException(format("Failed to receiving OAuth2 openid of - %s", resp));
    }

    @SuppressWarnings("unchecked")
    @Override
    public U getUserInfo(String accessToken, String openId) {
        Map<String, String> parameters = createRequestParameters();

        hasText(config.getAppId(), "'appId' is empty, please check the configure");
        parameters.put(DEFAULT_PARAM_CLIENT_ID, config.getAppId());

        hasText(accessToken, "'accessToken' is empty, please check the configure");
        parameters.put(DEFAULT_PARAM_ACCESS_TOKEN, accessToken);

        // such as: github oauth2/qq oauth2 does not have this flow.
        if (!isBlank(openId)) {
            parameters.put(DEFAULT_PARAM_OPEN_ID, openId);
        }

        // Post process
        postGetUserInfoUrl(parameters);

        String url = toParametersUrl(getUserInfoUriEndpoint(), parameters);
        log.info("user_info url='{}'", url);

        // Execution request.
        RequestEntity<Object> entity = new RequestEntity<>(
                additionalUserInfoHeaders(new LinkedMultiValueMap<>(), accessToken, openId), HttpMethod.GET, URI.create(url));
        ResponseEntity<String> resp = restTemplate.exchange(entity, String.class);
        if (nonNull(resp) && resp.getStatusCode() == HttpStatus.OK) {
            String body = resp.getBody();
            hasText(body, "OAuth2 response userinfo empty");
            body = new String(body.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
            log.debug("Resp userInfo='{}'", body);
            return (U) ((Oauth2UserProfile) createResponseMessage(3)).build(body).validate(resp);
        }

        throw new SnsApiBindingException(format("Failed to receiving OAuth2 userinfo of - %s", resp));
    }

    //
    // U R I _ E N D P O I N T _ M E T H O D
    //

    protected abstract String getAuthorizationCodeUriEndpoint();

    protected abstract String getAccessTokenUriEndpoint();

    protected abstract String getOpenIdUriEndpoint();

    protected abstract String getUserInfoUriEndpoint();

    protected void additionalCommonHeaders(HttpHeaders headers) {
    }

    protected MultiValueMap<String, String> additionalAccessTokenHeaders(MultiValueMap<String, String> headers) {
        return headers;
    }

    protected MultiValueMap<String, String> additionalOpenIdHeaders(MultiValueMap<String, String> headers, String accessToken) {
        return headers;
    }

    protected MultiValueMap<String, String> additionalUserInfoHeaders(
            MultiValueMap<String, String> headers,
            String accessToken,
            String openId) {
        return headers;
    }

    protected String decorateRedirectUri(String redirectUri) {
        return WebUtils2.safeEncodeURL(redirectUri);
    }

    //
    // C O N F I G U E _ M E T H O D
    //

    protected String state() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    protected abstract String scope();

    protected OAuth2GrantType grantType() {
        return OAuth2GrantType.getDefault();
    }

    protected OAuth2ResponseType responseType() {
        return OAuth2ResponseType.getDefault();
    }

    //
    // P O S T _ P R O C E S S _ M E T H O D
    //

    protected abstract void postGetAuthorizationCodeUrl(Map<String, String> parameters);

    protected abstract void postGetAccessTokenUrl(Map<String, String> parameters);

    protected abstract void postGetOpenIdUrl(Map<String, String> parameters);

    protected abstract void postGetUserInfoUrl(Map<String, String> parameters);

    //
    // O T H T E R _ M E T H O D
    //

    private String toParametersUrl(String baseUri, Map<String, String> parameters) {
        notNull(baseUri, "'baseUri' is empty, please check the configure");
        notEmpty(parameters, "'parameters' is empty, please check the configure");
        /*
         * eg:
         * https://open.weixin.qq.com/connect/qrconnect?appid=APPID&redirect_uri
         * =REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#
         * wechat_redirect
         */
        String fragment = ""; // Location anchors (tracing points) in HTML pages
        if (baseUri.contains("#")) {
            fragment = baseUri.substring(baseUri.lastIndexOf("#"), baseUri.length());
            baseUri = baseUri.substring(0, baseUri.lastIndexOf("#"));
        }
        StringBuffer url = new StringBuffer(baseUri).append("?");
        parameters.forEach((k, v) -> url.append(k).append("=").append(v).append("&"));
        return url.substring(0, url.length() - 1).toString().concat(fragment);
    }

    @SuppressWarnings("unchecked")
    private <E> E createResponseMessage(int index) {
        try {
            ResolvableType resolveType = ResolvableType.forClass(getClass());
            return (E) resolveType.getSuperType().getGeneric(index).resolve().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> createRequestParameters() {
        return new LinkedHashMap<>();
    }

}