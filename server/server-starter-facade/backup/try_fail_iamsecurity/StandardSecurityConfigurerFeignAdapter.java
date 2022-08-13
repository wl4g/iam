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
package com.wl4g.iam.service;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.iam.common.bean.FastCasClientInfo;
import com.wl4g.iam.common.bean.SocialConnectInfo;
import com.wl4g.iam.configure.ServerSecurityConfigurer;
import com.wl4g.iam.core.exception.BindingConstraintsException;
import com.wl4g.iam.core.subject.IamPrincipal;
import com.wl4g.iam.core.subject.IamPrincipal.Parameter;

/**
 * {@link StandardSecurityConfigurerFeignAdapter}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-01
 * @sine v1.0
 * @see
 */
@FeignClient("areaService")
@RequestMapping("/standardServerSecurityConfigurer")
public interface StandardSecurityConfigurerFeignAdapter extends ServerSecurityConfigurer {

	@Override
	default String decorateAuthenticateSuccessUrl(String successUrl, AuthenticationToken token, Subject subject,
			ServletRequest request, ServletResponse response) {
		return ServerSecurityConfigurer.super.decorateAuthenticateSuccessUrl(successUrl, token, subject, request, response);
	}

	@Override
	default String decorateAuthenticateFailureUrl(String loginUrl, AuthenticationToken token, Throwable ae,
			ServletRequest request, ServletResponse response) {
		return ServerSecurityConfigurer.super.decorateAuthenticateFailureUrl(loginUrl, token, ae, request, response);
	}

	@Override
	FastCasClientInfo getFastCasClientInfo(String appName);

	@Override
	List<FastCasClientInfo> findFastCasClientInfo(String... appNames);

	@Override
	IamPrincipal getIamUserDetail(Parameter parameter);

	@Override
	boolean isApplicationAccessAuthorized(String principal, String application);

	@Override
	<T extends SocialConnectInfo> List<T> findSocialConnections(String principal, String provider);

	@Override
	void bindSocialConnection(SocialConnectInfo social) throws BindingConstraintsException;

	@Override
	void unbindSocialConnection(SocialConnectInfo social) throws BindingConstraintsException;

}
