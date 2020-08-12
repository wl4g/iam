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
package com.wl4g.iam.test.handler;

import static com.wl4g.components.common.lang.Assert2.hasTextOf;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.lang.System.currentTimeMillis;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.iam.common.authc.model.SecondAuthcAssertModel;
import com.wl4g.iam.common.authc.model.SessionValidityAssertModel;
import com.wl4g.iam.common.authc.model.TicketValidateModel;
import com.wl4g.iam.common.authc.model.TicketValidatedAssertModel;
import com.wl4g.iam.common.config.AbstractIamProperties;
import com.wl4g.iam.common.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.common.handler.AuthenticatingHandler;
import com.wl4g.iam.common.subject.IamPrincipalInfo;
import com.wl4g.iam.common.subject.SimplePrincipalInfo;
import com.wl4g.iam.test.configure.MockContextConfigureInitializer;
import com.wl4g.iam.test.configure.MockContextConfigureInitializer.IamMockTestConfigWrapper;
import com.wl4g.iam.common.subject.IamPrincipalInfo.Attributes;

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

	@Autowired
	protected AbstractIamProperties<? extends ParamProperties> config;

	@Autowired
	protected MockContextConfigureInitializer configurar;

	@Override
	public TicketValidatedAssertModel<IamPrincipalInfo> validate(TicketValidateModel param) {
		log.debug("Mock validating. param: {}", param);

		TicketValidatedAssertModel<IamPrincipalInfo> assertion = new TicketValidatedAssertModel<>();
		String grantAppname = param.getApplication();
		hasTextOf(grantAppname, "grantAppname");

		// Grant attributes
		long now = currentTimeMillis();
		assertion.setValidFromDate(new Date(now));
		assertion.setValidUntilDate(new Date(now + 7200_000));

		// Mock data configuration
		IamMockTestConfigWrapper mock = configurar.getMockConfigWrapper();

		// Principal info
		SimplePrincipalInfo pinfo = new SimplePrincipalInfo(mock.getPrincipalId(), mock.getPrincipal(),
				"<Mock storedCredentials>", mock.getRoles(), mock.getPermissions(), mock.getOrganization());
		assertion.setPrincipalInfo(pinfo);

		// Grants roles and permissions attributes
		Attributes attrs = assertion.getPrincipalInfo().attributes();
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
	public SecondAuthcAssertModel secondaryValidate(String secondAuthCode, String appName) {
		log.debug("Mock loggedin. secondAuthCode: {}, appName: {}", secondAuthCode, appName);
		return null;
	}

	@Override
	public SessionValidityAssertModel sessionValidate(SessionValidityAssertModel param) {
		log.debug("Mock session validate. param: {}", param);
		return null;
	}

}
