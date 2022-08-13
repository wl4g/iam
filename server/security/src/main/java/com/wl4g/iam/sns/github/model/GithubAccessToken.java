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
package com.wl4g.iam.sns.github.model;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl4g.iam.sns.support.Oauth2AccessToken;
import com.wl4g.infra.common.web.WebUtils2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
public class GithubAccessToken implements Oauth2AccessToken {
    private static final long serialVersionUID = -2472798322235907609L;

    @JsonProperty("access_token")
    private String accessToken;

    // @JsonProperty("expires_in")
    // private Long expiresIn;
    //
    // @JsonProperty("refresh_token")
    // private String refreshToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    @Override
    public String accessToken() {
        return accessToken;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GithubAccessToken build(String message) {
        Map<String, String> params = WebUtils2.toQueryParams(message);
        setAccessToken(params.get("access_token"));
        setScope(params.get("scope"));
        setTokenType(params.get("token_type"));
        return this;
    }

    @Override
    public GithubAccessToken validate(ResponseEntity<String> resp) {
        return this;
    }

}