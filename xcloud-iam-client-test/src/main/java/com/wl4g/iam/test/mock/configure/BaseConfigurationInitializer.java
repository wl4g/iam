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
import static com.wl4g.component.common.collection.Collections2.safeList;
import static com.wl4g.component.common.collection.Collections2.safeMap;
import static com.wl4g.component.common.lang.Assert2.hasText;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.core.utils.AopUtils2.*;
import static com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockFilter;
import static com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockFilter.MockFilterType;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.springframework.http.HttpMethod.*;

import com.google.common.net.InetAddresses;
import com.typesafe.config.Config;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.typesafe.HoconConfigUtils;
import com.wl4g.iam.client.config.IamClientProperties;
import com.wl4g.iam.common.subject.IamPrincipal.OrganizationInfo;
import com.wl4g.iam.common.subject.IamPrincipal.PrincipalOrganization;
import com.wl4g.iam.core.config.CorsProperties;
import com.wl4g.iam.core.config.ReplayProperties;
import com.wl4g.iam.core.config.XsrfProperties;
import com.wl4g.iam.core.config.CorsProperties.CorsRule;
import com.wl4g.iam.test.mock.annotation.EnableIamMockAutoConfiguration;
import com.wl4g.iam.test.mock.configure.MockConfigurationFactory.MockAuthzInfo;

/**
 * {@link BaseConfigurationInitializer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-08-10
 * @sine v1.0.0
 * @see
 */
abstract class BaseConfigurationInitializer implements InitializingBean {

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

	/** {@link CorsProperties} */
	@Autowired(required = false)
	protected CorsProperties corsConfig;

	/** {@link ReplayProperties} */
	@Autowired(required = false)
	protected ReplayProperties replayConfig;

	/** {@link XsrfProperties} */
	@Autowired(required = false)
	protected XsrfProperties xsrfConfig;

	/** Mock configuration URL. */
	protected URL configURL;

	/** Mock enable debug. */
	protected boolean enableVerbose;

	public final URL getConfigURL() {
		return configURL;
	}

	public final boolean isEnableVerbose() {
		return enableVerbose;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		applyDefaultMockProperties();
		loadMockConfiguration();
	}

	/**
	 * Apply sets mock context service properties.
	 */
	private void applyDefaultMockProperties() {
		// IAM server configuration
		if (nonNull(coreConfig)) {
			String thatPort = actx.getEnvironment().getRequiredProperty("server.port");
			String thatCtxPath = actx.getEnvironment().getRequiredProperty("server.servlet.context-path");
			coreConfig.setServerUri("http://localhost:".concat(thatPort).concat("/").concat(thatCtxPath));
		}

		// IAM cors configuration
		if (nonNull(corsConfig)) {
			corsConfig.getRules().getOrDefault(URI_PATTERN_ALL, new CorsRule()).addAllowsOrigins(RESOURCE_ALL)
					.setAllowCredentials(true).addAllowsHeaders(RESOURCE_ALL).addAllowsMethods(METHOD_ALL);
			/**
			 * Flush cors configuration </br>
			 * 
			 * @see {@link CorsAutoConfiguration#corsSecurityFilter(CorsProperties,IamCorsProcessor)}
			 **/
			corsConfig.getRules().values().forEach(r -> r.resolveIamCorsConfiguration());
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
		Config config = HoconConfigUtils.loadConfig(location).resolve();
		// e.g: for throw print
		this.configURL = config.origin().url();
		// Enable verbose logs print
		this.enableVerbose = config.getBoolean("iam.enable-log-verbose");

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
				Integer type = org.getInt("type");
				Long areaId = org.getLong("area-id");
				String parent = org.getString("parent");
				porgan.getOrganizations().add(new OrganizationInfo(code, parent, type, name, areaId));
			}

			// Mock user
			MockAuthzInfo user = new MockAuthzInfo(principalId, principal, roles, permissions, porgan);

			// Mock filter
			MockFilterType type = MockFilterType.of(mock.getString("filter.type"));
			String value = mock.getString("filter.value");

			// Check filter
			checkFilterTypeAndValue(type, value);

			// Register mock configuration
			mockFactory.register(new MockFilter(type, value), user);
		}
	}

	/**
	 * Check filter type and value
	 * 
	 * @param type
	 * @param value
	 */
	private void checkFilterTypeAndValue(MockFilterType type, String value) {
		switch (type) {
		case All: // Any allows
			break;
		case Ip:
			hasText(value, "Ip filter value '%s' is requires", value);
			InetAddresses.forString(value); // Check IpString
			break;
		default:
			isTrue((!isBlank(value) && value.contains("=")), "Invalid filter value of '%s', Missing expected '='", value);
			break;
		}
	}

	/** URI mapping any */
	public static final String URI_PATTERN_ALL = "/**";
	/** Resource any */
	public static final String RESOURCE_ALL = "*";
	/** Http method any */
	public static final String[] METHOD_ALL = { GET.name(), POST.name(), HEAD.name(), OPTIONS.name(), PUT.name(), DELETE.name(),
			TRACE.name() };

}
