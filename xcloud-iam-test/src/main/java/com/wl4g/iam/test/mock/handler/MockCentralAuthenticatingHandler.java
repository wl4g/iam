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
package com.wl4g.iam.test.mock.handler;

import static com.wl4g.components.common.lang.Assert2.hasTextOf;
import static com.wl4g.components.common.lang.Assert2.notNull;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.lang.System.currentTimeMillis;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.iam.common.authc.model.SecondaryAuthcValidateResult;
import com.wl4g.iam.common.authc.model.SessionValidateResult;
import com.wl4g.iam.common.authc.model.TicketValidateRequest;
import com.wl4g.iam.common.authc.model.TicketValidateResult;
import com.wl4g.iam.common.config.AbstractIamProperties;
import com.wl4g.iam.common.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.common.handler.AuthenticatingHandler;
import com.wl4g.iam.common.subject.IamPrincipal;
import com.wl4g.iam.common.subject.IamPrincipal.Attributes;
import com.wl4g.iam.common.subject.SimpleIamPrincipal;
import com.wl4g.iam.test.mock.configure.MockAuthenticatingConfigurer;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockAuthcInfo;

/**
 * Default mock central authenticating handler implements
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-08-08
 * @sine v1.0.0
 * @see
 */
public class MockCentralAuthenticatingHandler implements AuthenticatingHandler {

	protected final SmartLogger log = getLogger(getClass());

	/** {@link AbstractIamProperties} */
	@Autowired
	protected AbstractIamProperties<? extends ParamProperties> config;

	/** Mock config of {@link MockConfigurationFactory} */
	@Autowired
	protected MockConfigurationFactory mockFactory;

	@Override
	public TicketValidateResult<IamPrincipal> validate(TicketValidateRequest param) {
		log.debug("Mock validating. param: {}", param);

		TicketValidateResult<IamPrincipal> assertion = new TicketValidateResult<>();
		String grantAppname = param.getApplication();
		hasTextOf(grantAppname, "grantAppname");

		// Grants attributes
		long now = currentTimeMillis();
		assertion.setValidFromTime(now);
		assertion.setValidUntilTime(now + 7200_000);

		// Gets mock configuration by expected principal
		String principal = param.getExtraParameters().get(MockAuthenticatingConfigurer.MOCK_AUTO_AUTHC_PRINCIPAL);
		MockAuthcInfo mock = mockFactory.getMockAuthcInfo(principal);
		notNull(mock, NoSuchMockCredentialsException.class, "No mock credentials were found for '%s'", principal);

		// Principal info
		SimpleIamPrincipal iamPrincipal = new SimpleIamPrincipal(mock.getPrincipalId(), mock.getPrincipal(),
				"<Mock storedCredentials>", mock.getRoles(), mock.getPermissions(), mock.getOrganization());
		assertion.setIamPrincipal(iamPrincipal);

		// Grants roles and permissions attributes
		Attributes attrs = assertion.getIamPrincipal().attributes();
		attrs.setSessionLang(Locale.getDefault().getLanguage());
		attrs.setParentSessionId("<Mock parent sessionId>");
		if (config.getCipher().isEnableDataCipher()) {
			attrs.setDataCipher(("<Mock dataCipher>"));
		}
		if (config.getSession().isEnableAccessTokenValidity()) {
			attrs.setAccessTokenSign("<Mock childAccessTokenSign>");
		}
		attrs.setClientHost("<Mock client host>");

		return assertion;
	}

	@Override
	public SecondaryAuthcValidateResult secondaryValidate(String secondAuthCode, String appName) {
		log.debug("Mock loggedin. secondAuthCode: {}, appName: {}", secondAuthCode, appName);
		return null;
	}

	@Override
	public SessionValidateResult sessionValidate(SessionValidateResult param) {
		log.debug("Mock session validate. param: {}", param);
		return null;
	}

}
