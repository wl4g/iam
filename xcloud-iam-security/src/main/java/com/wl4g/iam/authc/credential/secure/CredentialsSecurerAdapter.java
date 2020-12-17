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
package com.wl4g.iam.authc.credential.secure;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authc.CredentialsException;
import org.springframework.beans.factory.annotation.Autowired;

import com.wl4g.component.common.codec.CodecSource;
import com.wl4g.iam.core.authc.IamAuthenticationInfo;

/**
 * Credentials securer adapter
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年4月6日
 * @since
 */
public abstract class CredentialsSecurerAdapter implements CredentialsSecurer {

	@Autowired
	private IamCredentialsSecurer securer;

	@Override
	public String signature(@NotNull CredentialsToken token, @NotNull CodecSource publicSalt) {
		return securer.signature(token, publicSalt);
	}

	@Override
	public boolean validate(@NotNull CredentialsToken token, @NotNull IamAuthenticationInfo info)
			throws CredentialsException, RuntimeException {
		return securer.validate(token, info);
	}

}