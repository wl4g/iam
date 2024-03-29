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
package com.wl4g.iam.sns;

import java.util.Map;

import com.wl4g.iam.sns.support.Oauth2AccessToken;
import com.wl4g.iam.sns.support.Oauth2OpenId;
import com.wl4g.iam.sns.support.Oauth2UserProfile;

/**
 * {@link OAuth2ApiBinding}
 * 
 * @param <T>
 * @param <O>
 * @param <U>
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年2月7日 v1.0.0
 * @see
 */
public interface OAuth2ApiBinding<T extends Oauth2AccessToken, O extends Oauth2OpenId, U extends Oauth2UserProfile> {

    String providerId();

    String getAuthorizeCodeUrl(String state, Map<String, String> queryParams);

    T getAccessToken(String code);

    O getUserOpenId(T accessToken);

    U getUserInfo(String accessToken, String openId);

}