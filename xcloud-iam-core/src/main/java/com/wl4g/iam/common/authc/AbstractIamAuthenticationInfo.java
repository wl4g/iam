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
package com.wl4g.iam.common.authc;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.util.Assert.notNull;

import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import com.wl4g.iam.common.authc.IamAuthenticationInfo;
import com.wl4g.iam.common.subject.IamPrincipal;

/**
 * Abstract IAM authentication information.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年12月13日
 * @since
 */
public abstract class AbstractIamAuthenticationInfo extends SimpleAuthenticationInfo implements IamAuthenticationInfo {
	private static final long serialVersionUID = -2294251445038637917L;

	/**
	 * IAM principal information.
	 */
	final private IamPrincipal iamPrincipal;

	public AbstractIamAuthenticationInfo(IamPrincipal iamPrincipal, PrincipalCollection principals, String realmName) {
		this(iamPrincipal, principals, null, realmName);
	}

	public AbstractIamAuthenticationInfo(IamPrincipal iamPrincipal, PrincipalCollection principals, ByteSource credentialsSalt,
			String realmName) {
		/*
		 * Password is a string that may be set to empty.
		 * See:xx.secure.AbstractCredentialsSecurerSupport#validate
		 */
		super(principals, (nonNull(iamPrincipal) ? iamPrincipal.getStoredCredentials() : EMPTY));
		notNull(iamPrincipal, "Authenticate iamPrincipal can't null.");
		this.iamPrincipal = iamPrincipal;
		setCredentialsSalt(credentialsSalt);
	}

	@Override
	public IamPrincipal getIamPrincipal() {
		return iamPrincipal;
	}

}