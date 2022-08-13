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
package com.wl4g.iam.core.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;

import com.wl4g.iam.common.model.LoginedModel;
import com.wl4g.iam.common.model.LogoutModel;
import com.wl4g.iam.common.model.SecondaryAuthcValidateModel;
import com.wl4g.iam.common.model.ServiceTicketValidateModel;
import com.wl4g.iam.common.model.ServiceTicketValidateRequest;
import com.wl4g.iam.common.model.SessionValidateModel;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.exception.IllegalApplicationAccessException;
import com.wl4g.iam.core.exception.IllegalCallbackDomainException;

/**
 * Iam central authenticating handler.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
public interface AuthenticatingHandler {

    /**
     * Assertion the validity of the request parameters before executing the
     * login. (that is, verify that the <b>'source application'</b> and the
     * secure callback <b>'redirectUrl'</b> are legitimate)
     *
     * @param appName
     * @param redirectUrl
     * @throws IllegalCallbackDomainException
     */
    default void checkAuthenticateRedirectValidity(String appName, String redirectUrl) throws IllegalCallbackDomainException {
        throw new UnsupportedOperationException();
    }

    /**
     * Assertion whether the current login account has permission to access the
     * application. (that is, validating the legitimacy of <b>'principal'</b>
     * and <b>'application'</b>)
     *
     * @param principal
     * @param appName
     *            From source application
     * @throws IllegalApplicationAccessException
     */
    default void assertApplicationAccessAuthorized(String principal, String appName) throws IllegalApplicationAccessException {
        throw new UnsupportedOperationException();
    }

    /**
     * Validate application request ticket
     *
     * @param model
     *            ticket validation request
     * @return validation assert result
     */
    default ServiceTicketValidateModel<IamPrincipal> validate(ServiceTicketValidateRequest model) {
        throw new UnsupportedOperationException();
    }

    /**
     * Shiro authentication success callback process.
     *
     * @param appName
     *            from source application name
     * @param subject
     *            Shiro subject
     * @return Redirect callback information
     */
    default LoginedModel loggedin(String appName, Subject subject) {
        throw new UnsupportedOperationException();
    }

    /**
     * Logout server session, including all external applications logged-in<br/>
     * <br/>
     * The Iam server logs out with two entries: <br/>
     * 1: access http://iam-client/logout <br/>
     * 2: direct access http://iam-web/logout <br/>
     * {@link com.wl4g.iam.web.CentralAuthenticatingEndpoint#logout()}
     * {@link com.wl4g.iam.filter.LogoutAuthenticationFilter#preHandle()}
     *
     * @param forced
     *            logout forced
     * @param appName
     *            from source application name
     * @param request
     * @param response
     * @return
     */
    default LogoutModel logout(boolean forced, String appName, HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException();
    }

    /**
     * Validation application secondary authentication
     *
     * @param secondAuthCode
     *            Secondary authentication code
     * @param appName
     *            from source application name
     * @return
     */
    default SecondaryAuthcValidateModel secondaryValidate(String secondAuthCode, String appName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sessions expired validation
     *
     * @param param
     * @return
     */
    default SessionValidateModel sessionValidate(SessionValidateModel param) {
        throw new UnsupportedOperationException();
    }

}