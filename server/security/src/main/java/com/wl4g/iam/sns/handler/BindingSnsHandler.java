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
package com.wl4g.iam.sns.handler;

import static com.wl4g.infra.common.lang.Exceptions.getRootCauses;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.KEY_ERR_SESSION_SAVED;
import static com.wl4g.iam.core.utils.IamSecurityHolder.bind;

import com.wl4g.iam.common.bean.SocialConnectInfo;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.config.properties.SnsProperties;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.core.config.AbstractIamProperties.Which;
import com.wl4g.iam.sns.OAuth2ApiBindingFactory;

/**
 * Binding SNS handler
 *
 * @author wangl.sir
 * @version v1.0 2019年2月28日
 * @since
 */
public class BindingSnsHandler extends BasedBindSnsHandler {

    public BindingSnsHandler(IamProperties config, SnsProperties snsConfig, OAuth2ApiBindingFactory connectFactory,
            ServerSecurityConfigurer context) {
        super(config, snsConfig, connectFactory, context);
    }

    @Override
    public Which which() {
        return Which.BIND;
    }

    @Override
    protected void postBindingProcess(SocialConnectInfo info) {
        try {
            configurer.bindSocialConnection(info);
        } catch (Throwable e) {
            log.warn("Failed to binding sns.", e);
            // Save error to session
            bind(KEY_ERR_SESSION_SAVED, getRootCauses(e).getMessage());
        }
    }

}