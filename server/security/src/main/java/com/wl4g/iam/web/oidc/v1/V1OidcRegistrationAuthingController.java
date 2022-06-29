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

import static com.wl4g.iam.common.constant.V1OidcIAMConstants.URI_IAM_OIDC_ENDPOINT_REGISTRATION;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import com.wl4g.iam.annotation.V1OidcRegistrationController;
import com.wl4g.iam.handler.oidc.v1.V1OidcAuthingHandler;
import com.wl4g.iam.web.oidc.BasedOidcAuthingController;

/**
 * IAM V1-OIDC dynamic registration controller.
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-18 v1.0.0
 * @since v3.0.0
 */
@V1OidcRegistrationController
public class V1OidcRegistrationAuthingController extends BasedOidcAuthingController {

    @SuppressWarnings("unused")
    private @Autowired V1OidcAuthingHandler oidcAuthingHandler;

    /**
     * https://openid.net/specs/openid-connect-registration-1_0.html
     */
    @RequestMapping(value = URI_IAM_OIDC_ENDPOINT_REGISTRATION, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> registration(UriComponentsBuilder uriBuilder, HttpServletRequest req) {
        log.info("called:registration '{}' from '{}'", URI_IAM_OIDC_ENDPOINT_REGISTRATION, req.getRemoteHost());
        // TODO
        return ResponseEntity.ok().body("Not yes implemented");
    }

}
