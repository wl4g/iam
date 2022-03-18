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
package com.wl4g.iam.web.fastcas;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.infra.common.web.WebUtils2.getFullRequestURL;
import static com.wl4g.infra.common.web.WebUtils2.isTrue;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_S_LOGOUT;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_S_SECOND_VALIDATE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_S_SESSION_VALIDATE;
import static com.wl4g.iam.common.constant.FastCasIAMConstants.URI_S_VALIDATE;
import static com.wl4g.iam.core.utils.IamSecurityHolder.getSessionId;
import static org.apache.shiro.web.util.WebUtils.getCleanParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.shiro.web.util.WebUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.annotation.FastCasController;
import com.wl4g.iam.common.model.LogoutModel;
import com.wl4g.iam.common.model.SecondaryAuthcValidateModel;
import com.wl4g.iam.common.model.SessionValidateModel;
import com.wl4g.iam.common.model.ServiceTicketValidateRequest;
import com.wl4g.iam.common.model.ServiceTicketValidateModel;
import com.wl4g.iam.core.web.AuthenticatingController;
import com.wl4g.iam.web.BaseIamController;

/**
 * IAM(Fast-CAS) central authenticator controller
 *
 * @author wangl.sir
 * @version v1.0 2019年1月22日
 * @since
 */
@FastCasController
public class FastCasServerAuthenticatingController extends BaseIamController implements AuthenticatingController {

    @PostMapping(URI_S_VALIDATE)
    @ResponseBody
    @Override
    public RespBase<ServiceTicketValidateModel<IamPrincipal>> validate(@NotNull @RequestBody ServiceTicketValidateRequest param) {
        log.info("Ticket validating, sessionId: {} <= {}", getSessionId(), toJSONString(param));

        RespBase<ServiceTicketValidateModel<IamPrincipal>> resp = new RespBase<>();
        // Ticket assertion.
        resp.setData(authHandler.validate(param));

        log.info("Ticket validated. => {}", resp);
        return resp;
    }

    @PostMapping(URI_S_SECOND_VALIDATE)
    @ResponseBody
    @Override
    public RespBase<SecondaryAuthcValidateModel> secondaryValidate(HttpServletRequest request) {
        log.info("Secondary validating ... sessionId={} <= {}", getSessionId(), getFullRequestURL(request));
        RespBase<SecondaryAuthcValidateModel> resp = RespBase.create();

        String secondAuthCode = WebUtils.getCleanParam(request, config.getParam().getSecondaryAuthCode());
        String fromAppName = WebUtils.getCleanParam(request, config.getParam().getApplication());
        resp.setData(authHandler.secondaryValidate(secondAuthCode, fromAppName));

        log.info("Secondary validated. => {}", resp);
        return resp;
    }

    @PostMapping(URI_S_SESSION_VALIDATE)
    @ResponseBody
    @Override
    public RespBase<SessionValidateModel> sessionValidate(@NotNull @RequestBody SessionValidateModel req) {
        log.info("Sessions validating ...  <= {}", toJSONString(req));
        RespBase<SessionValidateModel> resp = RespBase.create();

        resp.setData(authHandler.sessionValidate(req));

        log.info("Sessions validated. => {}", resp);
        return resp;
    }

    @PostMapping(URI_S_LOGOUT)
    @ResponseBody
    @Override
    public RespBase<LogoutModel> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout... <= {}", getFullRequestURL(request));

        RespBase<LogoutModel> resp = new RespBase<>();
        String appName = getCleanParam(request, config.getParam().getApplication());
        // hasTextOf(fromAppName, config.getParam().getApplication());

        // Using coercion ignores remote exit failures
        boolean forced = isTrue(request, config.getParam().getLogoutForced(), true);
        resp.setData(authHandler.logout(forced, appName, request, response));

        log.info("Logout. => {}", resp);
        return resp;
    }

}