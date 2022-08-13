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
package com.wl4g.iam.captcha.config;

import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link CaptchaProperties}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2019-03-23 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
public class CaptchaProperties {

    private KaptchaProperties kaptcha = new KaptchaProperties();
    private GifProperties gif = new GifProperties();
    private JigsawProperties jigsaw = new JigsawProperties();

    /**
     * Kaptcha configuration properties
     * 
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0 2019-09-02
     * @since
     */
    @Getter
    @Setter
    @ToString
    public static class KaptchaProperties {
        private Properties properties = new Properties();

        public KaptchaProperties() {
            // Default kaptcha settings
            getProperties().put("kaptcha.border", "no");
            getProperties().put("kaptcha.border.color", "red");
            getProperties().put("kaptcha.border.thickness", "5");
            getProperties().put("kaptcha.image.width", "150");
            getProperties().put("kaptcha.image.height", "50");
            // 0,0,205 black
            getProperties().put("kaptcha.noise.color", "0,0,205");
            // 255,250,205
            getProperties().put("kaptcha.background.clear.from", "178,223,238");
            getProperties().put("kaptcha.background.clear.to", "240,255,240");
            getProperties().put("kaptcha.textproducer.font.names", "微软雅黑");
            getProperties().put("kaptcha.textproducer.font.size", "30");
            // 255,110,180
            getProperties().put("kaptcha.textproducer.font.color", "72,118,255");
            getProperties().put("kaptcha.textproducer.char.space", "3");
            getProperties().put("kaptcha.textproducer.char.string", "ABCDEFGHJKMNQRSTUVWXYZ123456789");
            getProperties().put("kaptcha.textproducer.char.length", "5");
        }
    }

    /**
     * Gif configuration properties.
     * 
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0 2019-09-02
     * @since
     */
    @Getter
    @Setter
    @ToString
    public static class GifProperties {
    }

    /**
     * Jigsaw configuration properties.
     * 
     * @author James Wong<jamewong1376@gmail.com>
     * @version v1.0 2019-09-02
     * @since
     */
    @Getter
    @Setter
    @ToString
    public static class JigsawProperties {

        /** Jigsaw image cache pool size. */
        private int poolImgSize = 64;

        /** Jigsaw image cache expireSec. */
        private int poolImgExpireSec = 30 * 60;

        /** Source image directory. */
        private String sourceDir;

        /** Analyze verification of pixels allowing X-offset. */
        private int allowOffsetX = 4;
    }

}