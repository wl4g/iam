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
package com.wl4g.iam.filter;

import static org.apache.shiro.web.util.WebUtils.getCleanParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wl4g.iam.authc.GenericAuthenticationToken;
import com.wl4g.iam.authc.ServerIamAuthenticationToken.RedirectInfo;
import com.wl4g.iam.core.annotation.IamFilter;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;

import static com.wl4g.component.common.web.WebUtils2.getFirstParameters;
import static com.wl4g.component.common.web.WebUtils2.rejectRequestMethod;
import static com.wl4g.iam.verification.SecurityVerifier.VerifyKind.*;

/**
 * {@link GenericAuthenticationFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2019-08-13
 * @sine v1.0.0
 * @see
 */
@IamFilter
public class GenericAuthenticationFilter extends AbstractServerIamAuthenticationFilter<GenericAuthenticationToken> {
	final public static String NAME = "generic";

	@Override
	protected GenericAuthenticationToken doCreateToken(String remoteHost, RedirectInfo redirectInfo, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		rejectRequestMethod(true, request, response, "POST");

		// Bsse required parameters.
		final String principal = getCleanParam(request, config.getParam().getPrincipalName());
		final String cipherCredentials = getCleanParam(request, config.getParam().getCredentialsName());
		final String algKind = getCleanParam(request, config.getParam().getSecretAlgKindName());
		final String clientSecretKey = getCleanParam(request, config.getParam().getClientSecretKeyName());
		final String umidToken = getCleanParam(request, config.getParam().getUmidTokenName());
		final String clientRef = getCleanParam(request, config.getParam().getClientRefName());
		final String verifiedToken = getCleanParam(request, config.getParam().getVerifiedTokenName());

		// Additional optional parameters.
		GenericAuthenticationToken token = new GenericAuthenticationToken(remoteHost, redirectInfo, principal, cipherCredentials,
				CryptKind.of(algKind), clientSecretKey, umidToken, clientRef, verifiedToken, of(request));

		// Extra custom parameters.
		token.setExtraParameters(getFirstParameters(request));

		return token;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getUriMapping() {
		return URI_BASE_MAPPING + NAME;
	}

}