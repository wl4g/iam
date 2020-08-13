package com.wl4g.iam.test.mock.configure;

import static java.util.Collections.unmodifiableMap;
import static com.wl4g.components.common.lang.Assert2.hasTextOf;
import static com.wl4g.components.common.lang.Assert2.isNullOf;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import com.wl4g.iam.common.subject.IamPrincipalInfo.PrincipalOrganization;
import static com.wl4g.components.common.web.WebUtils2.getHttpRemoteAddr;
import static com.wl4g.components.common.web.CookieUtils.getCookie;

/**
 * {@link MockConfigurationFactory}
 *
 * @since
 */
public class MockConfigurationFactory {

	/**
	 * Mock for IAM client authentication userinfo.
	 */
	private final Map<MockFilter, MockUserInfo> users = new ConcurrentHashMap<>(4);

	public Map<MockFilter, MockUserInfo> getUsers() {
		return unmodifiableMap(users);
	}

	/**
	 * Register mock filter configuration
	 * 
	 * @param filter
	 * @param info
	 */
	void register(MockFilter filter, MockUserInfo info) {
		isNullOf(users.putIfAbsent(filter, info), "Cannot register mock filter, becasue already exist");
	}

	/**
	 * {@link MockUserInfo}
	 *
	 * @since
	 */
	public static class MockUserInfo {

		/** Mock principalId */
		final private String principalId;

		/** Mock principal */
		final private String principal;

		/** Mock roles */
		final private String roles;

		/** Mock permissions */
		final private String permissions;

		/** {@link PrincipalOrganization} */
		final private PrincipalOrganization organization;

		public MockUserInfo(String principalId, String principal, String roles, String permissions,
				PrincipalOrganization organization) {
			super();
			this.principalId = principalId;
			this.principal = principal;
			this.roles = roles;
			this.permissions = permissions;
			this.organization = organization;
		}

		public String getPrincipalId() {
			return principalId;
		}

		public String getPrincipal() {
			return principal;
		}

		public String getRoles() {
			return roles;
		}

		public String getPermissions() {
			return permissions;
		}

		public PrincipalOrganization getOrganization() {
			return organization;
		}

	}

	/**
	 * {@link MockFilterType}
	 *
	 * @since
	 */
	public static enum MockFilterType {
		Ip((request, name) -> getHttpRemoteAddr(request)),

		Query((request, name) -> request.getParameter(name)),

		Header((request, name) -> request.getHeader(name)),

		Cookie((request, name) -> getCookie(request, name)),

		Request((request, name) -> {
			String value = Query.getParser().parseValue(request, name);
			value = isBlank(value) ? Header.getParser().parseValue(request, name) : value;
			value = isBlank(value) ? Cookie.getParser().parseValue(request, name) : value;
			return value;
		});

		/** {@link MockFilterParser} */
		private final MockFilterParser parser;

		private MockFilterType(MockFilterParser parser) {
			notNullOf(parser, "parser");
			this.parser = parser;
		}

		public MockFilterParser getParser() {
			return parser;
		}

		/**
		 * Parse mock filter real value by type
		 * 
		 * @param request
		 * @param type
		 * @return
		 */
		public static MockFilterType safeOf(String filterType) {
			for (MockFilterType type : values()) {
				if (type.name().equalsIgnoreCase(filterType)) {
					return type;
				}
			}
			return null;
		}

		/**
		 * {@link MockFilterParser}
		 *
		 * @since
		 */
		static interface MockFilterParser {
			String parseValue(HttpServletRequest request, String name);
		}

	}

	/**
	 * {@link MockFilter}
	 *
	 * @since
	 */
	public static class MockFilter {

		final private MockFilterType type;
		final private String value;

		public MockFilter(MockFilterType type, String value) {
			notNullOf(type, "mockFilterType");
			hasTextOf(value, "mockFilterValue");
			this.type = type;
			this.value = value;
		}

		public MockFilterType getType() {
			return type;
		}

		public String getValue() {
			return value;
		}

	}

}
