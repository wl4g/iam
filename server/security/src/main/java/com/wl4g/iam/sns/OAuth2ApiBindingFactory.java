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

import com.wl4g.iam.core.exception.NoSuchSocialProviderException;
import com.wl4g.iam.sns.support.Oauth2AccessToken;
import com.wl4g.iam.sns.support.Oauth2OpenId;
import com.wl4g.iam.sns.support.Oauth2UserProfile;

/**
 * IAM Social connection factory
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2019年1月4日
 * @since
 */
public interface OAuth2ApiBindingFactory {

    OAuth2ApiBinding<Oauth2AccessToken, Oauth2OpenId, Oauth2UserProfile> getApiBinding(String provider)
            throws NoSuchSocialProviderException;

}