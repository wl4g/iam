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
package com.wl4g.iam.core.web;

import com.wl4g.infra.context.web.BaseController;
import com.wl4g.iam.common.i18n.SessionResourceMessageBundler;
import com.wl4g.iam.core.annotation.XsrfController;
import com.wl4g.iam.core.config.AbstractIamProperties;
import com.wl4g.iam.core.config.CorsProperties;
import com.wl4g.iam.core.security.xsrf.repository.XsrfToken;
import com.wl4g.iam.core.security.xsrf.repository.XsrfTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.*;
import static com.wl4g.iam.core.security.xsrf.repository.XsrfTokenRepository.XsrfUtil.saveWebXsrfTokenIfNecessary;
import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * XSRF protection controller.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019年10月31日
 * @since
 */
@XsrfController
@ResponseBody
public class XsrfSecurityController extends BaseController {

    /**
     * IAM properties configuration.
     */
    @Autowired
    protected AbstractIamProperties<?> config;

    /**
     * Session delegate message source bundle.
     */
    @javax.annotation.Resource(name = BEAN_SESSION_RESOURCE_MSG_BUNDLER)
    protected SessionResourceMessageBundler bundle;

    @Autowired
    protected XsrfTokenRepository xtokenRepository;

    /**
     * Cors configuration properties
     */
    @Autowired
    protected CorsProperties corsConfig;

    @RequestMapping(method = { HEAD/* , GET, POST */ }, path = URI_XSRF_APPLY_TOKEN)
    public void applyXsrfToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("Apply xsrf token <= {}", request.getRequestURI());

        String requestOrigin = request.getHeader("Origin");
        try {
            hasTextOf(requestOrigin, "xsrfTokenRequestOrigin");
            // Assertion cors origin
            corsConfig.assertCorsOrigin(requestOrigin);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(format("Closing xsrf token response. uri: %s", request.getRequestURI()), e);
            } else {
                log.error(format("Closing xsrf token response. uri: %s, cause: %s", request.getRequestURI(), e.getMessage()));
            }
            try {
                response.getOutputStream().close();
            } catch (Exception e2) {
            }
        }

        // Generate & save xsrf token.
        XsrfToken xtoken = saveWebXsrfTokenIfNecessary(xtokenRepository, request, response, true);

        log.info("Apply xsrf token => {} by requestOrigin: {}", xtoken, requestOrigin);
    }

}