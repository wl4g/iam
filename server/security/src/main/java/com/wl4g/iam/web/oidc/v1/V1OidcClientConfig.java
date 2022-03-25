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
package com.wl4g.iam.web.oidc.v1;

import com.wl4g.iam.config.properties.V1OidcProperties;
import com.wl4g.infra.core.utils.bean.BeanCopierUtils;

/**
 * {@link V1OidcClientConfig}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-25 v3.0.0
 * @since v3.0.0
 */
public class V1OidcClientConfig extends V1OidcProperties {
    private static final long serialVersionUID = 4776976002803043619L;

    public V1OidcClientConfig createInstance(V1OidcProperties defaultConfig) {
        V1OidcClientConfig that = new V1OidcClientConfig();
        BeanCopierUtils.mapper(defaultConfig, that);
        return that;
    }

}
