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
package com.wl4g.iam.sns.wechat.api.model;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WechatMp APIs credentials bean of {@link WxmpAccessToken}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-08-07
 * @since
 */
public class WxmpAccessToken extends WxmpBase {

    /**
     * Credential token value
     */
    @JsonProperty("access_token")
    private String token;

    /**
     * Effective time of voucher, unit: seconds
     */
    @JsonProperty("expires_in")
    private int expiresIn;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
    }

}