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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.wl4g.iam.core.config.AbstractIamProperties.Which;
import com.wl4g.iam.sns.CallbackResult;

/**
 * Social networking services handler
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2019年1月7日
 * @since
 */
public interface SnsHandler {

    /**
     * Handling which(action) type
     *
     * @return
     */
    Which which();

    /**
     * Getting request SNS authorizing URL
     *
     * @param which
     * @param provider
     * @param state
     * @param connectParams
     * @return
     */
    String doGetAuthorizingUrl(Which which, String provider, String state, Map<String, String> connectParams);

    /**
     * SNS authorizing callback
     *
     * @param which
     * @param provider
     * @param state
     * @param code
     * @param request
     * @return
     */
    CallbackResult doCallback(Which which, String provider, String state, String code, HttpServletRequest request);

    /**
     * Request connects to a social network (requesting oauth2 authorization)
     * parameters binding session key name
     */
    public static final String KEY_SNS_CONNECT_PARAMS = "connect_params_";

}