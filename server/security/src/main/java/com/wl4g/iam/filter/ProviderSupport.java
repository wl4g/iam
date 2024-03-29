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

import java.util.HashSet;
import java.util.Set;

import com.wl4g.iam.core.exception.NoSuchSocialProviderException;

/**
 * Save Supported Social Network Service Providers
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2019年1月7日
 * @since
 */
public enum ProviderSupport {

    /**
     * WeChat public platform definitions.
     */
    WECHATMP("wechatmp", "WeChatMp", "微信"),

    /**
     * WeChat definitions.
     */
    WECHAT("wechat", "WeChat", "微信"),

    /**
     * QQ definitions.
     */
    QQ("qq", "QQ", "企鹅"),

    /**
     * Sina definitions.
     */
    SINA("sina", "Sina", "新浪"),

    /**
     * Google definitions.
     */
    GOOGLE("google", "Google", "谷歌"),

    /**
     * WeChat definitions.
     */
    GITHUB("github", "Github", "Github"),

    /**
     * Facebook definitions.
     */
    FACEBOOK("facebook", "Facebook", "脸书网"),

    /**
     * Dingtalk definitions.
     */
    DINGTALK("dingtalk", "Dingtalk", "钉钉"),

    /**
     * Twitter definitions.
     */
    TWITTER("twitter", "Twitter", "Twitter");

    /**
     * Supported SNS providers.
     */
    private static final Set<String> supported = new HashSet<>();

    private final String name;
    private final String displayNameEn;
    private final String displayNameZh;

    private ProviderSupport(String name, String displayNameEn, String displayNameZh) {
        this.name = name;
        this.displayNameEn = displayNameEn;
        this.displayNameZh = displayNameZh;
    }

    public final String getName() {
        return name;
    }

    public final String getDisplayNameEn() {
        return displayNameEn;
    }

    public final String getDisplayNameZh() {
        return displayNameZh;
    }

    public final boolean isEqual(String provider) {
        return name().equalsIgnoreCase(String.valueOf(provider));
    }

    /**
     * Check if you are a supported social network provider
     *
     * @param provider
     * @throws NoSuchSocialProviderException
     */
    public static final void checkSupport(String provider) throws NoSuchSocialProviderException {
        if (!isSupport(provider)) {
            throw new NoSuchSocialProviderException(String.format("Unsupported SNS service providers:[%s]", provider));
        }
    }

    /**
     * Check if you are a supported social network provider
     *
     * @param provider
     * @return
     */
    public final static boolean isSupport(String provider) {
        return supported.contains(provider);
    }

    /**
     * Add supported social networking service provider
     *
     * @param providerId
     */
    static final void addSupport(String providerId) {
        supported.add(providerId);
    }

}