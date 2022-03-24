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
package com.wl4g.iam.captcha.config;

import static com.wl4g.iam.common.constant.IAMConstants.CONF_PREFIX_IAM_SECURITY_CAPTCHA;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.infra.support.cache.locks.JedisLockManager;
import com.wl4g.iam.captcha.jigsaw.JigsawImageManager;
import com.wl4g.iam.captcha.verify.GifSecurityVerifier;
import com.wl4g.iam.captcha.verify.JigsawSecurityVerifier;
import com.wl4g.iam.core.cache.IamCacheManager;

/**
 * {@link CaptchaAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2019-03-24 v1.0.0
 * @since v1.0.0
 */
@Configuration
@ConditionalOnProperty(name = CONF_PREFIX_IAM_SECURITY_CAPTCHA + ".enabled", matchIfMissing = true)
public class CaptchaAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_IAM_SECURITY_CAPTCHA)
    public CaptchaProperties captchaProperties() {
        return new CaptchaProperties();
    }

    /**
     * {@link SimpleJPEGSecurityVerifier}
     * {@link IamAutoConfiguration#captchaHandler}
     * 
     * @return
     */
    @Bean
    public GifSecurityVerifier gifSecurityVerifier() {
        return new GifSecurityVerifier();
    }

    // --- Jigsaw ---

    @Bean
    public JigsawImageManager jigsawImageManager(
            CaptchaProperties config,
            IamCacheManager cacheManager,
            JedisLockManager lockManager) {
        return new JigsawImageManager(config, cacheManager, lockManager);
    }

    @Bean
    public JigsawSecurityVerifier jigsawSecurityVerifier() {
        return new JigsawSecurityVerifier();
    }

}