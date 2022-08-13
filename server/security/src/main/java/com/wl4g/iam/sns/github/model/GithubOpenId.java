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

import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;

import org.apache.shiro.util.Assert;
import org.springframework.http.ResponseEntity;

import com.wl4g.iam.sns.support.Oauth2OpenId;

public class GithubOpenId implements Oauth2OpenId {
    private static final long serialVersionUID = 7990021511401902830L;

    @Override
    public String openId() {
        return null;
    }

    @Override
    public String unionId() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public GithubOpenId build(String message) {
        Assert.notNull(message, "'message' must not be null");
        return parseJSON(message, GithubOpenId.class);
    }

    @Override
    public GithubOpenId validate(ResponseEntity<String> resp) {
        return this;
    }

    public static final GithubOpenId NONE = new GithubOpenId();
}