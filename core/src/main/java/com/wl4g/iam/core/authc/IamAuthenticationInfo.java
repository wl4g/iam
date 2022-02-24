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
package com.wl4g.iam.core.authc;

import java.security.Principal;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authc.AuthenticationInfo;

import com.wl4g.infra.common.codec.CodecSource;
import com.wl4g.iam.common.subject.IamPrincipal;

/**
 * IAM authentication information.
 * 
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2019-11-23
 * @since
 */
public interface IamAuthenticationInfo extends AuthenticationInfo {

	/**
	 * Gets current authentication {@link Principal} information.
	 * 
	 * @return
	 */
	@NotNull
	IamPrincipal getIamPrincipal();

	/**
	 * Gets current authentication credentials public salt. </br>
	 * </br>
	 * for example: Salt is needed when you login with an account and a static
	 * password.
	 * 
	 * @return Salt required for certification verification.
	 * @throws UnsupportedOperationException
	 *             When the current authentication mechanism does not support or
	 *             does not need salt, an exception will be thrown.
	 */
	@NotNull
	default CodecSource getPublicSalt() {
		throw new UnsupportedOperationException();
	}

}