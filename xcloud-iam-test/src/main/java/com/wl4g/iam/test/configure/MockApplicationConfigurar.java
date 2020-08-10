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
package com.wl4g.iam.test.configure;

import static com.wl4g.components.common.collection.Collections2.safeMap;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import static com.wl4g.components.core.utils.AopUtils2.*;

import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.common.subject.IamPrincipalInfo.OrganizationInfo;
import com.wl4g.iam.common.subject.IamPrincipalInfo.PrincipalOrganization;
import com.wl4g.iam.test.annotation.EnableIamMockTest;
import com.wl4g.iam.test.annotation.EnableIamMockTest.IamMockOrganization;

/**
 * {@link MockApplicationConfigurar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-08-10
 * @sine v1.0.0
 * @see
 */
public class MockApplicationConfigurar implements InitializingBean {

	/** {@link ApplicationContext} */
	@Autowired
	protected ApplicationContext actx;

	/** {@link IamClientProperties} */
	@Autowired
	protected IamClientProperties config;

	/** Mock roles */
	protected String roles;

	/** Mock permissions */
	protected String permissions;

	/** {@link PrincipalOrganization} */
	protected PrincipalOrganization mockOrganization;

	public String getRoles() {
		return roles;
	}

	public String getPermissions() {
		return permissions;
	}

	public PrincipalOrganization getMockOrganization() {
		return mockOrganization;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		applyMockServiceProperties();
		parseMockConfiguration();
	}

	/**
	 * Parse mock configuration
	 */
	private void parseMockConfiguration() {
		Map<String, Object> beans = safeMap(actx.getBeansWithAnnotation(EnableIamMockTest.class));
		if (!(nonNull(beans) && beans.size() == 1)) {
			throw new Error(format("Shouldn't be here. please check config: @%s", EnableIamMockTest.class.getSimpleName()));
		}

		Object bootstrapBean = beans.entrySet().iterator().next().getValue();
		Class<?> bootClass = bootstrapBean.getClass();
		if (isAopProxy(bootstrapBean)) {
			bootClass = getTargetClass(bootstrapBean);
		}
		EnableIamMockTest anno = AnnotationUtils.findAnnotation(bootClass, EnableIamMockTest.class);

		// Organizations
		this.mockOrganization = new PrincipalOrganization();
		if (nonNull(anno.organizations())) {
			for (IamMockOrganization org : anno.organizations()) {
				this.mockOrganization.getOrganizations()
						.add(new OrganizationInfo(org.organizationCode(), org.parent(), org.type(), org.name(), org.areaId()));
			}
		}

		// Roles/permissions.
		this.roles = anno.roles();
		this.permissions = anno.permissions();

	}

	/**
	 * Apply mock service properties.
	 */
	private void applyMockServiceProperties() {
		String thatPort = actx.getEnvironment().getRequiredProperty("server.port");
		String thatCtxPath = actx.getEnvironment().getRequiredProperty("server.servlet.context-path");
		config.setServerUri("http://localhost:".concat(thatPort).concat("/").concat(thatCtxPath));
	}

}
