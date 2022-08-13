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
package com.wl4g.iam.sns.github;

import java.util.Map;

import org.apache.shiro.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.wl4g.iam.config.properties.SnsProperties.GithubSocialProperties;
import com.wl4g.iam.sns.GenericOAuth2ApiBinding;
import com.wl4g.iam.sns.github.model.GithubAccessToken;
import com.wl4g.iam.sns.github.model.GithubOpenId;
import com.wl4g.iam.sns.github.model.GithubUserInfo;

import lombok.Getter;
import lombok.ToString;

/**
 * {@link GithubOauth2Template}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-03-23 v1.0.0
 * @since v1.0.0
 */
public class GithubOauth2Template
        extends GenericOAuth2ApiBinding<GithubSocialProperties, GithubAccessToken, GithubOpenId, GithubUserInfo> {

    public static final String PROVIDER_ID = "github";

    public static final String URI_AUTH_CODE = "https://github.com/login/oauth/authorize";
    public static final String URI_ACCESS_TOKEN = "https://github.com/login/oauth/access_token";
    public static final String URI_USER_INFO = "https://api.github.com/user";
    public static final String URI_EMAIL_INFO = "https://api.github.com/email";

    public GithubOauth2Template(GithubSocialProperties config, RestTemplate restTemplate, CacheManager cacheManager) {
        super(config, restTemplate, cacheManager);
    }

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public GithubOpenId getUserOpenId(GithubAccessToken accessToken) {
        // github no get openid flow
        return GithubOpenId.NONE;
    }

    @Override
    protected void additionalCommonHeaders(HttpHeaders headers) {
        // [FIX] http://developer.github.com/v3/#user-agent-required
        headers.add("User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36");
    }

    @Override
    protected MultiValueMap<String, String> additionalUserInfoHeaders(
            MultiValueMap<String, String> headers,
            String accessToken,
            String openId) {
        // [FIX] Must specify access token via Authorization header.
        // https://developer.github.com/changes/2020-02-10-deprecating-auth-through-query-param
        headers.add("Authorization", "Bearer ".concat(accessToken));
        return headers;
    }

    @Override
    protected void postGetAuthorizationCodeUrl(Map<String, String> parameters) {
    }

    @Override
    protected void postGetAccessTokenUrl(Map<String, String> parameters) {

    }

    @Override
    protected void postGetOpenIdUrl(Map<String, String> parameters) {
    }

    @Override
    protected void postGetUserInfoUrl(Map<String, String> parameters) {
    }

    @Override
    protected String scope() {
        return Scope.user_email.getName();
    }

    @Override
    protected String getAuthorizationCodeUriEndpoint() {
        return URI_AUTH_CODE;
    }

    @Override
    protected String decorateRedirectUri(String redirectUri) {
        // [FIX]: github does not recognize the encoded URL and will report the
        // error 'redirect_uri missing', Note: If the port is included, it may
        // match missing.
        return redirectUri;
    }

    @Override
    protected String getAccessTokenUriEndpoint() {
        return URI_ACCESS_TOKEN;
    }

    @Override
    protected String getOpenIdUriEndpoint() {
        // TODO
        return null;
    }

    @Override
    protected String getUserInfoUriEndpoint() {
        return URI_USER_INFO;
    }

    @Getter
    @ToString
    public static enum Scope {
        user_email(true, "user:email");

        private final boolean isDefault;
        private final String name;

        private Scope(boolean isDefault, String name) {
            this.isDefault = isDefault;
            this.name = name;
        }
    }

}