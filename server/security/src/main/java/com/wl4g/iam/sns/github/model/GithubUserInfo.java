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

import java.util.Date;

import org.springframework.http.ResponseEntity;

import com.wl4g.iam.sns.support.Oauth2UserProfile;

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
public class GithubUserInfo implements Oauth2UserProfile {
    private static final long serialVersionUID = 823944424065492261L;

    //
    // scope=user:email
    //

    private String login;
    private long id;
    private String node_id;
    private String avatar_url;
    private String gravatar_id;
    private String url;
    private String html_url;
    private String followers_url;
    private String following_url;
    private String gists_url;
    private String starred_url;
    private String subscriptions_url;
    private String organizations_url;
    private String repos_url;
    private String events_url;
    private String received_events_url;
    private String type;
    private boolean site_admin;
    private String name;
    private String company;
    private String blog;
    private String location;
    private String email;
    private String hireable;
    private String bio;
    private String twitter_username;
    private int public_repos;
    private int public_gists;
    private int followers;
    private int following;
    private Date created_at;
    private Date updated_at;

    @Override
    public GithubUserInfo validate(ResponseEntity<String> resp) {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GithubUserInfo build(String message) {
        return parseJSON(message, GithubUserInfo.class);
    }

}