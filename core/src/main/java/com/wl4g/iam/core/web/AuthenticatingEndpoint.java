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
package com.wl4g.iam.core.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.core.authc.model.LogoutResult;
import com.wl4g.iam.core.authc.model.SecondaryAuthcValidateResult;
import com.wl4g.iam.core.authc.model.SessionValidateModel;
import com.wl4g.iam.core.authc.model.TicketValidateRequest;
import com.wl4g.iam.core.authc.model.TicketValidateResult;

/**
 * IAM server validating authenticating endpoint
 *
 * @author wangl.sir
 * @version v1.0 2019年1月22日
 * @since
 */
public interface AuthenticatingEndpoint {

	/**
	 * Verification based on 'cas1' extension protocol.
	 *
	 * @param param
	 *            TicketValidationRequest parameters
	 * @param bind
	 *            BindingResult
	 * @return TicketAssertion result.
	 */
	default RespBase<TicketValidateResult<IamPrincipal>> validate(@NotNull TicketValidateRequest param) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Global applications logout all
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	default RespBase<LogoutResult> logout(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Secondary certification validation
	 *
	 * @param request
	 * @return
	 */
	default RespBase<SecondaryAuthcValidateResult> secondaryValidate(@NotNull HttpServletRequest request) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sessions expired validation
	 *
	 * @param param
	 * @return
	 */
	default RespBase<SessionValidateModel> sessionValidate(@NotNull SessionValidateModel param) {
		throw new UnsupportedOperationException();
	}

}