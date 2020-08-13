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
package com.wl4g.iam.test.mock.configure;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static com.wl4g.components.common.collection.Collections2.safeList;
import static com.wl4g.components.common.collection.Collections2.safeMap;
import static com.wl4g.components.core.utils.AopUtils2.*;
import static com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockFilter;
import static com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockUserInfo;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.typesafe.config.Config;
import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.common.typesafe.HoconConfigUtils;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.common.config.ReplayProperties;
import com.wl4g.iam.common.config.XsrfProperties;
import com.wl4g.iam.common.subject.IamPrincipalInfo.OrganizationInfo;
import com.wl4g.iam.common.subject.IamPrincipalInfo.PrincipalOrganization;
import com.wl4g.iam.test.mock.annotation.EnableIamMockAutoConfiguration;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockFilterType;

/**
 * {@link MockConfigurationInitializer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-08-10
 * @sine v1.0.0
 * @see
 */
public class MockConfigurationInitializer implements InitializingBean {

	protected final SmartLogger log = getLogger(getClass());

	/** {@link ApplicationContext} */
	@Autowired
	protected ApplicationContext actx;

	/** Mock config of {@link MockConfigurationFactory} */
	@Autowired
	protected MockConfigurationFactory mockFactory;

	/** {@link IamClientProperties} */
	@Autowired(required = false)
	protected IamClientProperties coreConfig;

	/** {@link ReplayProperties} */
	@Autowired(required = false)
	protected ReplayProperties replayConfig;

	/** {@link XsrfProperties} */
	@Autowired(required = false)
	protected XsrfProperties xsrfConfig;

	@Override
	public void afterPropertiesSet() throws Exception {
		applyDefaultMockContextProperties();
		loadMockConfiguration();
	}

	/**
	 * Apply sets mock context service properties.
	 */
	private void applyDefaultMockContextProperties() {
		// IAM server configuration
		if (nonNull(coreConfig)) {
			String thatPort = actx.getEnvironment().getRequiredProperty("server.port");
			String thatCtxPath = actx.getEnvironment().getRequiredProperty("server.servlet.context-path");
			coreConfig.setServerUri("http://localhost:".concat(thatPort).concat("/").concat(thatCtxPath));
		}

		// IAM replay configuration
		if (nonNull(replayConfig)) {
			replayConfig.getExcludeValidUriPatterns().add(URI_PATTERN_ALL);
		}

		// IAM xsrf configuration
		if (nonNull(xsrfConfig)) {
			xsrfConfig.getExcludeValidUriPatterns().add(URI_PATTERN_ALL);
		}
	}

	/**
	 * Parse mock configuration
	 */
	private void loadMockConfiguration() {
		Map<String, Object> beans = safeMap(actx.getBeansWithAnnotation(EnableIamMockAutoConfiguration.class));
		if (!(nonNull(beans) && beans.size() == 1)) {
			throw new Error(
					format("Shouldn't be here. please check config: @%s", EnableIamMockAutoConfiguration.class.getSimpleName()));
		}

		Object bootstrapBean = beans.entrySet().iterator().next().getValue();
		Class<?> bootClass = bootstrapBean.getClass();
		if (isAopProxy(bootstrapBean)) {
			bootClass = getTargetClass(bootstrapBean);
		}
		EnableIamMockAutoConfiguration anno = findAnnotation(bootClass, EnableIamMockAutoConfiguration.class);

		// Load & parse mock configuration
		parseMockConfiguration(anno.location());

		log.info("Resolved mock configuration: {}", toJSONString(mockFactory));
	}

	/**
	 * Parse mock (HOCON) configuration
	 * 
	 * @param location
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void parseMockConfiguration(String location) {
		Config config = HoconConfigUtils.loadConfig(location);

		List<Config> mocks = (List<Config>) config.getConfigList("iam.mocks");
		for (Config mock : mocks) {
			Config pinfo = mock.getConfig("userinfo");
			String principalId = pinfo.getString("principal-id");
			String principal = pinfo.getString("principal");
			String roles = pinfo.getString("roles");
			String permissions = pinfo.getString("permissions");

			PrincipalOrganization porgan = new PrincipalOrganization();
			List<Config> organs = (List<Config>) pinfo.getConfigList("organization");
			for (Config org : safeList(organs)) {
				String name = org.getString("name");
				String code = org.getString("code");
				int type = org.getInt("type");
				int areaId = org.getInt("area-id");
				String parent = org.getString("parent");
				porgan.getOrganizations().add(new OrganizationInfo(code, parent, type, name, areaId));
			}

			// Mock user
			MockUserInfo user = new MockUserInfo(principalId, principal, roles, permissions, porgan);

			// Mock filter
			MockFilterType type = mock.getEnum(MockFilterType.class, "filter.type");
			String value = mock.getString("filter.value");

			// Register mock configuration
			mockFactory.register(new MockFilter(type, value), user);
		}

	}

	/** URI mapping any */
	public static final String URI_PATTERN_ALL = "/**";

}
