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
package com.wl4g.iam.config.properties;

import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.StringUtils2.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.wl4g.iam.core.config.AbstractIamProperties.IamHttpProxy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Social networking services configuration
 *
 * @author wangl.sir
 * @version v1.0 2019年1月8日
 * @since
 */
@Getter
@Setter
@ToString
public class SnsProperties {

    /**
     * Number of SNS milliseconds of oauth2 connect callback expiration. </br>
     * 
     * Notice: e.g connecting to github can be slow, it is recommended to set a
     * longer setting.
     */
    private long oauth2ConnectExpireMs = 120_000L;

    private DingtalkSocialProperties dingtalk = new DingtalkSocialProperties();
    private LinkedinSocialProperties linkedin = new LinkedinSocialProperties();
    private TwitterSocialProperties twitter = new TwitterSocialProperties();
    private GithubSocialProperties github = new GithubSocialProperties();
    private GoogleSocialProperties google = new GoogleSocialProperties();
    private FacebookSocialProperties facebook = new FacebookSocialProperties();
    private QQSocialProperties qq = new QQSocialProperties();
    private WechatSocialProperties wechat = new WechatSocialProperties();
    private WechatMpSocialProperties wechatMp = new WechatMpSocialProperties();

    /**
     * Abstract socical networking services platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    @Getter
    @Setter
    @ToString
    public static abstract class AbstractSocialProperties {
        private String appId;
        private String appSecret;
        private String redirectUrl;
        private IamHttpProxy proxy = new IamHttpProxy();
    }

    /**
     * Dingtalk open platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    public static class DingtalkSocialProperties extends AbstractSocialProperties {

    }

    /**
     * Linkedin open platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    public static class LinkedinSocialProperties extends AbstractSocialProperties {

    }

    /**
     * Twitter open platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    public static class TwitterSocialProperties extends AbstractSocialProperties {

    }

    /**
     * Github open platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    public static class GithubSocialProperties extends AbstractSocialProperties {

    }

    /**
     * Google open platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    public static class GoogleSocialProperties extends AbstractSocialProperties {

    }

    /**
     * Facebook open platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    public static class FacebookSocialProperties extends AbstractSocialProperties {

    }

    /**
     * QQ open platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    public static class QQSocialProperties extends AbstractSocialProperties {

    }

    /**
     * Wechat open platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    @Getter
    @Setter
    @ToString
    public static class WechatSocialProperties extends AbstractSocialProperties {

        /**
         * Wechat has added a new page style that supports custom
         * authorization.<br/>
         * See:https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=2f2444cf6d55676b11a5de1b8b348ba202dbba8c&lang=zh_CN
         */
        private String href;

        public void setHref(String href) {
            if (!isBlank(href)) {
                // Wechat oauth2 authorization interface only supports HTTPS
                isTrue(startsWithIgnoreCase(href, "HTTPS"), "The 'href' link must be the absolute path of HTTPS protocol");
                this.href = href;
            }
        }

    }

    /**
     * Wechat public platform configuration properties
     *
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年2月17日
     * @since
     */
    public static class WechatMpSocialProperties extends AbstractSocialProperties {

    }

}