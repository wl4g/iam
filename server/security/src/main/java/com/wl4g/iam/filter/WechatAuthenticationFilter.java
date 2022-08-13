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
package com.wl4g.iam.filter;

import com.wl4g.iam.authc.WechatAuthenticationToken;
import com.wl4g.iam.core.annotation.IamFilter;

/**
 * {@link WechatAuthenticationFilter}
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019年7月8日
 * @since
 */
@IamFilter
public class WechatAuthenticationFilter extends Oauth2SnsAuthenticationFilter<WechatAuthenticationToken> {

    @Override
    public String getName() {
        return ProviderSupport.WECHAT.getName();
    }

    @Override
    public String getUriMapping() {
        return URI_BASE_MAPPING + ProviderSupport.WECHAT.getName();
    }

    @Override
    protected boolean enabled() {
        return true;
    }

}