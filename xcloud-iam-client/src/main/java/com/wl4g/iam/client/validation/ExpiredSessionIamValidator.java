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
package com.wl4g.iam.client.validation;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import com.wl4g.components.common.web.rest.RespBase;
import com.wl4g.components.common.web.rest.RespBase.RetCode;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.core.authc.model.SessionValidateResult;
import com.wl4g.iam.core.exception.InvalidGrantTicketException;
import com.wl4g.iam.core.exception.SessionValidateException;

import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_SESSION_VALIDATE;
import static java.lang.String.format;

/**
 * Expire session validator
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月29日
 * @since
 */
public class ExpiredSessionIamValidator
		extends AbstractBasedIamValidator<SessionValidateResult, SessionValidateResult> {

	public ExpiredSessionIamValidator(IamClientProperties config, RestTemplate restTemplate) {
		super(config, restTemplate);
	}

	@Override
	public SessionValidateResult validate(SessionValidateResult request) throws SessionValidateException {
		final RespBase<SessionValidateResult> resp = doIamRemoteValidate(URI_S_SESSION_VALIDATE, request);
		if (!RespBase.isSuccess(resp)) {
			if (RespBase.eq(resp, RetCode.UNAUTHC)) {
				throw new InvalidGrantTicketException(format("Remote validate error, %s", resp.getMessage()));
			}
			throw new SessionValidateException(format("Remote sessions expires validate error, %s", resp.getMessage()));
		}
		return resp.getData();
	}

	@Override
	protected ParameterizedTypeReference<RespBase<SessionValidateResult>> getTypeReference() {
		return new ParameterizedTypeReference<RespBase<SessionValidateResult>>() {
		};
	}

}