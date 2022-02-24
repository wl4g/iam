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
package com.wl4g.iam.core.authc.model;

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import com.wl4g.infra.common.lang.StringUtils2;

/**
 * {@link TicketValidateRequest}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2018-08-13
 * @since
 */
public final class TicketValidateRequest extends BaseValidateModel {
	private static final long serialVersionUID = 1383145313778896117L;

	/**
	 * Ticket may be empty when the first access is not logged-in<br/>
	 * {@link com.wl4g.devops.iam.web.IamServerController#validate}
	 */
	private String ticket;

	/**
	 * Currently validating IAM client sessionId.
	 */
	@NotBlank
	private String sessionId;

	/**
	 * Currently validating additional extra parameters.
	 */
	private Map<String, String> extraParameters = new HashMap<>();

	public TicketValidateRequest() {
		super();
	}

	public TicketValidateRequest(String ticket, String application, String sessionId) {
		super(application);
		setTicket(ticket);
		setSessionId(sessionId);
	}

	public final String getTicket() {
		return ticket;
	}

	public final TicketValidateRequest setTicket(String ticket) {
		if (!StringUtils2.isEmpty(ticket) && !"NULL".equalsIgnoreCase(ticket)) {
			this.ticket = ticket;
		}
		return this;
	}

	public final String getSessionId() {
		return sessionId;
	}

	public final TicketValidateRequest setSessionId(String sessionId) {
		if (!StringUtils2.isEmpty(sessionId) && !"NULL".equalsIgnoreCase(sessionId)) {
			this.sessionId = sessionId;
		}
		return this;
	}

	public Map<String, String> getExtraParameters() {
		return extraParameters;
	}

	public void setExtraParameters(Map<String, String> extraParameters) {
		if (nonNull(extraParameters)) {
			this.extraParameters = extraParameters;
		}
	}

	public TicketValidateRequest withExtraParameters(Map<String, String> extraParameters) {
		setExtraParameters(extraParameters);
		return this;
	}

}